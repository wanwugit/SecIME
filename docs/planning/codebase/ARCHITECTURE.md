# Architecture

## Overview

The app is an Android Input Method Editor (IME) based on fcitx5-android, extended with a secureime module for encryption. The architecture follows a **component-based pattern** with dependency injection via `org.mechdancer:dependency`, and uses **StateFlow pipelines** for reactive data propagation.

## Component System

All major UI/logic units are `UniqueComponent` implementations that resolve dependencies via `by manager.must<>()`:

```
InputWindowManager (hosts all components)
├── KeyboardWindow (EssentialWindow, InputBroadcastReceiver)
│   ├── CommonKeyActionListener
│   ├── InputDecisionBus
│   ├── LanguageAdapterComponent
│   ├── PopupComponent
│   ├── KawaiiBarComponent
│   └── ReturnKeyDrawableComponent
├── T9Keyboard / TextKeyboard (keyboard layouts)
└── Other windows (PickerWindow, ClipboardWindow, etc.)
```

## Core Data Flow

### Input → Output (Key Press to Text)
```
User key press
  → T9Keyboard / TextKeyboard (KeyAction dispatch)
  → InputDecisionBus (routes by action type)
  → LanguageAdapter.sendKey() / sendKeySym()
  → FcitxLanguageAdapter (JNI bridge)
  → fcitx5 daemon (native process)
  → Rime t9 speller algebra (T9 digit→pinyin→hanzi)
  → FcitxEvent.Candidate / FcitxEvent.Preedit (callbacks)
  → CandidatePipeline / PreeditPipeline (StateFlow publish)
  → UI observes StateFlow → update candidates/preedit
```

### T9 Input Model
- T9 decoding is **entirely handled by Rime** (`rime_frost_t9` schema)
- Frontend does NOT do beam search, pinyin trie, or any decoding
- `InputDecisionBus` maintains syllable state:
  - `confirmedSyllables` — confirmed pinyin syllables (e.g., ["ni", "he"])
  - `confirmedDigitGroups` — parallel digit groups for backspace
  - `pendingDigits` — current unconfirmed digit sequence
  - `rebuildInput()` — resets Rime and replays confirmed syllables + pending digits

## InputDecisionBus (Lightweight Router)

The Bus is NOT an orchestration engine. It's a pure routing layer:
- **T9 routing**: `onDigitPress` → `sendKey(digit)` → fcitx daemon
- **QWERTY routing**: `onQwertyKey` → `sendKey` → fcitx daemon
- **Candidate/Preedit**: `onFcitxCandidateEvent`/`onFcitxPreeditEvent` → Pipeline.publish
- **Control**: `onLangSwitchEnumerate`, `onCommitText`, `onReset`

## Pipeline Architecture (secureime module)

Two StateFlow pipelines in `org.secureime.sect9.bus`:
- **CandidatePipeline**: `StateFlow<CandidateState>` — candidates list, comments, total count
- **PreeditPipeline**: `StateFlow<PreeditState>` — rawInput, decodedDisplay, auxiliary, fcitxPreedit

## Keyboard Mode Management

`KeyboardWindow` manages `KeyboardMode` enum (T9, QWERTY):
- `setKeyboardMode()` is the single entry point for mode changes
- T9 mode has `allowQwertyInT9` gate for Chinese/English switching
- Switching calls `activateT9Schema`/`activateQwertySchema` on LanguageAdapter

### Language Switch Flow (T9 mode)
```
LangSwitchAction
  → CommonKeyActionListener
  → enumerateIme (async IME switch)
  → onImeUpdate callback
  → switchLayout (T9 ↔ QWERTY based on IME language)
```

## LanguageAdapter Interface

`org.secureime.sect9.language.LanguageAdapter` — suspend interface:
- `sendKey`, `sendKeySym`, `select`, `reset`, `commitText`
- `offsetCandidatePage`, `enumerateIme`, `toggleIme`
- `triggerQuickPhrase`, `triggerUnicode`, `finishComposing`
- `commitCurrentPreedit`, `ensureChineseIme`
- `activateT9Schema`, `activateQwertySchema`

Implementation: `FcitxLanguageAdapter` in `app/.../input/bus/`

## Entry Points

1. **FcitxInputMethodService** — main IME service (`LifecycleInputMethodService`)
2. **FcitxApplication** — application class
3. **MainActivity** — setup/settings UI
4. **Native entry**: `native-lib.cpp` → JNI `System.loadLibrary("native-lib")`

## Native Architecture

```
app/src/main/cpp/
├── CMakeLists.txt (root)
├── androidaddonloader/ — dynamic addon loading
├── androidfrontend/ — input context management
├── androidkeyboard/ — keyboard event dispatch
├── androidnotification/ — notification bridge
├── fcitx5-rime/ — Rime addon for fcitx5
│   ├── rimeengine.cpp/h
│   ├── rimecandidate.cpp/h
│   ├── rimeservice.cpp/h
│   └── rimefactory.cpp/h
└── po/ — translation files
```

---
*Last updated: 2026-05-23 after codebase scan*