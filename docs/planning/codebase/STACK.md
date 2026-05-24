# Stack

## Languages
- **Kotlin** 2.3.21 — primary language for all Android/JVM code
- **C++** — native modules (fcitx5 addon integration, Rime engine bridge)
- **CMake** 3.31.6 — native build system

## Android Platform
- **compileSdk**: 36
- **targetSdk**: 36
- **minSdk**: 23
- **NDK**: (configured via toolchain, path varies)
- **JVM toolchain**: 17 (secureime module)

## Build System
- **Gradle** (AGP 9.2.0)
- **KSP** 2.3.7 (annotation processing for Room, codegen)
- **Convention plugins** in `build-logic/convention/` (AndroidAppConvention, NativeAppConvention, etc.)
- **Composite build**: `secureime/` included as separate Gradle project

## Key Dependencies

### AndroidX
| Library | Version |
|---------|---------|
| Activity | 1.13.0 |
| AppCompat | 1.7.1 |
| Lifecycle | 2.10.0 |
| Navigation | 2.9.8 |
| Room | 2.8.4 |
| Paging | 3.4.2 |
| RecyclerView | 1.4.0 |
| ConstraintLayout | 2.2.1 |
| ViewPager2 | 1.1.0 |
| Preference | 1.2.1 |
| Startup | 1.2.0 |
| Autofill | 1.3.0 |

### Kotlin Ecosystem
| Library | Version |
|---------|---------|
| kotlinx-coroutines | 1.10.2 |
| kotlinx-serialization | 1.11.0 |
| Arrow (core+functions) | 2.2.2.1 |

### UI/Utility
| Library | Version |
|---------|---------|
| Splitties | 3.0.0 |
| Material | 1.13.0 |
| Flexbox | 3.0.0 |
| ImageCropper | 4.7.0 |
| AboutLibraries | 14.0.1 |
| Timber | 5.0.1 |

### Dependency Injection
| Library | Version |
|---------|---------|
| org.mechdancer:dependency | 0.1.2 |

### Testing
| Library | Version |
|---------|---------|
| JUnit | 4.13.2 |
| JUnit Jupiter | 5.10.2 |
| AndroidX Test | 1.7.0 |
| Lifecycle Testing | 2.10.0 |

## Native Libraries
- **libnative-lib.so** — JNI bridge for fcitx5
- **librime.so** — Rime input method engine (embedded)
- **libandroidfrontend.so** — fcitx5 Android frontend addon
- **libandroidkeyboard.so** — keyboard event handling addon
- **libandroidnotification.so** — notification addon
- **GmSSL** (planned, not yet integrated) — v3.1.1 for SM9/SM4 crypto

## Configuration Files
- `fcitx5-android/gradle/libs.versions.toml` — version catalog
- `secureime/gradle/libs.versions.toml` — secureime version catalog
- `fcitx5-android/gradle.properties` — JVM args, AndroidX, KSP2 flags
- `fcitx5-android/build-logic/convention/src/main/kotlin/Versions.kt` — SDK versions

---
*Last updated: 2026-05-23 after codebase scan*