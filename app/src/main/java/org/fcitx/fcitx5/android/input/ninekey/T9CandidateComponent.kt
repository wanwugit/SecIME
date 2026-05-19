package org.fcitx.fcitx5.android.input.ninekey

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import org.fcitx.fcitx5.android.data.theme.Theme

class T9CandidateComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : HorizontalScrollView(context, attrs, defStyle) {

    private val container: LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
    }

    private var candidates: List<String> = emptyList()
    var onCandidateSelected: ((Int) -> Unit)? = null

    private var theme: Theme? = null

    init {
        addView(container)
    }

    fun setTheme(theme: Theme) {
        this.theme = theme
    }

    fun updateCandidatesFromFcitx(candidatesArray: Array<String>) {
        candidates = candidatesArray.toList()
        rebuildViews()
    }

    private fun rebuildViews() {
        container.removeAllViews()
        val primaryColor = theme?.keyTextColor ?: 0xFF000000.toInt()
        val secondaryColor = theme?.altKeyTextColor ?: 0xFF888888.toInt()
        candidates.forEachIndexed { index, text ->
            val textView = TextView(context).apply {
                this.text = text
                setTextColor(primaryColor)
                setPadding(16, 8, 16, 8)
                textSize = 16f
                setOnClickListener { onCandidateSelected?.invoke(index) }
            }
            container.addView(textView)
        }
    }

    fun clear() {
        candidates = emptyList()
        container.removeAllViews()
    }
}