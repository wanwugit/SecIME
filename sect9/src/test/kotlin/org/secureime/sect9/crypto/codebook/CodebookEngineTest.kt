package org.secureime.sect9.crypto.codebook

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CodebookEngineTest {

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
        val key = "0102"
        val encrypted = engine.encrypt(plaintext, key)
        val decrypted = engine.decrypt(encrypted, key)
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt with key 0000 returns same text`() {
        val plaintext = "你好世界"
        val key = "0000"
        val encrypted = engine.encrypt(plaintext, key)
        assertEquals(plaintext, encrypted)
    }

    @Test
    fun `non-dictionary chars preserved in encrypt`() {
        val plaintext = "你abc好"
        val key = "0102"
        val encrypted = engine.encrypt(plaintext, key)
        assertTrue(encrypted.contains("abc"))
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
        val plaintext = "多"
        val key = "0101"
        val encrypted = engine.encrypt(plaintext, key)
        assertEquals("你", encrypted)
    }

    @Test
    fun `invalid index number returns input unchanged`() {
        val plaintext = "你好"
        assertEquals(plaintext, engine.encrypt(plaintext, "123"))
        assertEquals(plaintext, engine.encrypt(plaintext, "abcd"))
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