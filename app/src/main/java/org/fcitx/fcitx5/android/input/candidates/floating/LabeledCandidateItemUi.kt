/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2024 Fcitx5 for Android Contributors
 */

package org.fcitx.fcitx5.android.input.candidates.floating

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import org.fcitx.fcitx5.android.core.FcitxEvent
import org.fcitx.fcitx5.android.data.theme.Theme
import splitties.views.backgroundColor
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.wrapContent
import splitties.views.gravityCenter

class LabeledCandidateItemUi(
    override val ctx: Context,
    val theme: Theme,
    setupTextView: TextView.() -> Unit
) : Ui {

    private val commentView = textView {
        setupTextView(this)
        textSize = textSize * 0.7f
        isSingleLine = true
        gravity = gravityCenter
    }

    private val mainView = textView {
        setupTextView(this)
        isSingleLine = true
        gravity = gravityCenter
    }

    override val root = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        gravity = gravityCenter
        add(commentView, lParams(wrapContent, wrapContent))
        add(mainView, lParams(wrapContent, wrapContent))
    }

    fun update(candidate: FcitxEvent.Candidate, active: Boolean) {
        val fg = if (active) theme.genericActiveForegroundColor else theme.candidateTextColor
        val altFg = if (active) theme.genericActiveForegroundColor else theme.candidateCommentColor
        mainView.setTextColor(fg)
        mainView.text = candidate.text
        if (candidate.comment.isNotBlank()) {
            commentView.visibility = LinearLayout.VISIBLE
            commentView.setTextColor(altFg)
            commentView.text = candidate.comment
        } else {
            commentView.visibility = LinearLayout.GONE
        }
        val bg = if (active) theme.genericActiveBackgroundColor else Color.TRANSPARENT
        root.backgroundColor = bg
    }
}
