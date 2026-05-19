package com.secure.ime.rime.wrapper

class RimeBridge {

    companion object {
        init {
            System.loadLibrary("rime_jni_bridge")
        }
    }

    external fun init(sharedDataDir: String, userDataDir: String): Long

    external fun query(session: Long, pinyin: String): Array<String>

    external fun commit(session: Long, text: String)

    external fun reset(session: Long)

    external fun destroy(session: Long)
}