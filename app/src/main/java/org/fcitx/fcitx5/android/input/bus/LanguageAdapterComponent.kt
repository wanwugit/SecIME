package org.fcitx.fcitx5.android.input.bus

import org.secureime.sect9.language.LanguageAdapter
import org.fcitx.fcitx5.android.input.FcitxInputMethodService
import org.fcitx.fcitx5.android.input.dependency.inputMethodService
import org.mechdancer.dependency.Dependent
import org.mechdancer.dependency.UniqueComponent
import org.mechdancer.dependency.manager.ManagedHandler
import org.mechdancer.dependency.manager.managedHandler

class LanguageAdapterComponent :
    UniqueComponent<LanguageAdapterComponent>(), Dependent, ManagedHandler by managedHandler(), LanguageAdapter {

    private val service: FcitxInputMethodService by manager.inputMethodService()
    private val adapter: FcitxLanguageAdapter by lazy { FcitxLanguageAdapter(service) }

    override suspend fun select(index: Int) =
        adapter.select(index)

    override suspend fun reset() =
        adapter.reset()

    override suspend fun commitText(text: String) =
        adapter.commitText(text)

    override suspend fun sendKey(key: String, states: Int, code: Int) =
        adapter.sendKey(key, states, code)

    override suspend fun sendKeySym(sym: Int, states: Int) =
        adapter.sendKeySym(sym, states)

    override suspend fun offsetCandidatePage(offset: Int) =
        adapter.offsetCandidatePage(offset)

    override suspend fun enumerateIme() =
        adapter.enumerateIme()

    override suspend fun toggleIme() =
        adapter.toggleIme()

    override suspend fun triggerQuickPhrase() =
        adapter.triggerQuickPhrase()

    override suspend fun triggerUnicode() =
        adapter.triggerUnicode()

    override suspend fun finishComposing() =
        adapter.finishComposing()

    override suspend fun commitCurrentPreedit() =
        adapter.commitCurrentPreedit()

    override suspend fun ensureChineseIme() =
        adapter.ensureChineseIme()

    override suspend fun activateT9Schema() =
        adapter.activateT9Schema()

    override suspend fun activateQwertySchema() =
        adapter.activateQwertySchema()
}