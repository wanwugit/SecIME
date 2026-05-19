package com.secure.ime.rime.session

import com.secure.ime.rime.wrapper.RimeBridge

class RimeSessionManager(private val bridge: RimeBridge) {

    @Volatile
    private var currentSession: Long = 0L

    @Synchronized
    fun createSession(sharedDataDir: String, userDataDir: String): Long {
        destroySession()
        currentSession = bridge.init(sharedDataDir, userDataDir)
        return currentSession
    }

    @Synchronized
    fun query(pinyin: String): Array<String> {
        if (currentSession == 0L) return emptyArray()
        return bridge.query(currentSession, pinyin)
    }

    @Synchronized
    fun commit(text: String) {
        if (currentSession == 0L) return
        bridge.commit(currentSession, text)
    }

    @Synchronized
    fun reset() {
        if (currentSession == 0L) return
        bridge.reset(currentSession)
    }

    @Synchronized
    fun destroySession() {
        if (currentSession != 0L) {
            bridge.destroy(currentSession)
            currentSession = 0L
        }
    }

    fun hasActiveSession(): Boolean = currentSession != 0L
}