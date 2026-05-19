package org.fcitx.fcitx5.android.input.bus

import org.fcitx.fcitx5.android.input.SecLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.secureime.sect9.language.LanguageAdapter
import org.fcitx.fcitx5.android.input.FcitxInputMethodService

class FcitxLanguageAdapter(private val service: FcitxInputMethodService) : LanguageAdapter {

    override suspend fun select(index: Int) {
        SecLogger.d("FcitxAdapter", "select: index=$index")
        return suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                select(index)
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun reset() {
        return suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                reset()
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun commitText(text: String) {
        service.commitText(text)
    }

    override suspend fun sendKey(key: String, states: Int, code: Int) {
        SecLogger.d("FcitxAdapter", "sendKey: key='$key', states=$states, code=$code")
        suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                sendKey(key, states.toUInt(), code)
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun sendKeySym(sym: Int, states: Int) {
        suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                sendKey(sym, states.toUInt())
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun offsetCandidatePage(offset: Int) {
        suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                offsetCandidatePage(offset)
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun enumerateIme() {
        suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                enumerateIme()
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun toggleIme() {
        suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                toggleIme()
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun triggerQuickPhrase() {
        suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                triggerQuickPhrase()
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun triggerUnicode() {
        suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                triggerUnicode()
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun finishComposing() {
        service.finishComposing()
    }

    override suspend fun ensureChineseIme() {
        SecLogger.d("FcitxAdapter", "ensureChineseIme: start")
        // Use direct Rime API: set ascii_mode=false to ensure Chinese mode
        suspendCancellableCoroutine<Unit> { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                rimeSetAsciiMode(false)
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
        // Verify the IME language code is now Chinese
        for (i in 0..10) {
            val isChinese = suspendCancellableCoroutine<Boolean> { cont ->
                service.postFcitxJob {
                    if (!cont.isActive) return@postFcitxJob
                    val current = inputMethodEntryCached
                    if (current.languageCode.startsWith("zh")) {
                        cont.resumeWith(Result.success(true))
                    } else {
                        enumerateIme()
                        if (cont.isActive) cont.resumeWith(Result.success(false))
                    }
                }
            }
            if (isChinese) break
            delay(50)
        }
        SecLogger.d("FcitxAdapter", "ensureChineseIme: done")
    }

    override suspend fun activateT9Schema() {
        SecLogger.d("FcitxAdapter", "activateT9Schema: selecting rime_frost_t9 via direct Rime API")
        suspendCancellableCoroutine<Unit> { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                rimeSelectSchema("rime_frost_t9")
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
        SecLogger.d("FcitxAdapter", "activateT9Schema: done")
    }

    override suspend fun activateQwertySchema() {
        SecLogger.d("FcitxAdapter", "activateQwertySchema: selecting rime_frost via direct Rime API")
        suspendCancellableCoroutine<Unit> { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                rimeSelectSchema("rime_frost")
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
        SecLogger.d("FcitxAdapter", "activateQwertySchema: done")
    }

    override suspend fun commitCurrentPreedit() {
        suspendCancellableCoroutine { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                if (inputMethodEntryCached.languageCode.startsWith("zh")) {
                    if (clientPreeditCached.isNotEmpty() || inputPanelCached.preedit.isNotEmpty()) {
                        select(0)
                    }
                } else {
                    service.finishComposing()
                }
                reset()
                if (cont.isActive) cont.resumeWith(Result.success(Unit))
            }
        }
    }
}
