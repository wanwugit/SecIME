package org.fcitx.fcitx5.android.input.keyboard

class T9DigitKey(
    val digit: String,
    val letters: String,
    percentWidth: Float = 0.25f,
    behaviors: Set<KeyDef.Behavior> = setOf(KeyDef.Behavior.Press(KeyAction.T9DigitAction(digit))),
    popup: Array<KeyDef.Popup>? = arrayOf(
        KeyDef.Popup.AltPreview(digit, letters),
        KeyDef.Popup.Keyboard(digit)
    )
) : KeyDef(
    KeyDef.Appearance.AltText(
        displayText = digit,
        altText = letters,
        textSize = 23f,
        percentWidth = percentWidth
    ),
    behaviors,
    popup
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

class T9PunctuationPanelKey(
    val symbols: List<String> = listOf(
        "，", "。", "？", "！", "；", "：", "…", "——",
        "、", "～", "（）", "《》", "【】", "''", "\"\"", "·",
        "@", "#", "*", "^", "|", "\\", "/", "~",
        "=", "+", "-", "_", "<", ">", "￥", "$",
        "℃", "‰", "°", "※", "→", "←", "↑", "↓"
    ),
    val visibleCount: Int = 4,
    percentWidth: Float = 0.15f,
    rowSpan: Int = 3
) : KeyDef(
    KeyDef.Appearance.Text(
        displayText = "，",
        textSize = 18f,
        percentWidth = percentWidth,
        variant = KeyDef.Appearance.Variant.Alternative
    ),
    emptySet(),
    rowSpan = rowSpan
)

class SpacerKey(
    percentWidth: Float = 0.15f
) : KeyDef(
    KeyDef.Appearance.Text(
        displayText = "",
        textSize = 0f,
        percentWidth = percentWidth,
        variant = KeyDef.Appearance.Variant.Normal
    ),
    emptySet()
)