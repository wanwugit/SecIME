# Structure

## Top-Level Layout

```
D:\Proj\Sec input method\
├── fcitx5-android/           ← Main Android project
│   ├── app/                  ← Application module
│   ├── build-logic/          ← Gradle convention plugins
│   ├── codegen/              ← KSP code generator
│   ├── gradle/               ← Version catalog (libs.versions.toml)
│   ├── lib/                  ← Library modules
│   │   ├── common/           ← Shared utilities
│   │   ├── fcitx5/           ← Fcitx5 JNI wrapper
│   │   ├── fcitx5-chinese-addons/ ← Chinese input addons
│   │   ├── fcitx5-lua/       ← Lua scripting support
│   │   ├── libime/           ← LibIME integration
│   │   ├── plugin-base/      ← Plugin API base
│   │   └── rime-bridge/      ← Rime Kotlin bridge
│   └── plugin/               ← Optional input method plugins
│       ├── rime/             ← Rime plugin
│       ├── anthy/            ← Japanese
│       ├── chewing/          ← Traditional Chinese
│       ├── hangul/           ← Korean
│       └── ...               ← Other IME plugins
├── secureime/                ← Encryption module (composite build)
│   └── src/main/kotlin/org/secureime/sect9/
│       ├── bus/              ← CandidatePipeline, PreeditPipeline
│       └── language/         ← LanguageAdapter interface
├── librime/                  ← Prebuilt librime binaries
├── rime-frost/               ← Rime dictionary source files
├── rime-wanxiang-wanxiang/   ← Additional Rime schemas
├── Back/                     ← Backup/old versions
├── SPECIFICATION.md          ← Encryption feature spec
├── Me21.txt                  ← Conversation history notes
└── ui_dump.xml               ← UI hierarchy dump
```

## App Package Structure

`fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/`

| Package | Purpose |
|---------|---------|
| `core/` | Fcitx5 core API, events, key mapping, lifecycle |
| `core/data/` | Data descriptors, file actions, data hierarchy |
| `daemon/` | Fcitx5 daemon connection, lifecycle management |
| `data/clipboard/` | Clipboard manager + Room DB |
| `data/prefs/` | SharedPreferences management (AppPrefs) |
| `data/punctuation/` | Punctuation mapping |
| `data/quickphrase/` | Quick phrase management |
| `data/table/` | Table-based input method dictionaries |
| `data/theme/` | Theme management and serialization |
| `ime/` | IME service (currently empty, service in input/) |
| `input/` | Main input method UI and logic |
| `input/bar/` | Keyboard bar (KawaiiBarComponent) |
| `input/broadcast/` | Broadcast receivers for input events |
| `input/bus/` | InputDecisionBus, FcitxLanguageAdapter, pipelines |
| `input/candidates/` | Candidate display (horizontal/vertical) |
| `input/clipboard/` | Clipboard window UI |
| `input/cursor/` | Cursor control |
| `input/dependency/` | DI helper extensions (fcitx, theme, service) |
| `input/dialog/` | Dialog components |
| `input/editing/` | Text editing window |
| `input/editorinfo/` | Editor info handling |
| `input/keyboard/` | Keyboard layouts, key actions, mode management |
| `input/ninekey/` | T9-specific candidate/preedit components |
| `input/picker/` | Picker windows (emoji, symbol) |
| `input/popup/` | Popup key actions |
| `input/preedit/` | Preedit display |
| `input/status/` | Status area window |
| `input/wm/` | Window manager for input windows |
| `provider/` | Content providers |
| `ui/common/` | Shared UI components |
| `ui/main/` | Main activity, settings, setup |
| `ui/setup/` │ First-run setup wizard |
| `utils/` | Utility classes |
| `utils/config/` | Configuration utilities |

## Key Files

| File | Purpose |
|------|---------|
| `input/FcitxInputMethodService.kt` | Main IME service entry point |
| `input/bus/InputDecisionBus.kt` | Central input routing |
| `input/bus/FcitxLanguageAdapter.kt` | LanguageAdapter implementation |
| `input/keyboard/KeyboardWindow.kt` | Keyboard mode management (T9/QWERTY) |
| `input/keyboard/T9Keyboard.kt` | T9 keyboard layout |
| `input/keyboard/TextKeyboard.kt` | QWERTY keyboard layout |
| `input/keyboard/KeyAction.kt` | Key action sealed classes |
| `input/keyboard/T9DefPreset.kt` | T9 key definitions |
| `input/keyboard/CommonKeyActionListener.kt` | Shared key action handler |
| `input/keyboard/LangSwitchBehavior.kt` | Language switch logic |
| `input/AutoScaleTextView.kt` | Custom TextView with span support |
| `input/SecLogger.kt` | Logging utility |
| `core/Fcitx.kt` | Fcitx5 JNI API wrapper |
| `core/FcitxEvent.kt` | Event sealed classes |
| `daemon/FcitxDaemon.kt` | Daemon lifecycle |

## Native Code Locations

| Path | Purpose |
|------|---------|
| `app/src/main/cpp/CMakeLists.txt` | Root native build config |
| `app/src/main/cpp/androidfrontend/` | Input context management |
| `app/src/main/cpp/androidkeyboard/` | Keyboard event handling |
| `app/src/main/cpp/fcitx5-rime/src/` | Rime engine addon |
| `lib/rime-bridge/src/main/kotlin/` | Kotlin Rime bridge |

## Build Config Locations

| Path | Purpose |
|------|---------|
| `fcitx5-android/build.gradle.kts` | Root build config |
| `fcitx5-android/app/build.gradle.kts` | App module config |
| `fcitx5-android/gradle/libs.versions.toml` | Version catalog |
| `fcitx5-android/build-logic/convention/` | Convention plugins |
| `secureime/build.gradle.kts` | Secureime module config |
| `secureime/settings.gradle.kts` | Secureime settings |

## Resource Locations

| Path | Purpose |
|------|---------|
| `app/src/main/res/xml/input_method.xml` | IME service declaration |
| `app/src/main/res/xml/` | Preference screens, backup rules |
| `app/src/main/res/layout/` | Layout XML (minimal, mostly programmatic UI) |

---
*Last updated: 2026-05-23 after codebase scan*