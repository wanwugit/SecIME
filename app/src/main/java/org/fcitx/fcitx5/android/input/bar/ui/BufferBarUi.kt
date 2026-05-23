/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.bar.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.ImageView
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.theme.Theme
import splitties.dimensions.dp
import splitties.views.dsl.constraintlayout.centerVertically
import splitties.views.dsl.constraintlayout.constraintLayout
import splitties.views.dsl.constraintlayout.endOfParent
import splitties.views.dsl.constraintlayout.lParams
import splitties.views.dsl.constraintlayout.matchConstraints
import splitties.views.dsl.constraintlayout.startToEndOf
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.wrapContent
import splitties.views.imageResource
import splitties.views.padding

class BufferBarUi(override val ctx: Context, private val theme: Theme) : Ui {

    private val borderColorEncrypt = Color.parseColor("#7C4DFF")
    private val bgColorEncrypt = Color.parseColor("#F3E5F5")
    private val textColorEncrypt = Color.parseColor("#7C4DFF")

    private val borderColorDecrypt = Color.parseColor("#4CAF50")
    private val bgColorDecrypt = Color.WHITE
    private val textColorDecrypt = Color.parseColor("#4CAF50")

    private val borderDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = ctx.dp(4).toFloat()
        setStroke(ctx.dp(1), borderColorEncrypt)
        setColor(bgColorEncrypt)
    }

    val lockIcon = imageView {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        padding = ctx.dp(4)
        imageResource = R.drawable.ic_encrypt_lock_24
        setColorFilter(borderColorEncrypt)
    }

    val previewText = textView {
        textSize = 13f
        setTextColor(textColorEncrypt)
        setSingleLine(true)
        ellipsize = android.text.TextUtils.TruncateAt.END
    }

    val charCount = textView {
        textSize = 11f
        setTextColor(textColorEncrypt)
        setGravity(Gravity.CENTER)
    }

    val deleteButton = imageView {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        padding = ctx.dp(4)
        imageResource = R.drawable.ic_baseline_delete_24
        setColorFilter(textColorEncrypt)
    }

    override val root = ctx.constraintLayout {
        background = borderDrawable
        setPadding(ctx.dp(4), ctx.dp(4), ctx.dp(4), ctx.dp(4))

        add(lockIcon, lParams(ctx.dp(20), ctx.dp(20)) {
            centerVertically()
        })
        add(previewText, lParams(matchConstraints, ctx.dp(20)) {
            startToEndOf(lockIcon)
            centerVertically()
        })
        add(charCount, lParams(wrapContent, ctx.dp(20)) {
            startToEndOf(previewText)
            centerVertically()
        })
        add(deleteButton, lParams(ctx.dp(20), ctx.dp(20)) {
            startToEndOf(charCount)
            endOfParent()
            centerVertically()
        })
    }

    fun setEncryptMode() {
        lockIcon.imageResource = R.drawable.ic_encrypt_lock_24
        lockIcon.setColorFilter(borderColorEncrypt)
        previewText.setTextColor(textColorEncrypt)
        charCount.setTextColor(textColorEncrypt)
        deleteButton.setColorFilter(textColorEncrypt)
        borderDrawable.setStroke(ctx.dp(1), borderColorEncrypt)
        borderDrawable.setColor(bgColorEncrypt)
    }

    fun setDecryptMode() {
        lockIcon.imageResource = R.drawable.ic_decrypt_lock_24
        lockIcon.setColorFilter(borderColorDecrypt)
        previewText.setTextColor(textColorDecrypt)
        charCount.setTextColor(textColorDecrypt)
        deleteButton.setColorFilter(textColorDecrypt)
        borderDrawable.setStroke(ctx.dp(1), borderColorDecrypt)
        borderDrawable.setColor(bgColorDecrypt)
    }

    fun updateContent(text: String) {
        previewText.text = text
        charCount.text = "${text.length}"
    }

    fun clearContent() {
        previewText.text = ""
        charCount.text = "0"
    }
}