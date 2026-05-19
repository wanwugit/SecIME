package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.picker.PickerWindow

@SuppressLint("ViewConstructor")
class T9Keyboard(
    context: Context,
    theme: Theme
) : BaseKeyboard(context, theme, Layout) {

    companion object {
        const val Name = "T9"

        // Rows 1-3 key widths
        private const val W_SYM = 0.15f
        private const val W_T9 = 0.22f
        private const val W_FUNC = 0.19f

        // Row 4 key widths
        private const val W_FUNC_B = 0.15f
        private const val W_SPACE = 0.40f

        val Layout: List<List<KeyDef>> = listOf(
            // Row 1: ， 1/分词 2ABC 3DEF ⌫
            listOf(
                T9PunctuationKey("，", percentWidth = W_SYM),
                KeyDef(
                    KeyDef.Appearance.AltText(
                        displayText = "1",
                        altText = "分词",
                        textSize = 23f,
                        percentWidth = W_T9
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.T9SegmentAction)),
                    arrayOf(KeyDef.Popup.AltPreview("1", "分词"))
                ),
                T9DigitKey("2", "ABC", percentWidth = W_T9),
                T9DigitKey("3", "DEF", percentWidth = W_T9),
                KeyDef(
                    KeyDef.Appearance.Image(
                        src = R.drawable.ic_baseline_backspace_24,
                        percentWidth = W_FUNC,
                        variant = KeyDef.Appearance.Variant.Alternative,
                        viewId = R.id.button_backspace
                    ),
                    setOf(
                        KeyDef.Behavior.Press(KeyAction.T9BackspaceAction),
                        KeyDef.Behavior.Repeat(KeyAction.T9BackspaceAction)
                    )
                ),
            ),
            // Row 2: 。 4GHI 5JKL 6MNO 重输
            listOf(
                T9PunctuationKey("。", percentWidth = W_SYM),
                T9DigitKey("4", "GHI", percentWidth = W_T9),
                T9DigitKey("5", "JKL", percentWidth = W_T9),
                T9DigitKey("6", "MNO", percentWidth = W_T9),
                KeyDef(
                    KeyDef.Appearance.Text(
                        displayText = "重输",
                        textSize = 16f,
                        percentWidth = W_FUNC,
                        variant = KeyDef.Appearance.Variant.Alternative
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.T9ResetAction))
                ),
            ),
            // Row 3: ？ 7PQRS 8TUV 9WXYZ 0
            listOf(
                T9PunctuationKey("？", percentWidth = W_SYM),
                T9DigitKey("7", "PQRS", percentWidth = W_T9),
                T9DigitKey("8", "TUV", percentWidth = W_T9),
                T9DigitKey("9", "WXYZ", percentWidth = W_T9),
                KeyDef(
                    KeyDef.Appearance.AltText(
                        displayText = "0",
                        altText = " ",
                        textSize = 23f,
                        percentWidth = W_FUNC
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.T9ZeroAction)),
                    arrayOf(KeyDef.Popup.AltPreview("0", " "))
                ),
            ),
            // Row 4: 符 123 空格 中/英 ↵
            listOf(
                KeyDef(
                    KeyDef.Appearance.Text(
                        displayText = "符",
                        textSize = 16f,
                        percentWidth = W_FUNC_B,
                        variant = KeyDef.Appearance.Variant.Alternative
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.PickerSwitchAction(PickerWindow.Key.Symbol)))
                ),
                KeyDef(
                    KeyDef.Appearance.Text(
                        displayText = "123",
                        textSize = 16f,
                        percentWidth = W_FUNC_B,
                        variant = KeyDef.Appearance.Variant.Alternative
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.LayoutSwitchAction(NumberKeyboard.Name)))
                ),
                KeyDef(
                    KeyDef.Appearance.Text(
                        displayText = "空格",
                        textSize = 18f,
                        percentWidth = W_SPACE,
                        border = KeyDef.Appearance.Border.Special,
                        viewId = R.id.button_space
                    ),
                    setOf(
                        KeyDef.Behavior.Press(KeyAction.T9SpaceAction),
                        KeyDef.Behavior.LongPress(KeyAction.SpaceLongPressAction)
                    )
                ),
                KeyDef(
                    KeyDef.Appearance.Image(
                        src = R.drawable.ic_baseline_language_24,
                        percentWidth = W_FUNC_B,
                        variant = KeyDef.Appearance.Variant.AltForeground,
                        viewId = R.id.button_lang
                    ),
                    setOf(
                        KeyDef.Behavior.Press(KeyAction.LangSwitchAction),
                        KeyDef.Behavior.LongPress(KeyAction.ShowInputMethodPickerAction)
                    )
                ),
                KeyDef(
                    KeyDef.Appearance.Image(
                        src = R.drawable.ic_baseline_keyboard_return_24,
                        percentWidth = W_FUNC_B,
                        variant = KeyDef.Appearance.Variant.Accent,
                        border = KeyDef.Appearance.Border.Special,
                        viewId = R.id.button_return
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.T9EnterAction))
                ),
            ),
        )
    }
}