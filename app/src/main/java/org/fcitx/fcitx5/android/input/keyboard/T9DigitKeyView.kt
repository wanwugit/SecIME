package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.AutoScaleTextView
import splitties.dimensions.dp
import splitties.views.dsl.constraintlayout.lParams
import splitties.views.dsl.constraintlayout.parentId
import splitties.views.dsl.core.add
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent

@SuppressLint("ViewConstructor")
class T9DigitKeyView(ctx: Context, theme: Theme, def: KeyDef.Appearance.AltText) :
    KeyView(ctx, theme, def) {

    val lettersText = view(::AutoScaleTextView) {
        isClickable = false
        isFocusable = false
        background = null
        text = def.altText
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
        setTypeface(typeface, Typeface.NORMAL)
        textDirection = View.TEXT_DIRECTION_FIRST_STRONG_LTR
        setTextColor(theme.keyTextColor)
    }

    val digitText = view(::AutoScaleTextView) {
        isClickable = false
        isFocusable = false
        background = null
        text = def.displayText
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
        setTypeface(typeface, Typeface.NORMAL)
        setTextColor(theme.altKeyTextColor)
        alpha = 0.6f
    }

    init {
        appearanceView.apply {
            add(lettersText, lParams(wrapContent, wrapContent) {
                topToTop = parentId
                bottomToBottom = parentId
                leftToLeft = parentId
                rightToRight = parentId
            })
            add(digitText, lParams(wrapContent, wrapContent) {
                topToTop = parentId
                topMargin = vMargin + dp(2)
                rightToRight = parentId
                rightMargin = hMargin + dp(4)
            })
        }
    }
}