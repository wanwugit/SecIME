# Concerns

## Critical: Test Coverage Gap
- Only 3 test files in the entire project (2 unit, 1 instrumented)
- No tests for InputDecisionBus, T9 input model, keyboard routing
- Adding encryption features without test infrastructure risks regression bugs
- **Recommendation**: Establish test foundation before building encryption features

## Security Concerns (from SPECIFICATION.md)

### Key Storage
- SM9 private keys must be stored locally with password protection — implementation not started
- Private key must never leave the device — need strict enforcement in code
- Key file secure storage is listed as "待实现" (to be implemented)

### Clipboard Handling
- Clipboard auto-decrypt requires reading clipboard content on every change
- Android clipboard access has privacy implications
- Hash fingerprint dedup prevents "回魂" (repeat decrypt) but needs careful implementation
- Clipboard content may contain sensitive data that should not persist

### Encryption Implementation
- GmSSL integration (v3.1.1) has known build issues: main branch has `kyber.h` compilation bug
- Must pin to v3.1.1 release, not main branch
- Native library loading order critical: `System.loadLibrary("gmssl")` BEFORE `System.loadLibrary("gmssljni")`
- NativeLoader needs Android-specific adaptation (skip `config.properties`, use `System.loadLibrary` per ABI)

### IME Environment Constraints
- Input method processes are low-priority, subject to Android low-memory kills
- Encryption state must survive process restart (SharedPreferences persistence)
- Spec requires: encryption lock state, mode, selected contacts, template ID all persist across restarts
- Keyboard crash should not affect system stability (spec constraint)

## Performance Concerns
- Spec requires encryption latency < 100ms
- SM9 encryption is computationally expensive — needs benchmarking
- Native crypto operations must not block UI thread (coroutine + JNI threading)
- Base58 encode/decode on large payloads could be slow
- Template wrapping/unwrapping adds overhead to each encrypt/decrypt cycle

## Fragile Areas

### T9 Input State Machine
- `InputDecisionBus` manages complex syllable state (confirmedSyllables, confirmedDigitGroups, pendingDigits)
- `rebuildInput()` resets Rime and replays — timing-sensitive, async IME switching
- Backspace handling across syllable boundaries is edge-case-heavy

### IME Language Switch Timing
- `LangSwitchAction` → `enumerateIme` (async) → `onImeUpdate` callback → `switchLayout`
- IME switch is async, language key text updates after keyboard switch
- Race condition potential between IME switch and keyboard layout change

### AutoScaleTextView Span Rendering
- Dual rendering path (custom draw vs. super.draw) based on `hasSpans` flag
- Span text measurement relies on `super.onMeasure()` — must be correctly synced
- If spans are set incorrectly, rendering falls back to pure string (invisible spans)

## Technical Debt
- `Back/` directory contains old code versions (v1-dlsym-bridge) — should be archived
- Deprecated zero-width character protocol headers still need recognition code
- `ui_dump.xml` and `Me21.txt` are development artifacts, not project files
- Multiple memory notes marked as ⚠️ 过时 (outdated) — significant architectural drift

## Dependency Risks
- NDK version not pinned explicitly — builds may break on NDK updates
- Prebuilt librime in `librime/` — version/update mechanism unclear
- fcitx5-android is a large upstream project — merging upstream changes could conflict with customizations
- `org.mechdancer:dependency` 0.1.2 is a niche DI library — limited community support

## Extension Difficulty (Encryption Feature)
- Adding encryption toolbar to keyboard area requires modifying `KawaiiBarComponent` extensively
- Input intercept pipeline (encrypt before commit) needs new layer between key actions and `commitText`
- Clipboard monitoring + decrypt UI needs new window type in `InputWindowManager`
- Room database for friends/channels/index numbers requires new module or expanding secureime
- GmSSL native build integration requires CMake and NDK cross-compilation setup

---
*Last updated: 2026-05-23 after codebase scan*