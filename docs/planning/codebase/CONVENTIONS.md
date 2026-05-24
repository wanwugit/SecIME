# Conventions

## Kotlin Style
- **Official style**: `kotlin.code.style=official` in `gradle.properties`
- **Naming**: Standard Kotlin conventions — PascalCase for classes, camelCase for functions/properties
- **Package naming**: `org.fcitx.fcitx5.android.*` for app, `org.secureime.sect9.*` for secureime module

## Component Pattern (mechdancer.dependency)
- Components implement `UniqueComponent<Self>()` + `Dependent`
- DI via `by manager.must<ComponentType>()` — field delegation
- `ManagedHandler` for lifecycle management via `managedHandler()`
- Components are registered with `InputWindowManager` and resolved at runtime

## State Management
- **StateFlow**: Primary reactive state mechanism
- `CandidatePipeline` and `PreeditPipeline` use `StateFlow<State>` + `publish()` pattern
- UI components observe StateFlow via `lifecycleScope.launch { pipeline.state.collect {} }`

## UI Construction
- **Splitties DSL**: Programmatic UI construction preferred over XML layouts
  - `splitties.views.dsl.core.*` — frameLayout, textView, etc.
  - `splitties.views.dsl.constraintlayout.*` — ConstraintLayout DSL
- **Native Android widgets**: `android.widget.Button`, `android.widget.Switch` — NOT Material Components
  - SPECIFICATION.md explicitly warns against MaterialButton, SwitchMaterial, BottomSheetDialog
  - Dialogs use `Dialog` class, not `BottomSheetDialog`

## Custom Views
- **AutoScaleTextView**: Custom TextView that supports both auto-scaling and span rendering
  - `hasSpans` flag detects `Spanned` input
  - When `hasSpans=true`, delegates to `super.onMeasure()` / `super.onDraw()` for span rendering
  - Used for language key with `RelativeSizeSpan` (Chinese 1.5x, English 0.8x)

## Logging
- **SecLogger**: Custom logging utility in `input/SecLogger.kt`
  - Tag format: `[KBWin]`, `[T9Keyboard]`, `[CandAdapter]`, `[Bar]` etc.
  - Used throughout input/ package for debug tracing

## Dimension/Spacing Conventions (from spec)
- Dialog: 24dp corner radius, 24dp padding
- Text: title 18sp bold, body default, input 11sp for slots
- Slots: 16dp height, 3-char truncation, 4dp spacing
- Dialog width: 85% of screen width

## Error Handling
- Minimal explicit error handling in UI layer
- Exceptions logged via Timber/SecLogger
- fcitx5 daemon failures handled in `FcitxConnection` with retry logic
- Native crashes are fatal (IME process restart)

## Coroutine Usage
- `lifecycleScope.launch {}` for UI-bound async work
- `suspend` functions on `LanguageAdapter` interface
- No structured concurrency patterns beyond lifecycleScope

## Native Code Conventions
- C++ modules follow fcitx5 addon naming: `androidfrontend`, `androidkeyboard`, `androidnotification`
- JNI functions follow standard `Java_org_fcitx_` naming
- CMake targets declared in `app/src/main/cpp/CMakeLists.txt`

---
*Last updated: 2026-05-23 after codebase scan*