---
wave: 1
depends_on: []
files_modified:
  - secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookTable.kt
  - secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngine.kt
  - secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/EncryptionMarker.kt
autonomous: true
---

# PLAN 01: Core Crypto Engine (secureime)

## Objective
Create the pure-Kotlin codebook encryption/decryption engine, table data structure, and encryption marker parser — all in the secureime module (no Android dependencies).

## Tasks

### Task 1: Create CodebookTable
<read_first>
- .planning/phases/02-codebook/02-CONTEXT.md — D1, D4 decisions (分页字典, 二维数组+HashMap)
- codebook.json — data structure reference (pages, index, meta)
</read_first>

<action>
Create file `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookTable.kt`:

```kotlin
package org.secureime.sect9.crypto.codebook

import java.io.Closeable

/**
 * Codebook lookup table for codebook encryption/decryption.
 * Loaded from a JSON source with 149 pages × 100 chars/page.
 */
class CodebookTable(
    private val pages: List<List<String>>,
    private val charToCoord: Map<Char, Pair<Int, Int>>
) : Closeable {

    val totalPages: Int get() = pages.size
    val pageSize: Int get() = if (pages.isEmpty()) 0 else pages[0].size
    val totalChars: Int get() = charToCoord.size

    /** Encrypt direction: char → (page, index) coordinate */
    fun lookupCoord(char: Char): Pair<Int, Int>? = charToCoord[char]

    /** Decrypt direction: (page, index) coordinate → char */
    fun lookupChar(page: Int, index: Int): Char? {
        if (page < 0 || page >= pages.size) return null
        val p = pages[page]
        if (index < 0 || index >= p.size) return null
        return p[index][0]
    }

    override fun close() {
        charToCoord.clear()
        (pages as? MutableList)?.clear()
    }

    companion object {
        /**
         * Build CodebookTable from raw JSON-like data structures.
         * @param pagesData List of pages, each page is a List of single-char strings.
         * @param indexData Map of char → [page, index] coordinate pairs.
         */
        fun fromData(
            pagesData: List<List<String>>,
            indexData: Map<String, List<Int>>
        ): CodebookTable {
            val charToCoord = mutableMapOf<Char, Pair<Int, Int>>()
            for ((char, coord) in indexData) {
                if (char.length == 1 && coord.size == 2) {
                    charToCoord[char[0]] = Pair(coord[0], coord[1])
                }
            }
            return CodebookTable(pagesData, charToCoord)
        }
    }
}
```

</action>

<acceptance_criteria>
- `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookTable.kt` exists
- Class has `lookupCoord(Char): Pair<Int,Int>?` method
- Class has `lookupChar(Int,Int): Char?` method
- Class implements `Closeable`
- Companion `fromData(pagesData, indexData)` factory method exists
- `totalPages`, `pageSize`, `totalChars` properties exist
</acceptance_criteria>

### Task 2: Create CodebookEngine
<read_first>
- .planning/phases/02-codebook/02-CONTEXT.md — D1, D2 decisions (坐标偏移公式, 仅汉字偏移)
- SPECIFICATION.md §2.4 — 加密公式 p'=(p+KP)mod149, i'=(i+KI)mod100
- secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookTable.kt — just created
</read_first>

<action>
Create file `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngine.kt`:

```kotlin
package org.secureime.sect9.crypto.codebook

/**
 * Codebook encryption/decryption engine.
 * Uses coordinate displacement: p'=(p+KP)mod totalPages, i'=(i+KI)mod pageSize
 * Key format: 4-digit string, first 2 digits = KP (page offset), last 2 digits = KI (index offset)
 */
class CodebookEngine(private val table: CodebookTable) {

    /**
     * Encrypt plaintext using codebook coordinate displacement.
     * Each character in the dictionary is replaced by the character at the displaced coordinate.
     * Characters not in the dictionary are preserved as-is.
     *
     * @param plaintext The input text to encrypt.
     * @param indexNumber 4-digit key string (e.g. "4207" → KP=42, KI=7).
     * @return Encrypted text (same length as input for dictionary chars).
     */
    fun encrypt(plaintext: String, indexNumber: String): String {
        val (kp, ki) = parseIndexNumber(indexNumber) ?: return plaintext
        val sb = StringBuilder(plaintext.length)
        for (ch in plaintext) {
            val coord = table.lookupCoord(ch)
            if (coord != null) {
                val newPage = (coord.first + kp) % table.totalPages
                val newIndex = (coord.second + ki) % table.pageSize
                val encrypted = table.lookupChar(newPage, newIndex)
                sb.append(encrypted ?: ch)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Decrypt ciphertext using codebook coordinate displacement (reverse).
     * Each character in the dictionary is replaced by the character at the reverse-displaced coordinate.
     * Characters not in the dictionary are preserved as-is.
     *
     * @param ciphertext The encrypted text to decrypt.
     * @param indexNumber 4-digit key string (e.g. "4207" → KP=42, KI=7).
     * @return Decrypted plaintext.
     */
    fun decrypt(ciphertext: String, indexNumber: String): String {
        val (kp, ki) = parseIndexNumber(indexNumber) ?: return ciphertext
        val sb = StringBuilder(ciphertext.length)
        for (ch in ciphertext) {
            val coord = table.lookupCoord(ch)
            if (coord != null) {
                val origPage = ((coord.first - kp) % table.totalPages + table.totalPages) % table.totalPages
                val origIndex = ((coord.second - ki) % table.pageSize + table.pageSize) % table.pageSize
                val decrypted = table.lookupChar(origPage, origIndex)
                sb.append(decrypted ?: ch)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /** Parse 4-digit index number into (KP, KI) pair. Returns null if invalid. */
    fun parseIndexNumber(key: String): Pair<Int, Int>? {
        if (key.length != 4) return null
        val kp = key.substring(0, 2).toIntOrNull() ?: return null
        val ki = key.substring(2, 4).toIntOrNull() ?: return null
        return Pair(kp, ki)
    }
}
```

</action>

<acceptance_criteria>
- `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngine.kt` exists
- `encrypt(plaintext: String, indexNumber: String): String` method exists
- `decrypt(ciphertext: String, indexNumber: String): String` method exists
- `parseIndexNumber(key: String): Pair<Int,Int>?` method exists
- Encrypt uses formula `(coord.first + kp) % totalPages` and `(coord.second + ki) % pageSize`
- Decrypt uses reverse formula with proper modulo for negative numbers
- Non-dictionary characters preserved as-is in both encrypt and decrypt
</acceptance_criteria>

### Task 3: Create EncryptionMarker
<read_first>
- .planning/phases/02-codebook/02-CONTEXT.md — D6 decision (加密标记格式)
- SPECIFICATION.md §3.1-3.2 — 标识头定义和模式检测规则
</read_first>

<action>
Create file `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/EncryptionMarker.kt`:

```kotlin
package org.secureime.sect9.crypto.codebook

/**
 * Encryption marker for codebook mode.
 * Format: ⟦CB:1:{contactId}⟧密文⟦/⟧
 * Also supports legacy spec header: ⸝ (U+2E1D)
 */
object EncryptionMarker {
    const val MARKER_OPEN = "⟦"
    const val MARKER_CLOSE = "⟦/⟧"
    const val MODE_CODEBOOK = "CB"
    const val VERSION = "1"
    const val LEGACY_HEADER = "⸝" // ⸝ RAISED COMMA

    /** Wrap ciphertext with encryption marker. */
    fun wrap(ciphertext: String, contactId: String): String {
        return "$MARKER_OPEN$MODE_CODEBOOK:$VERSION:$contactId$MARKER_CLOSE$ciphertext$MARKER_CLOSE"
    }

    /**
     * Parse encrypted text to extract contactId and ciphertext.
     * Supports both ⟦CB:1:xxx⟧...⟦/⟧ and legacy ⸝... format.
     * @return Pair(contactId, ciphertext) or null if not a valid marker.
     */
    fun unwrap(text: String): Pair<String, String>? {
        // Try new format: ⟦CB:1:{contactId}⟧密文⟦/⟧
        if (text.startsWith(MARKER_OPEN)) {
            val afterOpen = text.substring(MARKER_OPEN.length)
            val headerEnd = afterOpen.indexOf(MARKER_CLOSE)
            if (headerEnd > 0) {
                val header = afterOpen.substring(0, headerEnd)
                val parts = header.split(":")
                if (parts.size >= 3 && parts[0] == MODE_CODEBOOK && parts[1] == VERSION) {
                    val contactId = parts[2]
                    val ciphertext = afterOpen.substring(headerEnd + MARKER_CLOSE.length)
                    if (ciphertext.endsWith(MARKER_CLOSE)) {
                        return Pair(contactId, ciphertext.substring(0, ciphertext.length - MARKER_CLOSE.length))
                    }
                }
            }
        }
        // Try legacy format: ⸝密文
        if (text.startsWith(LEGACY_HEADER)) {
            val ciphertext = text.substring(LEGACY_HEADER.length)
            return Pair("", ciphertext)
        }
        return null
    }

    /** Check if text contains an encryption marker. */
    fun isEncrypted(text: String): Boolean {
        return text.startsWith(MARKER_OPEN) || text.startsWith(LEGACY_HEADER)
    }

    /** Detect encryption mode from text header. */
    fun detectMode(text: String): String? {
        return when {
            text.startsWith("⸝·") -> "EMOJI"   // ⸝·
            text.startsWith("⸝ ") -> "SM"            // ⸝ (with space)
            text.startsWith("⸝") -> "CODEBOOK"       // ⸝ (no space)
            text.startsWith(MARKER_OPEN) -> "CODEBOOK"    // ⟦CB:...
            else -> null
        }
    }
}
```

</action>

<acceptance_criteria>
- `secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/EncryptionMarker.kt` exists
- `wrap(ciphertext, contactId): String` method exists
- `unwrap(text): Pair<String,String>?` method exists
- `isEncrypted(text): Boolean` method exists
- `detectMode(text): String?` method exists
- MARKER_OPEN = "⟦", MARKER_CLOSE = "⟦/⟧", MODE_CODEBOOK = "CB", VERSION = "1"
- LEGACY_HEADER = "⸝" (⸝)
- detectMode handles ⸝· (Emoji), ⸝+space (SM), ⸝ alone (Codebook)
</acceptance_criteria>

## Verification
- All 3 files compile in secureime module (pure JVM, no Android imports)
- CodebookEngine.encrypt then .decrypt with same indexNumber returns original text
- EncryptionMarker.wrap then .unwrap returns original (contactId, ciphertext)

## must_haves
- CodebookTable with O(1) bidirectional lookup (char↔coord)
- CodebookEngine with coordinate displacement formula per spec §2.4
- EncryptionMarker with ⟦CB:1:xxx⟧...⟦/⟧ format and legacy ⸝ detection
