/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.bar.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import splitties.dimensions.dp
import splitties.views.dsl.core.Ui
import splitties.views.padding

class SlotBarUi(override val ctx: Context) : Ui {

    enum class SlotState {
        Empty, Idle, Active
    }

    private val emptyBorderColor = Color.parseColor("#808080")
    private val activeBorderColor = Color.parseColor("#7C4DFF")
    private val activeTextColor = Color.parseColor("#7C4DFF")

    var onSlotClick: ((index: Int) -> Unit)? = null

    private fun slotDrawable(state: SlotState) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = ctx.dp(2).toFloat()
        when (state) {
            SlotState.Empty -> {
                setStroke(ctx.dp(1), emptyBorderColor, ctx.dp(4).toFloat(), ctx.dp(2).toFloat())
                setColor(Color.TRANSPARENT)
            }
            SlotState.Idle -> {
                setStroke(ctx.dp(1), emptyBorderColor)
                setColor(Color.parseColor("#F5F5F5"))
            }
            SlotState.Active -> {
                setStroke(ctx.dp(1), activeBorderColor)
                setColor(Color.parseColor("#EDE7F6"))
            }
        }
    }

    private val slots = (0 until 4).map { index ->
        TextView(ctx).apply {
            background = slotDrawable(SlotState.Empty)
            text = "+"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            gravity = Gravity.CENTER
            setTextColor(emptyBorderColor)
            padding = ctx.dp(2)
            setOnClickListener { onSlotClick?.invoke(index) }
        }
    }

    override val root = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        weightSum = 4f
        slots.forEachIndexed { index, slot ->
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            if (index > 0) lp.marginStart = ctx.dp(2)
            if (index < 3) lp.marginEnd = ctx.dp(2)
            addView(slot, lp)
        }
    }

    fun setSlotState(index: Int, state: SlotState, label: String = "") {
        if (index !in 0..3) return
        val slot = slots[index]
        slot.background = slotDrawable(state)
        when (state) {
            SlotState.Empty -> {
                slot.text = "+"
                slot.setTextColor(emptyBorderColor)
            }
            SlotState.Idle -> {
                slot.text = label.ifEmpty { "+" }
                slot.setTextColor(emptyBorderColor)
            }
            SlotState.Active -> {
                slot.text = label
                slot.setTextColor(activeTextColor)
            }
        }
    }
}