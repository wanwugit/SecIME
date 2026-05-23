package org.fcitx.fcitx5.android.input.bus

import org.fcitx.fcitx5.android.core.ScancodeMapping
import org.fcitx.fcitx5.android.input.SecLogger
import org.secureime.sect9.bus.CandidatePipeline
import org.secureime.sect9.bus.PreeditPipeline
import org.secureime.sect9.bus.PreeditState
import org.fcitx.fcitx5.android.input.FcitxInputMethodService
import org.fcitx.fcitx5.android.input.bar.EncryptionBarComponent
import org.fcitx.fcitx5.android.input.dependency.inputMethodService
import org.mechdancer.dependency.Dependent
import org.mechdancer.dependency.UniqueComponent
import org.mechdancer.dependency.manager.ManagedHandler
import org.mechdancer.dependency.manager.managedHandler
import org.mechdancer.dependency.manager.must
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class InputDecisionBus :
    UniqueComponent<InputDecisionBus>(), Dependent, ManagedHandler by managedHandler() {

    private val languageAdapter: LanguageAdapterComponent by manager.must()
    private val encryptionBar: EncryptionBarComponent by manager.must()
    private val service: FcitxInputMethodService by manager.inputMethodService()

    val candidatePipeline = CandidatePipeline()
    val preeditPipeline = PreeditPipeline()

    // ── T9 逐音节状态 ────────────────────────────────────────────

    /** 已确认的拼音字母列表（如 ["ni", "he"]），用于 rebuildInput 发送给 Rime */
    private val confirmedSyllables = mutableListOf<String>()

    /** 已确认的数字组列表（如 ["64", "43"]），与 confirmedSyllables 并行，用于退格回退 */
    private val confirmedDigitGroups = mutableListOf<String>()

    /** 当前未确认的数字序列（如 "43"） */
    var pendingDigits = ""
        private set

    /** 当前音节索引 = confirmedSyllables.size，用于从候选词注释中提取当前音节 */
    val currentSyllableIndex: Int get() = confirmedSyllables.size

    /** rebuildInput 期间屏蔽中间态更新 */
    @Volatile
    var isRebuilding = false

    /** rebuildInput 期间缓存的最后一次候选词数据，用于结束后重放 */
    @Volatile
    private var lastSuppressedCandidates: Array<String>? = null

    @Volatile
    private var lastSuppressedCandidatesTotal: Int = -1

    /** rebuildInput 期间缓存的最后一次 preedit 数据，用于结束后重放 */
    @Volatile
    private var lastSuppressedPreedit: String? = null

    @Volatile
    private var lastSuppressedAuxUp: String? = null

    @Volatile
    private var lastSuppressedAuxDown: String? = null

    /** 清空逐音节状态 */
    private fun clearSyllableState() {
        confirmedSyllables.clear()
        confirmedDigitGroups.clear()
        pendingDigits = ""
    }

    /** 重建输入：reset Rime → 重发已确认音节（字母+分词符）+ 当前数字 */
    private suspend fun rebuildInput() {
        isRebuilding = true
        lastSuppressedCandidates = null
        lastSuppressedPreedit = null
        languageAdapter.reset()
        for (syllable in confirmedSyllables) {
            for (ch in syllable) {
                val code = ScancodeMapping.charToScancode(ch)
                languageAdapter.sendKey(ch.toString(), 0, code)
            }
            languageAdapter.sendKey("'", 0, '\''.code)
        }
        for (digit in pendingDigits) {
            val code = ScancodeMapping.charToScancode(digit)
            languageAdapter.sendKey(digit.toString(), 0, code)
        }
        isRebuilding = false
        // Replay last suppressed events so UI gets the final correct state
        lastSuppressedCandidates?.let { candidates ->
            lastSuppressedCandidatesTotal?.let { total ->
                onFcitxCandidateEvent(candidates, total)
            }
        }
        lastSuppressedPreedit?.let { preedit ->
            onFcitxPreeditEvent(
                preedit,
                lastSuppressedAuxUp ?: "",
                lastSuppressedAuxDown ?: ""
            )
        }
    }

    // ── T9 按键入口 ───────────────────────────────────────────

    /** T9 数字键：记录数字到 pendingDigits，发送给 Rime */
    fun onDigitPress(digit: Char): Boolean {
        pendingDigits += digit
        SecLogger.d("Bus", "Bus.onDigitPress: digit='$digit', pending='$pendingDigits', syllables=${confirmedSyllables.toList()}, digitGroups=${confirmedDigitGroups.toList()}")
        val code = ScancodeMapping.charToScancode(digit)
        service.lifecycleScope.launch { languageAdapter.sendKey(digit.toString(), 0, code) }
        return true
    }

    /** T9 分词键：发送单引号，Rime t9_processor 在此处切分音节 */
    fun onSegment(): Boolean {
        SecLogger.d("Bus", "Bus.onSegment")
        service.lifecycleScope.launch { languageAdapter.sendKey("'", 0, '\''.code) }
        return true
    }

    /** T9 退格：先删 pendingDigits 的末尾数字，再删最后一个已确认组（数字恢复为 pending，字母从 syllables 删除） */
    fun onT9Backspace(): Boolean {
        SecLogger.d("Bus", "Bus.onT9Backspace: pending='$pendingDigits', syllables=${confirmedSyllables.toList()}, digitGroups=${confirmedDigitGroups.toList()}")
        if (pendingDigits.isNotEmpty()) {
            pendingDigits = pendingDigits.dropLast(1)
            service.lifecycleScope.launch { rebuildInput() }
        } else if (confirmedDigitGroups.isNotEmpty()) {
            pendingDigits = confirmedDigitGroups.removeLast()
            confirmedSyllables.removeLast()
            service.lifecycleScope.launch { rebuildInput() }
        } else {
            service.lifecycleScope.launch { languageAdapter.sendKeySym(0xFF08, 0) }
        }
        return true
    }

    /** T9 重置：清空所有状态 */
    fun onT9Reset() {
        SecLogger.d("Bus", "Bus.onT9Reset")
        clearSyllableState()
        candidatePipeline.clear()
        preeditPipeline.clear()
        service.lifecycleScope.launch { languageAdapter.reset() }
    }

    // ── QWERTY 通道 ────────────────────────────────────────────

    fun onQwertyKey(key: String, states: Int, code: Int): Boolean {
        SecLogger.d("Bus", "Bus.onQwertyKey: key='$key', states=$states, code=$code")
        service.lifecycleScope.launch { languageAdapter.sendKey(key, states, code) }
        return true
    }

    fun onQwertyKeySym(sym: Int, states: Int): Boolean {
        SecLogger.d("Bus", "Bus.onQwertyKeySym: sym=$sym, states=$states")
        service.lifecycleScope.launch { languageAdapter.sendKeySym(sym, states) }
        return true
    }

    // ── 候选词选择（T9 和 QWERTY 统一） ────────────────────────

    fun onUiCandidateSelect(index: Int) {
        SecLogger.d("Bus", "Bus.onUiCandidateSelect: index=$index")
        clearSyllableState()
        service.lifecycleScope.launch { languageAdapter.select(index) }
    }

    fun onUiCandidatePageOffset(offset: Int) {
        SecLogger.d("Bus", "Bus.onUiCandidatePageOffset: offset=$offset")
        service.lifecycleScope.launch { languageAdapter.offsetCandidatePage(offset) }
    }

    // ── 文本提交 ────────────────────────────────────────────────

    fun onCommitText(text: String) {
        SecLogger.d("Bus", "Bus.onCommitText: text='$text'")
        if (encryptionBar.isEncrypting) {
            encryptionBar.appendToBuffer(text)
            return
        }
        clearSyllableState()
        service.lifecycleScope.launch { languageAdapter.commitText(text) }
        candidatePipeline.clear()
        preeditPipeline.clear()
    }

    fun onCommitTextToApp(text: String) {
        SecLogger.d("Bus", "Bus.onCommitTextToApp: text='$text'")
        if (encryptionBar.isEncrypting) {
            encryptionBar.appendToBuffer(text)
            return
        }
        service.lifecycleScope.launch { languageAdapter.commitText(text) }
    }

    fun onCommitCurrentPreedit() {
        SecLogger.d("Bus", "Bus.onCommitCurrentPreedit")
        service.lifecycleScope.launch { languageAdapter.commitCurrentPreedit() }
    }

    // ── 语言/输入法切换 ─────────────────────────────────────────

    fun onLangSwitchEnumerate() {
        service.lifecycleScope.launch { languageAdapter.enumerateIme() }
    }

    fun onLangSwitchToggle() {
        service.lifecycleScope.launch { languageAdapter.toggleIme() }
    }

    fun onQuickPhrase() {
        service.lifecycleScope.launch {
            languageAdapter.commitCurrentPreedit()
            languageAdapter.triggerQuickPhrase()
        }
    }

    fun onUnicode() {
        service.lifecycleScope.launch {
            languageAdapter.commitCurrentPreedit()
            languageAdapter.triggerUnicode()
        }
    }

    /** T9 拼音选择：记录字母到 confirmedSyllables + 对应数字到 confirmedDigitGroups，清空已消耗的 pendingDigits */
    fun onPinyinSelect(pinyin: String) {
        val consumedDigits = pinyin.length.coerceAtMost(pendingDigits.length)
        val remainingDigits = if (consumedDigits < pendingDigits.length) pendingDigits.substring(consumedDigits) else ""
        SecLogger.d("Bus", "Bus.onPinyinSelect: pinyin='$pinyin', consumedDigits=$consumedDigits, pending='$pendingDigits'→'$remainingDigits', syllables=${confirmedSyllables.toList()}, digitGroups=${confirmedDigitGroups.toList()}")
        confirmedSyllables.add(pinyin)
        if (consumedDigits > 0) {
            confirmedDigitGroups.add(pendingDigits.substring(0, consumedDigits))
        }
        pendingDigits = remainingDigits
        candidatePipeline.clear()
        preeditPipeline.clear()
        service.lifecycleScope.launch { rebuildInput() }
    }

    // ── Fcitx 事件通道（T9 和 QWERTY 统一流动） ────────────────

    fun onFcitxCandidateEvent(candidates: Array<String>, total: Int = -1) {
        if (isRebuilding) {
            lastSuppressedCandidates = candidates
            lastSuppressedCandidatesTotal = total
            return
        }
        SecLogger.d("Bus", "Bus.onFcitxCandidateEvent: candidates=${candidates.toList()}, total=$total")
        val texts = mutableListOf<String>()
        val comments = mutableListOf<String>()
        for (c in candidates) {
            val (text, comment) = org.secureime.sect9.bus.splitTextComment(c)
            // Filter out pure-pinyin candidates (e.g. "ni", "mi", "n", "ming")
            // Rime T9 returns pinyin as candidate text when no matching word exists
            // Keep English words like "OK", "no", "me" — they have empty comment or different structure
            if (isPurePinyin(text, comment)) {
                continue
            }
            texts.add(text)
            comments.add(comment)
        }
        candidatePipeline.publish(texts, comments, texts.size)
    }

    /** Check if a candidate is a pure pinyin syllable that should only appear in the pinyin panel */
    private fun isPurePinyin(text: String, comment: String): Boolean =
        org.secureime.sect9.bus.isPurePinyinCandidate(text, comment)

    /** T9 模式下由前端构建的 preedit：已确认音节(撇号分隔) + 未确认部分拼音(截断到用户实际输入的字母数) */
    private fun buildT9Preedit(rimePreedit: String): String {
        val sb = StringBuilder()
        for (syllable in confirmedSyllables) {
            sb.append(syllable)
            sb.append("'")
        }
        // Extract the unconfirmed part from Rime's preedit
        val rimeSyllables = rimePreedit.split(' ', '\'').filter { it.isNotEmpty() }
        val unconfirmed = rimeSyllables.drop(confirmedSyllables.size)

        if (pendingDigits.isEmpty() && unconfirmed.isEmpty()) {
            // Nothing unconfirmed — remove trailing apostrophe
            if (sb.isNotEmpty() && sb.last() == '\'') {
                sb.deleteCharAt(sb.length - 1)
            }
        } else if (pendingDigits.isNotEmpty() && unconfirmed.isNotEmpty()) {
            // Truncate unconfirmed pinyin to match actual digit count.
            // Each digit maps to one letter, so pendingDigits.length = max letters to show.
            val fullPinyin = unconfirmed.joinToString("")
            val truncated = if (fullPinyin.length > pendingDigits.length) {
                fullPinyin.substring(0, pendingDigits.length)
            } else {
                fullPinyin
            }
            val truncatedSyllables = splitTruncatedSyllables(truncated, unconfirmed)
            sb.append(truncatedSyllables.joinToString("'"))
        } else if (pendingDigits.isNotEmpty() && unconfirmed.isEmpty()) {
            // Rime has no preedit but user has pending digits — show raw digits as fallback
            sb.append(pendingDigits)
        } else if (unconfirmed.isNotEmpty()) {
            // No pendingDigits but Rime has unconfirmed part (e.g. after rebuildInput before new digits)
            sb.append(unconfirmed.joinToString("'"))
        }
        return sb.toString()
    }

    /** Split truncated pinyin back into syllables using the original syllable boundaries as guide */
    private fun splitTruncatedSyllables(truncated: String, originalSyllables: List<String>): List<String> {
        val result = mutableListOf<String>()
        var pos = 0
        for (syllable in originalSyllables) {
            if (pos >= truncated.length) break
            val take = syllable.length.coerceAtMost(truncated.length - pos)
            result.add(truncated.substring(pos, pos + take))
            pos += take
        }
        // If there are remaining chars not covered by original syllables (shouldn't happen, but safe)
        if (pos < truncated.length) {
            result.add(truncated.substring(pos))
        }
        return result
    }

    fun onFcitxPreeditEvent(preedit: String, auxUp: String, auxDown: String) {
        if (isRebuilding) {
            lastSuppressedPreedit = preedit
            lastSuppressedAuxUp = auxUp
            lastSuppressedAuxDown = auxDown
            return
        }
        val displayPreedit = buildT9Preedit(preedit)
        SecLogger.d("Bus", "Bus.onFcitxPreeditEvent: rime='$preedit'→display='$displayPreedit', syllables=${confirmedSyllables.toList()}, pending='$pendingDigits'")
        val auxParts = mutableListOf<String>()
        if (auxUp.isNotEmpty()) auxParts.add(auxUp)
        if (auxDown.isNotEmpty()) auxParts.add(auxDown)
        preeditPipeline.publish(PreeditState(
            rawInput = "",
            decodedDisplay = displayPreedit,
            auxiliary = auxParts,
            fcitxPreedit = preedit
        ))
    }

    fun onFcitxPreeditMerge(fcitxPreedit: String) {
        val current = preeditPipeline.state.value
        preeditPipeline.publish(current.copy(
            fcitxPreedit = fcitxPreedit
        ))
    }
}