# SecIME 当前版本功能记录

## 版本信息

- 基于项目：fcitx5-android (0.1.2-59-gce1ac074)
- 包名：org.fcitx.fcitx5.android.debug
- 架构：x86_64 模拟器

## T9 九宫格键盘

### 跨行标点面板（左侧跨3行键）

- **架构**：T9PunctuationPanelView 为独立 FrameLayout（不继承 KeyView）
- **原因**：KeyView 的两层结构（appearanceView）遮挡自定义绘制，且不适合"可切换内容面板"的复用需求
- **关键修复**：`setWillNotDraw(false)` — FrameLayout 默认不调用 onDraw

**符号模式（当前可用）**：
- 默认显示4个符号，竖排均匀分布有呼吸感
- 下滑滚动露出更多符号，滑到哪组就停在哪组（scrollOffset 不重置）
- 点击符号提交该符号字符（T9CommitAction → onCommitText）
- 符号列表40个，分4组：
  - 第1组：，。？！；：…——
  - 第2组：、《》【】''""·
  - 第3组：@#*^|\\/~=+-_<>￥$
  - 第4组：℃‰°※→←↑↓

**拼音模式（接口预留）**：
- `showPinyin(candidates)` → 切换显示拼音候选
- `showSymbols()` → 切回符号模式
- 点击拼音候选 → T9CandidateSelectAction → onUiCandidateSelect
- 数据流：InputDecisionBus → T9Keyboard → punctPanel.showPinyin()

### 布局对齐

- 列对齐已验证：1/4/7、2/5/8、3/6/9 完全对齐
- span key 在 BaseKeyboard 层级，placeholder（INVISIBLE View, width=0 MATCH_CONSTRAINT）占位保持 Row 1 chain
- span key 约束：leftToLeft + rightToRight + horizontalBias=0 + matchConstraintPercentWidth

### T9 键盘布局

```
Row 1: ┌标点┐ │ 1/分词 │ 2ABC │ 3DEF │ ⌫
Row 2: │跨行│ │ 4GHI  │ 5JKL │ 6MNO │ 重输
Row 3: │键  │ │ 7PQRS │ 8TUV │ 9WXYZ│ 0
Row 4:  符   │ 123    │ ␣    │ 中/英│ ↵
```

宽度参数：
- W_SYM = 0.15f（标点/空格）
- W_T9 = 0.22f（数字键）
- W_FUNC = 0.19f（功能键）
- Row 4: W_R4_SYM=0.15, W_R4_NUM=0.10, W_R4_LANG=0.10, W_R4_ENTER=0.19, space=0.46

### T9 按键行为

| 键 | 行为 |
|---|---|
| 1/分词 | T9SegmentAction（分词） |
| 2ABC-9WXYZ | T9DigitAction（输入数字对应拼音） |
| ⌫ | T9BackspaceAction（支持长按重复） |
| 重输 | T9ResetAction（重置输入） |
| 0 | T9ZeroAction |
| 符 | PickerSwitchAction(Symbol) |
| 123 | LayoutSwitchAction(NumberKeyboard) |
| ␣ | T9SpaceAction / SpaceLongPressAction |
| 中/英 | LanguageKey（IME切换） |
| ↵ | T9EnterAction |

## QWERTY 键盘

- Row 4 redesign: 符123, ␣, 中/英, ↵
- percentWidth 总计 0.95
- 语言键显示"中"/"EN"，基于 ime.languageCode

## 涉及文件

| 文件 | 改动 |
|---|---|
| T9PunctuationPanelView.kt | 重写为独立 FrameLayout + 自绘 + 双模式 |
| T9DefPreset.kt | 符号列表扩充为40个 |
| BaseKeyboard.kt | createKeyView 返回 View，rowSpan placeholder 逻辑修复 |
| T9Keyboard.kt | punctPanel 引用，Layout 定义 |
| KeyDef.kt | rowSpan 参数 |
| KeyAction.kt | T9CommitAction, T9CandidateSelectAction 等 |

## 已修复的 Bug

1. **StackOverflowError**：onAction 递归 → `this@BaseKeyboard.onAction(action)`
2. **placeholder 宽度**：width 必须为 0 (MATCH_CONSTRAINT)，否则 matchConstraintPercentWidth 不生效
3. **FrameLayout 不绘制**：setWillNotDraw(false) 启用自定义 onDraw
4. **scrollOffset 重置**：松手后不应重置滚动位置，滑到哪组就停在哪组