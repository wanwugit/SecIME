---
plan: 02
phase: 03-toolbar
status: complete
---

## Summary

Built encryption toolbar UI layer: component, Buffer Bar, slot placeholders, and wired into InputView layout.

### Key Files Created
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/input/bar/EncryptionBarComponent.kt` — UniqueViewComponent with state machine, visibility toggle, encrypt/decrypt/send/edit methods, state restoration from SecurePrefs
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/input/bar/ui/BufferBarUi.kt` — Splitties DSL: horizontal constraint layout with lock icon, text preview, char count, delete button; purple encrypt mode / green decrypt mode
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/input/bar/ui/SlotBarUi.kt` — 4 equal-width placeholder slots with dashed border (Empty), solid border (Idle/Active); 16dp height, 11sp text
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/input/bar/ui/EncryptionCandidateUi.kt` — Vertical LinearLayout combining BufferBarUi + SlotBarUi (40dp total)
- `fcitx5-android/app/src/main/res/drawable/ic_encrypt_lock_24.xml` — Purple closed lock vector icon
- `fcitx5-android/app/src/main/res/drawable/ic_decrypt_lock_24.xml` — Green open lock vector icon

### Key Files Modified
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt` — Added encryptionBar to scope, layout (below KawaiiBar, keyboard below encryption bar), side padding
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/input/bar/KawaiiBarComponent.kt` — Added DI dependency on EncryptionBarComponent, wired encrypt/decrypt button clicks
- `fcitx5-android/app/src/main/java/org/fcitx/fcitx5/android/input/bar/ui/idle/ButtonsBarUi.kt` — Added encryptLockButton and decryptLockButton to toolbar FlexboxLayout

### Decisions
- Used `GradientDrawable` with `setStroke(width, color, dashWidth, dashGap)` for dashed slot borders (not `DashPathEffect` which isn't a GradientDrawable parameter)
- Used `ManagedPreferenceInternal` for Secure prefs (no Settings UI needed) vs `ManagedPreferenceCategory`
- `EncryptionBarComponent.view` defaults to `GONE` — only visible when encrypt/decrypt lock is toggled on
- State restoration reads `SecurePrefs.lockState` on scope setup