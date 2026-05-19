package org.fcitx.fcitx5.android.input.keyboard

class T9DigitKey(
    val digit: String,
    val letters: String,
    percentWidth: Float = 0.25f
) : KeyDef(
    KeyDef.Appearance.AltText(
        displayText = digit,
        altText = letters,
        textSize = 23f,
        percentWidth = percentWidth
    ),
    setOf(
        KeyDef.Behavior.Press(KeyAction.T9DigitAction(digit))
    ),
    arrayOf(
        KeyDef.Popup.AltPreview(digit, letters),
        KeyDef.Popup.Keyboard(digit)
    )
)

class T9PunctuationKey(
    val punctuation: String,
    percentWidth: Float = 0.15f,
    variant: KeyDef.Appearance.Variant = KeyDef.Appearance.Variant.Alternative
) : KeyDef(
    KeyDef.Appearance.Text(
        displayText = punctuation,
        textSize = 23f,
        percentWidth = percentWidth,
        variant = variant
    ),
    setOf(
        KeyDef.Behavior.Press(KeyAction.T9CommitAction(punctuation))
    )
)