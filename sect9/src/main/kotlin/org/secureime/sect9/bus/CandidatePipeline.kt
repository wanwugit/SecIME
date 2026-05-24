package org.secureime.sect9.bus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CandidateState(
    val candidates: List<String> = emptyList(),
    val comments: List<String> = emptyList(),
    val total: Int = -1
)

class CandidatePipeline {
    private val _state = MutableStateFlow(CandidateState())
    val state: StateFlow<CandidateState> = _state.asStateFlow()

    fun publish(candidates: List<String>, comments: List<String>, total: Int = -1) {
        _state.value = CandidateState(candidates, comments, total)
    }

    fun clear() {
        _state.value = CandidateState()
    }
}

fun splitTextComment(combined: String): Pair<String, String> {
    val lastCjk = combined.indexOfLast { isCJK(it) }
    if (lastCjk < 0) return Pair(combined, "")
    val spaceIdx = combined.indexOf(' ', lastCjk + 1)
    if (spaceIdx < 0) return Pair(combined, "")
    return Pair(combined.substring(0, spaceIdx), combined.substring(spaceIdx + 1))
}

fun isCJK(c: Char): Boolean =
    c.code in 0x4E00..0x9FFF || c.code in 0x3400..0x4DBF ||
    c.code in 0x3000..0x303F || c.code in 0xFF00..0xFFEF

fun isPurePinyinCandidate(text: String, comment: String): Boolean {
    if (text.isEmpty()) return false
    if (!text.all { it in 'a'..'z' }) return false
    return comment == text || comment.split(' ').any { it == text }
}

/**
 * Split a pinyin string into individual syllables.
 * Rime t9 spelling_hints comments use apostrophe as syllable separator:
 *   "ni'ming'hao" → ["ni", "ming", "hao"]
 * Single syllable: "ni" → ["ni"]
 * If no separator found, fall back to rule-based splitting using
 * the pinyin initial/final pattern.
 */
fun splitPinyinSyllables(pinyin: String): List<String> {
    if (pinyin.isEmpty()) return emptyList()
    // Apostrophe separator (Rime standard)
    if (pinyin.contains('\'')) {
        return pinyin.split('\'').filter { it.isNotEmpty() }
    }
    // Space separator
    if (pinyin.contains(' ')) {
        return pinyin.split(' ').filter { it.isNotEmpty() }
    }
    // No separator — rule-based split
    return ruleBasedSplit(pinyin)
}

// Pinyin initials (声母) — ordered longest first for greedy matching
private val PINYIN_INITIALS = listOf(
    "zh", "ch", "sh",
    "b", "p", "m", "f", "d", "t", "n", "l",
    "g", "k", "h", "j", "q", "x",
    "r", "z", "c", "s", "y", "w"
)

// Pinyin finals (韵母) that can stand alone as a syllable
private val STANDALONE_FINALS = setOf(
    "a", "ai", "an", "ang", "ao",
    "e", "ei", "en", "eng", "er",
    "o", "ou",
    "i", "ia", "ian", "iang", "iao", "ie", "in", "ing", "iong", "iu",
    "u", "ua", "uai", "uan", "uang", "ui", "un", "uo",
    "v", "ve", "vn"
)

/**
 * Rule-based pinyin syllable splitting.
 * Greedy scan: try to match initial + final as one syllable.
 * If no initial matches, try standalone final.
 */
private fun ruleBasedSplit(pinyin: String): List<String> {
    val syllables = mutableListOf<String>()
    var pos = 0
    while (pos < pinyin.length) {
        // Try matching an initial
        val initial = PINYIN_INITIALS.firstOrNull { pinyin.startsWith(it, pos) }
        val initLen = initial?.length ?: 0
        // Try matching a final after the initial
        var matched = false
        for (finalLen in (pinyin.length - pos - initLen) downTo 1) {
            val final = pinyin.substring(pos + initLen, pos + initLen + finalLen)
            if (final in STANDALONE_FINALS) {
                syllables.add(pinyin.substring(pos, pos + initLen + finalLen))
                pos += initLen + finalLen
                matched = true
                break
            }
        }
        if (!matched) {
            // No initial+final match — try standalone final
            for (finalLen in (pinyin.length - pos) downTo 1) {
                val final = pinyin.substring(pos, pos + finalLen)
                if (final in STANDALONE_FINALS) {
                    syllables.add(final)
                    pos += finalLen
                    matched = true
                    break
                }
            }
        }
        if (!matched) {
            // Cannot parse — take remaining as one chunk
            syllables.add(pinyin.substring(pos))
            break
        }
    }
    return syllables
}