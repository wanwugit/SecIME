package org.secureime.sect9.language

interface LanguageAdapter {
    suspend fun sendKey(key: String, states: Int, code: Int = 0)
    suspend fun sendKeySym(sym: Int, states: Int)
    suspend fun select(index: Int)
    suspend fun reset()
    suspend fun commitText(text: String)
    suspend fun offsetCandidatePage(offset: Int)
    suspend fun enumerateIme()
    suspend fun toggleIme()
    suspend fun triggerQuickPhrase()
    suspend fun triggerUnicode()
    suspend fun finishComposing()
    suspend fun commitCurrentPreedit()
    suspend fun ensureChineseIme()
    suspend fun activateT9Schema()
    suspend fun activateQwertySchema()
}