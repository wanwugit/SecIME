package org.secureime.sect9.crypto.codebook

import java.io.Closeable

class CodebookTable(
    private val pages: List<List<String>>,
    private val charToCoord: MutableMap<Char, Pair<Int, Int>>
) : Closeable {

    val totalPages: Int get() = pages.size
    val pageSize: Int get() = if (pages.isEmpty()) 0 else pages[0].size
    val totalChars: Int get() = charToCoord.size

    fun lookupCoord(char: Char): Pair<Int, Int>? = charToCoord[char]

    fun lookupChar(page: Int, index: Int): Char? {
        if (page < 0 || page >= pages.size) return null
        val p = pages[page]
        if (index < 0 || index >= p.size) return null
        return p[index][0]
    }

    override fun close() {
        charToCoord.clear()
    }

    companion object {
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