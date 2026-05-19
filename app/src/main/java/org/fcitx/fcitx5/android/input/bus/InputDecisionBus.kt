package org.fcitx.fcitx5.android.input.bus

import org.fcitx.fcitx5.android.core.ScancodeMapping
import org.fcitx.fcitx5.android.input.SecLogger
import org.secureime.sect9.bus.CandidatePipeline
import org.secureime.sect9.bus.PreeditPipeline
import org.secureime.sect9.bus.PreeditState
import org.fcitx.fcitx5.android.input.FcitxInputMethodService
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
    private val service: FcitxInputMethodService by manager.inputMethodService()

    val candidatePipeline = CandidatePipeline()
    val preeditPipeline = PreeditPipeline()

    // ── T9 按键入口 ───────────────────────────────────────────

    /** T9 数字键：发送给 fcitx，Rime speller algebra 处理数字→字母映射 */
    fun onDigitPress(digit: Char): Boolean {
        val code = ScancodeMapping.charToScancode(digit)
        SecLogger.d("Bus", "Bus.onDigitPress: digit='$digit', scancode=$code")
        service.lifecycleScope.launch { languageAdapter.sendKey(digit.toString(), 0, code) }
        return true
    }

    /** T9 分词键：发送单引号，Rime t9_processor 在此处切分音节 */
    fun onSegment(): Boolean {
        SecLogger.d("Bus", "Bus.onSegment")
        service.lifecycleScope.launch { languageAdapter.sendKey("'", 0, '\''.code) }
        return true
    }

    /** T9 退格：直接发给 fcitx，Rime 自行回退输入状态 */
    fun onT9Backspace(): Boolean {
        SecLogger.d("Bus", "Bus.onT9Backspace")
        service.lifecycleScope.launch { languageAdapter.sendKeySym(0xFF08, 0) }
        return true
    }

    /** T9 重置：清空 fcitx 输入状态 */
    fun onT9Reset() {
        SecLogger.d("Bus", "Bus.onT9Reset")
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
        service.lifecycleScope.launch { languageAdapter.select(index) }
    }

    fun onUiCandidatePageOffset(offset: Int) {
        SecLogger.d("Bus", "Bus.onUiCandidatePageOffset: offset=$offset")
        service.lifecycleScope.launch { languageAdapter.offsetCandidatePage(offset) }
    }

    // ── 文本提交 ────────────────────────────────────────────────

    fun onCommitText(text: String) {
        SecLogger.d("Bus", "Bus.onCommitText: text='$text'")
        service.lifecycleScope.launch { languageAdapter.commitText(text) }
        candidatePipeline.clear()
        preeditPipeline.clear()
    }

    fun onCommitTextToApp(text: String) {
        SecLogger.d("Bus", "Bus.onCommitTextToApp: text='$text'")
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

    // ── Fcitx 事件通道（T9 和 QWERTY 统一流动） ────────────────

    fun onFcitxCandidateEvent(candidates: Array<String>, total: Int = -1) {
        SecLogger.d("Bus", "Bus.onFcitxCandidateEvent: candidates=${candidates.toList()}, total=$total")
        val texts = mutableListOf<String>()
        val comments = mutableListOf<String>()
        for (c in candidates) {
            val (text, comment) = org.secureime.sect9.bus.splitTextComment(c)
            texts.add(text)
            comments.add(comment)
        }
        candidatePipeline.publish(texts, comments, total)
    }

    fun onFcitxPreeditEvent(preedit: String, auxUp: String, auxDown: String) {
        SecLogger.d("Bus", "Bus.onFcitxPreeditEvent: preedit='$preedit', auxUp='$auxUp', auxDown='$auxDown'")
        val auxParts = mutableListOf<String>()
        if (auxUp.isNotEmpty()) auxParts.add(auxUp)
        if (auxDown.isNotEmpty()) auxParts.add(auxDown)
        preeditPipeline.publish(PreeditState(
            rawInput = "",
            decodedDisplay = preedit,
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