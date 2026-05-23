package org.fcitx.fcitx5.android.data.secure.codebook

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.secureime.sect9.crypto.codebook.CodebookTable
import java.io.IOException

class AssetsCodebookSource(private val context: Context) {

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