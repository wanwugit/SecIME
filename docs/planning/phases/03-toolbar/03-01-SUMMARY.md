---
plan: 01
phase: 03-toolbar
status: complete
---

## Summary

Created dual-lock state machine and secure preferences namespace — pure logic with no UI dependency.

### Key Files Created
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/input/bar/EncryptionBarStateMachine.kt` — 4 states (UNLOCKED, LOCKED, ENCRYPTING, ENCRYPTED), 7 transition events, BufferEmpty boolean key, EventStateMachine pattern

### Key Files Modified
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/data/prefs/AppPrefs.kt` — Added `Secure` inner class (extends ManagedPreferenceInternal) with 5 string fields: lockState, encryptMode, selectedContactIds, codebookId, bufferContent

### Decisions
- Used `ManagedPreferenceInternal` instead of `ManagedPreferenceCategory` for Secure prefs — these are internal state prefs that don't need a Settings UI entry
- `LockToggled` handles UNLOCKED↔LOCKED bidirectional toggle (encrypt mode)
- `DecryptToggled` handles UNLOCKED→LOCKED and ENCRYPTED→LOCKED (decrypt mode)
- `EncryptRequested` only fires when BufferEmpty is false (buffer must have content)