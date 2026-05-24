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
        ensureChineseImeWithSchema(null)
    }

    /** Activate Chinese IME and optionally switch schema in one atomic FcitxJob */
    private suspend fun ensureChineseImeWithSchema(schema: String?) {
        SecLogger.d("FcitxAdapter", "ensureChineseIme: start, schema=$schema")
        val alreadyChinese = suspendCancellableCoroutine<Boolean> { cont ->
            service.postFcitxJob {
                if (!cont.isActive) return@postFcitxJob
                val current = inputMethodEntryCached
                val isChinese = current.languageCode.startsWith("zh")
                if (isChinese) {
                    // Already on Chinese IME — just switch schema if needed
                    if (schema != null) {
                        rimeSelectSchema(schema)
                    }
                    cont.resumeWith(Result.success(true))
                } else {
                    // Not on Chinese IME — activate Rime + switch schema atomically
                    val rimeEntries = availableIme().filter { it.addon.equals("rime", ignoreCase = true) }
                    if (rimeEntries.isNotEmpty()) {
                        activateIme(rimeEntries.first().uniqueName)
                    }
                    rimeSetAsciiMode(false)
                    // Set schema immediately after IME activation — same FcitxJob ensures Rime session sees it
                    if (schema != null) {
                        rimeSelectSchema(schema)
                    }
                    cont.resumeWith(Result.success(false))
                }
            }
        }
        if (alreadyChinese) {
            SecLogger.d("FcitxAdapter", "ensureChineseIme: already Chinese, done")
            return
        }
        // Wait for IME switch to take effect
        delay(200)
        // Verify schema took effect
        if (schema != null) {
            val currentSchema = suspendCancellableCoroutine<String> { cont ->
                service.postFcitxJob {
                    if (!cont.isActive) return@postFcitxJob
                    cont.resumeWith(Result.success(rimeCurrentSchema()))
                }
            }
            SecLogger.d("FcitxAdapter", "ensureChineseIme: currentSchema='$currentSchema', expected='$schema'")
            if (currentSchema != schema) {
                // Schema not applied — force IC recreation then retry
                SecLogger.d("FcitxAdapter", "ensureChineseIme: schema not applied, forcing focusOutIn + retry")
                suspendCancellableCoroutine<Unit> { cont ->
                    service.postFcitxJob {
                        if (!cont.isActive) return@postFcitxJob
                        focusOutIn()
                        if (cont.isActive) cont.resumeWith(Result.success(Unit))
                    }
                }
                delay(100)
                suspendCancellableCoroutine<Unit> { cont ->
                    service.postFcitxJob {
                        if (!cont.isActive) return@postFcitxJob
                        rimeSelectSchema(schema)
                        if (cont.isActive) cont.resumeWith(Result.success(Unit))
                    }
                }
                delay(50)
            }
        }
        SecLogger.d("FcitxAdapter", "ensureChineseIme: done")
    }

    override suspend fun activateT9Schema() {
        SecLogger.d("FcitxAdapter", "activateT9Schema: via ensureChineseImeWithSchema")
        ensureChineseImeWithSchema("rime_frost_t9")
    }

    override suspend fun activateQwertySchema() {
        SecLogger.d("FcitxAdapter", "activateQwertySchema: via ensureChineseImeWithSchema")
        ensureChineseImeWithSchema("rime_frost")
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
