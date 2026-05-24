package org.secureime.sect9.crypto.codebook

class CodebookEngine(private val table: CodebookTable) {

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

    fun parseIndexNumber(key: String): Pair<Int, Int>? {
        if (key.length != 4) return null
        val kp = key.substring(0, 2).toIntOrNull() ?: return null
        val ki = key.substring(2, 4).toIntOrNull() ?: return null
        return Pair(kp, ki)
    }
}