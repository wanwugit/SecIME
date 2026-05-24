# Integrations

## Fcitx5 Daemon (Core Integration)
- **Bridge**: `app/src/main/cpp/CMakeLists.txt` builds JNI native lib
- **API**: `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt` wraps native calls
- **Lifecycle**: `FcitxDaemon` manages fcitx5 process lifecycle (connect/disconnect)
- **Events**: `FcitxEvent` sealed class handles candidate/preedit/status updates
- **Key flow**: UI key press → `InputDecisionBus.sendKey()` → `LanguageAdapter.sendKey()` → JNI → fcitx5 daemon → `onFcitxCandidateEvent`/`onFcitxPreeditEvent` → Pipeline → UI

## Rime Engine (Embedded)
- **Native addon**: `app/src/main/cpp/fcitx5-rime/src/rimeengine.cpp` — Rime as fcitx5 addon
- **Kotlin bridge**: `lib/rime-bridge/src/main/kotlin/com/secure/ime/rime/`
  - `RimeBridge.kt` — high-level Rime API wrapper
  - `RimeSessionManager.kt` — session management
- **Schema**: `rime_frost_t9` schema handles T9 digit→pinyin→hanzi mapping
- **Dictionary**: `rime-frost/` contains dictionary source files
- **Integration**: Rime is compiled as a CMake target (`rime`) within the fcitx5 native build

## Android IME Framework
- **Service**: `FcitxInputMethodService` extends `LifecycleInputMethodService`
- **Entry**: Declared in `app/src/main/AndroidManifest.xml` as `android.view.InputMethod`
- **Window**: `InputWindowManager` manages keyboard window lifecycle
- **Lifecycle callbacks**: `onCreate`, `onWindowShown`, `onWindowHidden`, `onStartInput`

## Clipboard Monitoring
- **Manager**: `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/ClipboardManager.kt`
- **Database**: Room database with `ClipboardDao`, `ClipboardEntry`
- **Auto-decrypt**: Planned feature — monitor clipboard changes, detect encrypted content, auto-decrypt
- **Hash dedup**: Planned — prevent re-processing same ciphertext ("回魂" problem)

## Dependency Injection (mechdancer.dependency)
- **Pattern**: Component-based DI via `UniqueComponent`, `Dependent`, `ManagedHandler`
- **Resolution**: `by manager.must<ComponentType>()` — field delegation
- **Key components**: `InputDecisionBus`, `LanguageAdapterComponent`, `KawaiiBarComponent`, `PopupComponent`
- **Window manager**: `InputWindowManager` hosts all components via `manager`

## Data Storage
- **Room**: `ClipboardDatabase` for clipboard entries
- **SharedPreferences**: `AppPrefs` for all user preferences (keyboard, theme, etc.)
- **Planned**: Friend/Channel/IndexNumber storage via Room for encryption contacts

## Planned: KGC Server
- **Purpose**: SM9 key management (master public key, user private key)
- **Stack**: Spring Boot (per SPECIFICATION.md)
- **APIs**: Get MPK, Apply private key, Key update, Device binding
- **Status**: Not yet implemented

## Planned: GmSSL JNI
- **Purpose**: SM9/SM4 cryptographic operations
- **Integration**: Native .so loaded via `System.loadLibrary("gmssl")` then `System.loadLibrary("gmssljni")`
- **Build**: Cross-compiled with Android NDK CMake toolchain
- **Status**: Not yet integrated

---
*Last updated: 2026-05-23 after codebase scan*