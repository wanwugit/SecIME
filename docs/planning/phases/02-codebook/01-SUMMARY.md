---
plan: 01
phase: 02-codebook
status: complete
---

## Summary

Created pure-Kotlin codebook crypto engine in secureime module with bidirectional lookup, coordinate displacement encrypt/decrypt, and encryption marker support.

### Key Files Created
- `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookTable.kt` — Bidirectional lookup table (char→coord HashMap + coord→char pages array), Closeable for memory cleanup
- `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngine.kt` — Encrypt/decrypt using p'=(p+KP)mod149, i'=(i+KI)mod100, non-dict chars preserved
- `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/EncryptionMarker.kt` — ⟦CB:1:xxx⟧...⟦/⟧ format + legacy ⸝ detection, mode detection for Emoji/SM/Codebook

### Decisions
- Coordinate displacement per spec §2.4 (not index-number output)
- 4-digit key: KP=first2, KI=last2, used as modulo offsets
- Non-dictionary characters preserved as-is (no dual-track yet)