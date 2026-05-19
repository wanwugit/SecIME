package org.fcitx.fcitx5.android.input.ninekey

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import org.fcitx.fcitx5.android.data.theme.Theme

class T9PreeditComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val digitView: TextView = TextView(context)
    private val pinyinView: TextView = TextView(context)
    private val speculativeView: TextView = TextView(context)
    private val fcitxPreeditView: TextView = TextView(context)

    private var theme: Theme? = null

    init {
        orientation = HORIZONTAL
        addView(digitView)
        addView(pinyinView)
        addView(speculativeView)
        addView(fcitxPreeditView)
    }

    fun setTheme(theme: Theme) {
        this.theme = theme
        digitView.setTextColor(theme.keyTextColor)
        pinyinView.setTextColor(theme.altKeyTextColor)
        speculativeView.setTextColor(theme.altKeyTextColor)
        fcitxPreeditView.setTextColor(theme.keyTextColor)
    }

    fun updatePreedit(digits: String, pinyin: String, speculativePaths: List<String>) {
        digitView.text = digits
        pinyinView.text = if (pinyin.isNotEmpty()) " | $pinyin" else ""
        speculativeView.text = if (speculativePaths.isNotEmpty()) {
            " [" + speculativePaths.joinToString(", ") + "]"
        } else ""
    }

    fun updateFcitxPreedit(text: String) {
        fcitxPreeditView.text = if (text.isNotEmpty()) " → $text" else ""
    }

    fun clear() {
        digitView.text = ""
        pinyinView.text = ""
        speculativeView.text = ""
        fcitxPreeditView.text = ""
    }
}