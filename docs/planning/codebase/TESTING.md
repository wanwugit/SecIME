# Testing

## Test Framework
- **JUnit 4.13.2** — unit tests
- **JUnit Jupiter 5.10.2** — secureime module tests
- **AndroidX Test 1.7.0** — instrumented tests (runner + rules)
- **Lifecycle Testing 2.10.0** — lifecycle-aware test support

## Test Structure

### Unit Tests
```
app/src/test/java/org/fcitx/fcitx5/android/
├── StringEscapeTest.kt
└── ThemeSerializationTest.kt
```
Only 2 unit test files in the app module. Very minimal coverage.

### Instrumented Tests
```
app/src/androidTest/java/org/fcitx/fcitx5/android/
└── FcitxTest.kt
```
Single integration test file.

### Secureime Tests
No test files exist yet in `secureime/src/test/`.

## Test Configuration
- `testImplementation(kotlin("test-junit5"))` in secureime module
- `useJUnitPlatform()` configured for secureime tests
- AndroidX Test runner configured in `app/build.gradle.kts`
- Room schema export configured: `room.schemaLocation = "$projectDir/schemas"`

## Coverage Assessment
- **Very low**: Only 3 test files total (2 unit + 1 instrumented)
- **No T9/input tests**: No tests for InputDecisionBus, T9 input model, keyboard actions
- **No encryption tests**: None yet (encryption module not implemented)
- **No pipeline tests**: CandidatePipeline and PreeditPipeline have no test coverage
- **Critical gap**: Core input routing and state management is completely untested

## Mocking
- No mocking framework detected (MockK/Mockito not in dependencies)
- Tests appear to use real components (Fcitx daemon integration in FcitxTest)

## CI/CD
- No CI configuration files detected (no `.github/workflows/`, no `Jenkinsfile`)
- Build appears to be local-only

---
*Last updated: 2026-05-23 after codebase scan*