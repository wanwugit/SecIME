/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.bar.ui

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import org.fcitx.fcitx5.android.data.theme.Theme
import splitties.dimensions.dp
import splitties.views.dsl.core.Ui

class EncryptionCandidateUi(
    override val ctx: Context,
    private val theme: Theme
) : Ui {

    val bufferBar = BufferBarUi(ctx, theme)
    val slotBar = SlotBarUi(ctx)

    override val root = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        val bufferLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ctx.dp(24))
        addView(bufferBar.root, bufferLp)
        val slotLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ctx.dp(16))
        addView(slotBar.root, slotLp)
    }

    fun setEncryptMode() = bufferBar.setEncryptMode()
    fun setDecryptMode() = bufferBar.setDecryptMode()
    fun updateContent(text: String) = bufferBar.updateContent(text)
    fun clearContent() = bufferBar.clearContent()
    fun setSlotState(index: Int, state: SlotBarUi.SlotState, label: String = "") =
        slotBar.setSlotState(index, state, label)
}