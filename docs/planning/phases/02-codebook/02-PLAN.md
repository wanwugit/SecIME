---
wave: 2
depends_on:
  - 02-codebook
files_modified:
  - app/src/main/assets/codebook_default.json
  - app/src/main/java/org/fcitx/fcitx5/android/data/secure/codebook/AssetsCodebookSource.kt
  - secureime/src/test/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngineTest.kt
autonomous: true
---

# PLAN 02: App-Layer Integration + Tests

## Objective
Copy codebook.json to APK assets, create Android-side loader, and write unit tests for the crypto engine.

## Tasks

### Task 1: Copy codebook.json to assets
<read_first>
- .planning/phases/02-codebook/02-CONTEXT.md — D5 decision (JSON in assets)
- codebook.json — source file (149 pages × 100 chars, 756KB)
</read_first>

<action>
Copy `codebook.json` to `app/src/main/assets/codebook_default.json`.

</action>

<acceptance_criteria>
- `app/src/main/assets/codebook_default.json` exists
- File content is identical to project root `codebook.json`
- File size ~756KB
</acceptance_criteria>

### Task 2: Create AssetsCodebookSource
<read_first>
- .planning/phases/02-codebook/02-CONTEXT.md — D5, D7 decisions (JSON in assets, secureime 纯 JVM + app 层加载)
- secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookTable.kt — fromData factory
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDatabase.kt — existing Android context usage pattern
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/codebook/AssetsCodebookSource.kt`:

```kotlin
package org.fcitx.fcitx5.android.data.secure.codebook

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.secureime.sect9.crypto.codebook.CodebookTable
import java.io.IOException

/**
 * Loads codebook data from APK assets JSON file.
 * Provides CodebookTable instances for encryption/decryption.
 */
class AssetsCodebookSource(private val context: Context) {

    /**
     * Load a codebook table from assets.
     * @param assetPath Path in assets folder (default: "codebook_default.json")
     * @return CodebookTable loaded from the asset file.
     * @throws IOException if the asset file cannot be read.
     */
    fun load(assetPath: String = DEFAULT_CODEBOOK): CodebookTable {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val root = JSONObject(json)

        val pagesArr = root.getJSONArray("pages")
        val pagesData = mutableListOf<List<String>>()
        for (i in 0 until pagesArr.length()) {
            val pageArr = pagesArr.getJSONArray(i)
            val page = mutableListOf<String>()
            for (j in 0 until pageArr.length()) {
                page.add(pageArr.getString(j))
            }
            pagesData.add(page)
        }

        val indexObj = root.getJSONObject("index")
        val indexData = mutableMapOf<String, List<Int>>()
        val keys = indexObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val coordArr = indexObj.getJSONArray(key)
            indexData[key] = listOf(coordArr.getInt(0), coordArr.getInt(1))
        }

        return CodebookTable.fromData(pagesData, indexData)
    }

    companion object {
        const val DEFAULT_CODEBOOK = "codebook_default.json"
    }
}
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/codebook/AssetsCodebookSource.kt` exists
- Class takes `Context` constructor parameter
- `load(assetPath: String = DEFAULT_CODEBOOK): CodebookTable` method exists
- Parses JSON "pages" array and "index" object
- Uses `CodebookTable.fromData()` factory method
- DEFAULT_CODEBOOK = "codebook_default.json"
</acceptance_criteria>

### Task 3: Create CodebookEngineTest
<read_first>
- secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngine.kt — engine under test
- secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/CodebookTable.kt — table under test
- secureime/src/main/kotlin/org/secureime/sect9/crypto/codebook/EncryptionMarker.kt — marker under test
</read_first>

<action>
Create file `secureime/src/test/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngineTest.kt`:

```kotlin
package org.secureime.sect9.crypto.codebook

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CodebookEngineTest {

    // Mini codebook: 3 pages × 5 chars for deterministic testing
    // Pages layout:
    //   page 0: 你 好 世 界 吗
    //   page 1: 我 是 人 的 了
    //   page 2: 一 个 大 小 多
    private val testPages = listOf(
        listOf("你", "好", "世", "界", "吗"),
        listOf("我", "是", "人", "的", "了"),
        listOf("一", "个", "大", "小", "多")
    )

    private val testIndex = mapOf(
        "你" to listOf(0, 0), "好" to listOf(0, 1), "世" to listOf(0, 2),
        "界" to listOf(0, 3), "吗" to listOf(0, 4),
        "我" to listOf(1, 0), "是" to listOf(1, 1), "人" to listOf(1, 2),
        "的" to listOf(1, 3), "了" to listOf(1, 4),
        "一" to listOf(2, 0), "个" to listOf(2, 1), "大" to listOf(2, 2),
        "小" to listOf(2, 3), "多" to listOf(2, 4)
    )

    private lateinit var table: CodebookTable
    private lateinit var engine: CodebookEngine

    @Before
    fun setup() {
        table = CodebookTable.fromData(testPages, testIndex)
        engine = CodebookEngine(table)
    }

    @After
    fun teardown() {
        table.close()
    }

    @Test
    fun `table lookup and reverse lookup are consistent`() {
        for ((char, coord) in testIndex) {
            val lookedUp = table.lookupCoord(char[0])
            assertNotNull("Char '$char' should have a coordinate", lookedUp)
            assertEquals(coord[0], lookedUp!!.first)
            assertEquals(coord[1], lookedUp.second)

            val reverseLookup = table.lookupChar(coord[0], coord[1])
            assertEquals(char[0], reverseLookup)
        }
    }

    @Test
    fun `encrypt then decrypt returns original`() {
        val plaintext = "你好世界"
        val key = "0102" // KP=1, KI=2
        val encrypted = engine.encrypt(plaintext, key)
        val decrypted = engine.decrypt(encrypted, key)
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt with key 0000 returns same text`() {
        val plaintext = "你好世界"
        val key = "0000" // no offset
        val encrypted = engine.encrypt(plaintext, key)
        assertEquals(plaintext, encrypted)
    }

    @Test
    fun `non-dictionary chars preserved in encrypt`() {
        val plaintext = "你abc好"
        val key = "0102"
        val encrypted = engine.encrypt(plaintext, key)
        // abc should be preserved
        assertTrue(encrypted.contains("abc"))
        // 你 and 好 should be replaced (different chars)
        val decrypted = engine.decrypt(encrypted, key)
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `different keys produce different ciphertext`() {
        val plaintext = "你好世界"
        val key1 = "0102"
        val key2 = "0203"
        val encrypted1 = engine.encrypt(plaintext, key1)
        val encrypted2 = engine.encrypt(plaintext, key2)
        assertTrue("Different keys should produce different ciphertext", encrypted1 != encrypted2)
    }

    @Test
    fun `encrypt with wrap-around modulo`() {
        // Use key that forces wrap-around
        val plaintext = "多" // at (2,4), pageSize=5
        val key = "0101" // KP=1, KI=1
        // (2+1)%3=0, (4+1)%5=0 → pages[0][0] = "你"
        val encrypted = engine.encrypt(plaintext, key)
        assertEquals("你", encrypted)
    }

    @Test
    fun `invalid index number returns input unchanged`() {
        val plaintext = "你好"
        assertEquals(plaintext, engine.encrypt(plaintext, "123"))   // too short
        assertEquals(plaintext, engine.encrypt(plaintext, "abcd")) // non-numeric
        assertEquals(plaintext, engine.decrypt(plaintext, "12"))
    }

    @Test
    fun `marker wrap and unwrap`() {
        val contactId = "alice"
        val ciphertext = "你好密文"
        val wrapped = EncryptionMarker.wrap(ciphertext, contactId)
        val result = EncryptionMarker.unwrap(wrapped)
        assertNotNull(result)
        assertEquals(contactId, result!!.first)
        assertEquals(ciphertext, result.second)
    }

    @Test
    fun `marker isEncrypted detection`() {
        assertTrue(EncryptionMarker.isEncrypted("⟦CB:1:alice⟧密文⟦/⟧"))
        assertTrue(EncryptionMarker.isEncrypted("⸝密文"))
        assertTrue(!EncryptionMarker.isEncrypted("普通文本"))
    }

    @Test
    fun `marker detectMode`() {
        assertEquals("CODEBOOK", EncryptionMarker.detectMode("⟦CB:1:alice⟧密文⟦/⟧"))
        assertEquals("CODEBOOK", EncryptionMarker.detectMode("⸝密文"))
        assertEquals("EMOJI", EncryptionMarker.detectMode("⸝·密文"))
        assertNull(EncryptionMarker.detectMode("普通文本"))
    }

    @Test
    fun `encrypt decrypt round-trip with multiple chars`() {
        val plaintext = "我是一个人你好吗大小多"
        val key = "0203"
        val encrypted = engine.encrypt(plaintext, key)
        val decrypted = engine.decrypt(encrypted, key)
        assertEquals(plaintext, decrypted)
    }
}
```

</action>

<acceptance_criteria>
- `secureime/src/test/kotlin/org/secureime/sect9/crypto/codebook/CodebookEngineTest.kt` exists
- Test class covers: table lookup consistency, encrypt-decrypt round-trip, zero-offset identity, non-dict chars preserved, different keys differ, wrap-around modulo, invalid key handling, marker wrap/unwrap, marker detection, multi-char round-trip
- At least 10 test methods
</acceptance_criteria>

## Verification
- `codebook_default.json` in assets is valid JSON with pages + index keys
- AssetsCodebookSource compiles (uses org.json from Android SDK)
- Unit tests compile and pass in secureime test module

## must_haves
- codebook_default.json asset file
- AssetsCodebookSource that parses JSON and creates CodebookTable
- Unit tests proving encrypt→decrypt round-trip works
