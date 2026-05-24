---
plan: 02
phase: 02-codebook
status: complete
---

## Summary

Copied codebook.json to assets, created Android-side JSON loader, and wrote 11 unit tests for crypto engine.

### Key Files Created
- `app/src/main/assets/codebook_default.json` — 149 pages × 100 chars = 14810 chars, with pages array and index HashMap
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/codebook/AssetsCodebookSource.kt` — Android context-based loader parsing JSON into CodebookTable
- `secureime/src/test/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngineTest.kt` — 11 tests: round-trip, zero-offset, non-dict chars, different keys, wrap-around, invalid keys, marker wrap/unwrap, detection, mode, multi-char

### Decisions
- codebook.json directly in assets (not converted to text format)
- AssetsCodebookSource uses org.json (Android SDK built-in) for JSON parsing
- Test uses mini 3×5 codebook for deterministic verification