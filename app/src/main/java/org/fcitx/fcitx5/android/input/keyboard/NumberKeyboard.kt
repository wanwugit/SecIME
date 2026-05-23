/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.FcitxKeyMapping
import org.fcitx.fcitx5.android.core.KeySym
import org.fcitx.fcitx5.android.data.InputFeedbacks
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.picker.PickerWindow
import org.fcitx.fcitx5.android.input.popup.PopupAction
import splitties.views.imageResource

@SuppressLint("ViewConstructor")
class NumberKeyboard(
    context: Context,
    theme: Theme,
) : BaseKeyboard(context, theme, Layout) {

    companion object {
        const val Name = "Number"

        private fun digitKey(digit: String, textSize: Float = 22f, percentWidth: Float = 0f,
                variant: KeyDef.Appearance.Variant = KeyDef.Appearance.Variant.Normal) = KeyDef(
            KeyDef.Appearance.Text(
                displayText = digit,
                textSize = textSize,
                percentWidth = percentWidth,
                variant = variant
            ),
            setOf(KeyDef.Behavior.Press(KeyAction.CommitAction(digit)))
        )

        val Layout: List<List<KeyDef>> = listOf(
            listOf(
                NumPadKey("+", 0xffab, 23f, 0.15f, KeyDef.Appearance.Variant.Alternative),
                digitKey("1"),
                digitKey("2"),
                digitKey("3"),
                NumPadKey("/", 0xffaf, 23f, 0.15f, KeyDef.Appearance.Variant.Alternative),
            ),
            listOf(
                NumPadKey("-", 0xffad, 23f, 0.15f, KeyDef.Appearance.Variant.Alternative),
                digitKey("4"),
                digitKey("5"),
                digitKey("6"),
                SymbolKey("#", 0.15f, KeyDef.Appearance.Variant.Alternative),
            ),
            listOf(
                NumPadKey("*", 0xffaa, 23f, 0.15f, KeyDef.Appearance.Variant.Alternative),
                digitKey("7"),
                digitKey("8"),
                digitKey("9"),
                BackspaceKey()
            ),
            listOf(
                TextPickerSwitchKey("符", PickerWindow.Key.Symbol, 0.15f, KeyDef.Appearance.Variant.Alternative),
                KeyDef(
                    KeyDef.Appearance.Text(
                        displayText = "返回",
                        textSize = 16f,
                        textStyle = Typeface.BOLD,
                        percentWidth = 0.233f,
                        variant = KeyDef.Appearance.Variant.Alternative
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.LayoutSwitchAction(TextKeyboard.Name)))
                ),
                digitKey("0", percentWidth = 0.234f),
                KeyDef(
                    KeyDef.Appearance.Image(
                        src = R.drawable.ic_baseline_space_bar_24,
                        percentWidth = 0.233f,
                        variant = KeyDef.Appearance.Variant.Alternative,
                        border = KeyDef.Appearance.Border.Special,
                        viewId = R.id.button_space,
                        soundEffect = InputFeedbacks.SoundEffect.SpaceBar
                    ),
                    setOf(
                        KeyDef.Behavior.Press(KeyAction.SymAction(KeySym(FcitxKeyMapping.FcitxKey_space))),
                        KeyDef.Behavior.LongPress(KeyAction.SpaceLongPressAction)
                    )
                ),
                ReturnKey(percentWidth = 0.15f)
            )
        )
    }

    val backspace: ImageKeyView by lazy { findViewById(R.id.button_backspace) }
    val space: TextKeyView by lazy { findViewById(R.id.button_mini_space) }
    val `return`: ImageKeyView by lazy { findViewById(R.id.button_return) }

    override fun onReturnDrawableUpdate(returnDrawable: Int) {
        `return`.img.imageResource = returnDrawable
    }

    @SuppressLint("MissingSuperCall")
    override fun onPopupAction(action: PopupAction) {
        // leave empty on purpose to disable popup in NumberKeyboard
    }

}
