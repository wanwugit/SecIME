package org.fcitx.fcitx5.android.input.bus

import org.fcitx.fcitx5.android.input.SecLogger
import org.secureime.sect9.bus.CandidatePipeline
import org.secureime.sect9.bus.CandidateState
import org.secureime.sect9.bus.splitPinyinSyllables
import org.fcitx.fcitx5.android.input.candidates.horizontal.HorizontalCandidateComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CandidateViewAdapter(
    private val pipeline: CandidatePipeline,
    private val horizontalCandidate: HorizontalCandidateComponent
) {
    /** Callback with current syllable options for the pinyin panel */
    var onPinyinUpdate: ((List<String>) -> Unit)? = null

    /** Which syllable index to extract — dynamically read from InputDecisionBus */
    var getSyllableIndex: () -> Int = { 0 }

    /** Current pending digits — used to generate pinyin options */
    var getPendingDigits: () -> String = { "" }

    fun startCollecting(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            pipeline.state.collect { state ->
                SecLogger.d("CandAdapter", "collect: candidates.size=${state.candidates.size}, total=${state.total}")
                if (state.candidates.isEmpty()) {
                    horizontalCandidate.view.visibility = android.view.View.GONE
                    onPinyinUpdate?.invoke(emptyList())
                } else {
                    horizontalCandidate.updateFromPipeline(state.candidates.toTypedArray(), state.total)
                    horizontalCandidate.view.visibility = android.view.View.VISIBLE
                    val idx = getSyllableIndex()
                    val pending = getPendingDigits()
                    // Extract full syllables from comments
                    val fullSyllables = state.comments
                        .filter { it.isNotEmpty() }
                        .mapNotNull { comment ->
                            val syllables = splitPinyinSyllables(comment)
                            if (idx < syllables.size) syllables[idx] else null
                        }
                        .distinct()
                    // Generate all valid pinyin prefixes from pending digits
                    val digitPinyin = if (pending.isNotEmpty()) {
                        t9PinyinOptions(pending)
                    } else emptyList()
                    val syllableOptions = (fullSyllables + digitPinyin).distinct()
                    SecLogger.d("CandAdapter", "syllableIndex=$idx, pending='$pending', digitPinyin=$digitPinyin, fullSyllables=$fullSyllables, options=$syllableOptions")
                    onPinyinUpdate?.invoke(syllableOptions)
                }
            }
        }
    }
}

/** T9 digit-to-letter mapping */
private val T9_MAP = mapOf(
    '2' to "abc", '3' to "def", '4' to "ghi",
    '5' to "jkl", '6' to "mno", '7' to "pqrs",
    '8' to "tuv", '9' to "wxyz"
)

/** Valid pinyin syllables and initials (for prefix matching) */
private val VALID_PINYIN = setOf(
    // Single-letter initials (abbreviations)
    "a", "b", "c", "d", "e", "f", "g", "h",
    "j", "k", "l", "m", "n", "o", "p", "q",
    "r", "s", "t", "w", "x", "y", "z",
    // Full syllables
    "ai", "an", "ang", "ao",
    "ba", "bai", "ban", "bang", "bao", "bei", "ben", "beng", "bi", "bian", "biao", "bie", "bin", "bing", "bo", "bu",
    "ca", "cai", "can", "cang", "cao", "ce", "cen", "ceng", "cha", "chai", "chan", "chang", "chao", "che", "chen", "cheng", "chi", "chong", "chou", "chu", "chua", "chuai", "chuan", "chuang", "chui", "chun", "chuo", "ci", "cong", "cou", "cu", "cuan", "cui", "cun", "cuo",
    "da", "dai", "dan", "dang", "dao", "de", "dei", "den", "deng", "di", "dian", "diao", "die", "ding", "diu", "dong", "dou", "du", "duan", "dui", "dun", "duo",
    "e", "ei", "en", "er",
    "fa", "fan", "fang", "fei", "fen", "feng", "fo", "fou", "fu",
    "ga", "gai", "gan", "gang", "gao", "ge", "gei", "gen", "geng", "gong", "gou", "gu", "gua", "guai", "guan", "guang", "gui", "gun", "guo",
    "ha", "hai", "han", "hang", "hao", "he", "hei", "hen", "heng", "hong", "hou", "hu", "hua", "huai", "huan", "huang", "hui", "hun", "huo",
    "ji", "jia", "jian", "jiang", "jiao", "jie", "jin", "jing", "jiong", "jiu", "ju", "juan", "jue", "jun",
    "ka", "kai", "kan", "kang", "kao", "ke", "ken", "keng", "kong", "kou", "ku", "kua", "kuai", "kuan", "kuang", "kui", "kun", "kuo",
    "la", "lai", "lan", "lang", "lao", "le", "lei", "leng", "li", "lia", "lian", "liang", "liao", "lie", "lin", "ling", "liu", "lo", "long", "lou", "lu", "lv", "luan", "lve", "lun", "luo",
    "ma", "mai", "man", "mang", "mao", "me", "mei", "men", "meng", "mi", "mian", "miao", "mie", "min", "ming", "miu", "mo", "mou", "mu",
    "na", "nai", "nan", "nang", "nao", "ne", "nei", "nen", "neng", "ni", "nian", "niang", "niao", "nie", "nin", "ning", "niu", "nong", "nou", "nu", "nv", "nuan", "nve", "nun", "nuo",
    "ou",
    "pa", "pai", "pan", "pang", "pao", "pei", "pen", "peng", "pi", "pian", "piao", "pie", "pin", "ping", "po", "pou", "pu",
    "qi", "qia", "qian", "qiang", "qiao", "qie", "qin", "qing", "qiong", "qiu", "qu", "quan", "que", "qun",
    "ran", "rang", "rao", "re", "ren", "reng", "ri", "rong", "rou", "ru", "rua", "ruan", "rui", "run", "ruo",
    "sa", "sai", "san", "sang", "sao", "se", "sen", "seng", "sha", "shai", "shan", "shang", "shao", "she", "shei", "shen", "sheng", "shi", "shou", "shu", "shua", "shuai", "shuan", "shuang", "shui", "shun", "shuo", "si", "song", "sou", "su", "suan", "sui", "sun", "suo",
    "ta", "tai", "tan", "tang", "tao", "te", "teng", "ti", "tian", "tiao", "tie", "ting", "tong", "tou", "tu", "tuan", "tui", "tun", "tuo",
    "wa", "wai", "wan", "wang", "wei", "wen", "weng", "wo", "wu",
    "xi", "xia", "xian", "xiang", "xiao", "xie", "xin", "xing", "xiong", "xiu", "xu", "xuan", "xue", "xun",
    "ya", "yan", "yang", "yao", "ye", "yi", "yin", "ying", "yo", "yong", "you", "yu", "yuan", "yue", "yun",
    "za", "zai", "zan", "zang", "zao", "ze", "zei", "zen", "zeng", "zha", "zhai", "zhan", "zhang", "zhao", "zhe", "zhei", "zhen", "zheng", "zhi", "zhong", "zhou", "zhu", "zhua", "zhuai", "zhuan", "zhuang", "zhui", "zhun", "zhuo", "zi", "zong", "zou", "zu", "zuan", "zui", "zun", "zuo"
)

/**
 * Generate all valid pinyin options from a T9 digit sequence.
 * Returns pinyin of all possible prefix lengths (1 digit, 2 digits, ... all digits).
 * For "64" → [m, n, o, mi, ni]  (1-letter abbreviations + 2-letter syllables)
 * For "6443" → [m, n, o, mi, ni, ming]  (1-letter + 2-letter + 4-letter)
 */
private fun t9PinyinOptions(digits: String): List<String> {
    if (digits.isEmpty()) return emptyList()
    val results = mutableSetOf<String>()
    // Try all prefix lengths: consume 1 digit, 2 digits, ... up to all digits
    for (prefixLen in 1..digits.length) {
        val prefixDigits = digits.substring(0, prefixLen)
        // Generate all letter combinations for this prefix
        var candidates = listOf("")
        for (digit in prefixDigits) {
            val letters = T9_MAP[digit] ?: continue
            candidates = candidates.flatMap { prefix ->
                letters.map { ch -> prefix + ch }
            }
        }
        // Filter to valid pinyin
        candidates.filter { it in VALID_PINYIN }.forEach { results.add(it) }
    }
    // Sort: full syllables by length desc (longer=more precise first), then single-letter abbreviations
    return results.sortedWith(compareBy({ it.length == 1 }, { -it.length }, { it }))
}
