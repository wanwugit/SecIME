package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.data.InputFeedbacks
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.SecLogger
import org.fcitx.fcitx5.android.input.picker.PickerWindow
import androidx.core.view.children

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

        // Row 4 key widths (local to T9, does not affect other keyboards)
        private const val W_R4_SYM = 0.15f
        private const val W_R4_NUM = 0.10f
        private const val W_R4_LANG = 0.10f
        private const val W_R4_ENTER = 0.19f

        val Layout: List<List<KeyDef>> = listOf(
            // Row 1: PunctuationPanel(rowSpan=3) + 1/分词 + 2ABC + 3DEF + ⌫
            listOf(
                T9PunctuationPanelKey(rowSpan = 3),
                T9DigitKey("1", "分词", percentWidth = W_T9,
                    behaviors = setOf(KeyDef.Behavior.Press(KeyAction.T9SegmentAction))),
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
            // Row 2: Spacer + 4GHI + 5JKL + 6MNO + 重输
            listOf(
                SpacerKey(W_SYM),
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
            // Row 3: Spacer + 7PQRS + 8TUV + 9WXYZ + 0
            listOf(
                SpacerKey(W_SYM),
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
                    arrayOf(KeyDef.Popup.AltPreview("0", " "), KeyDef.Popup.Keyboard("0"))
                ),
            ),
            // Row 4: 符 123 ␣ 中/英 ↵
            listOf(
                KeyDef(
                    KeyDef.Appearance.Text(
                        displayText = "符",
                        textSize = 16f,
                        textStyle = Typeface.BOLD,
                        percentWidth = W_R4_SYM,
                        variant = KeyDef.Appearance.Variant.Alternative
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.PickerSwitchAction(PickerWindow.Key.Symbol)))
                ),
                KeyDef(
                    KeyDef.Appearance.Text(
                        displayText = "123",
                        textSize = 16f,
                        textStyle = Typeface.BOLD,
                        percentWidth = W_R4_NUM,
                        variant = KeyDef.Appearance.Variant.Alternative
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.LayoutSwitchAction(NumberKeyboard.Name)))
                ),
                KeyDef(
                    KeyDef.Appearance.Image(
                        src = R.drawable.ic_baseline_space_bar_24,
                        percentWidth = 0.46f,
                        variant = KeyDef.Appearance.Variant.Alternative,
                        border = KeyDef.Appearance.Border.Special,
                        viewId = R.id.button_space,
                        soundEffect = InputFeedbacks.SoundEffect.SpaceBar
                    ),
                    setOf(
                        KeyDef.Behavior.Press(KeyAction.T9SpaceAction),
                        KeyDef.Behavior.LongPress(KeyAction.SpaceLongPressAction)
                    )
                ),
                LanguageKey(),
                KeyDef(
                    KeyDef.Appearance.Image(
                        src = R.drawable.ic_baseline_keyboard_return_24,
                        percentWidth = W_R4_ENTER,
                        variant = KeyDef.Appearance.Variant.Accent,
                        border = KeyDef.Appearance.Border.Special,
                        viewId = R.id.button_return
                    ),
                    setOf(KeyDef.Behavior.Press(KeyAction.T9EnterAction))
                ),
            ),
        )
    }

    val lang: TextKeyView by lazy { findViewById(R.id.button_lang) }

    val punctPanel: T9PunctuationPanelView by lazy {
        children.toList().filterIsInstance<T9PunctuationPanelView>().first()
    }

    override fun onInputMethodUpdate(ime: InputMethodEntry) {
        val isChinese = ime.languageCode.startsWith("zh")
        SecLogger.d("T9Keyboard", "onInputMethodUpdate: languageCode='${ime.languageCode}' isChinese=$isChinese")
        lang.mainText.text = if (isChinese) "中" else "EN"
    }
}