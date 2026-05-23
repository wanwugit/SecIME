/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.bar

import android.view.View
import android.widget.FrameLayout
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.FcitxInputMethodService
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.BooleanKey.BufferEmpty
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.State.ENCRYPTED
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.State.ENCRYPTING
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.State.LOCKED
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.State.UNLOCKED
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.TransitionEvent.BufferUpdated
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.TransitionEvent.DecryptToggled
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.TransitionEvent.EditRequested
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.TransitionEvent.EncryptRequested
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.TransitionEvent.EncryptionCompleted
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.TransitionEvent.LockToggled
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.TransitionEvent.SendRequested
import org.fcitx.fcitx5.android.input.bar.ui.EncryptionCandidateUi
import org.fcitx.fcitx5.android.input.dependency.UniqueViewComponent
import org.fcitx.fcitx5.android.input.dependency.context
import org.fcitx.fcitx5.android.input.dependency.inputMethodService
import org.fcitx.fcitx5.android.input.dependency.theme
import org.secureime.sect9.crypto.codebook.CodebookEngine
import org.secureime.sect9.crypto.codebook.CodebookTable

class EncryptionBarComponent : UniqueViewComponent<EncryptionBarComponent, FrameLayout>() {

    private val context by manager.context()
    private val theme by manager.theme()
    private val service: FcitxInputMethodService by manager.inputMethodService()

    private val prefs = AppPrefs.getInstance().secure

    private val candidateUi by lazy { EncryptionCandidateUi(context, theme) }

    private val stateMachine = EncryptionBarStateMachine.new { state ->
        onStateChanged(state)
    }

    private var buffer = ""

    private var isDecryptMode = false

    val isEncrypting: Boolean
        get() = stateMachine.currentState == LOCKED || stateMachine.currentState == ENCRYPTING

    private fun onStateChanged(state: EncryptionBarStateMachine.State) {
        when (state) {
            UNLOCKED -> {
                view.visibility = View.GONE
                candidateUi.clearContent()
                buffer = ""
                prefs.lockState.setValue("UNLOCKED")
                prefs.bufferContent.setValue("")
            }
            LOCKED -> {
                view.visibility = View.VISIBLE
                candidateUi.setEncryptMode()
                candidateUi.clearContent()
                buffer = ""
                prefs.lockState.setValue("LOCKED")
            }
            ENCRYPTING -> {
                candidateUi.setEncryptMode()
                prefs.lockState.setValue("ENCRYPTING")
                doEncrypt()
            }
            ENCRYPTED -> {
                candidateUi.setEncryptMode()
                prefs.lockState.setValue("ENCRYPTED")
            }
        }
    }

    private fun doEncrypt() {
        if (buffer.isEmpty()) return
        val indexNumber = prefs.codebookId.getValue().let { key ->
            if (key.length == 4) key else "0000"
        }
        val table = loadDefaultCodebook()
        val engine = CodebookEngine(table)
        val ciphertext = engine.encrypt(buffer, indexNumber)
        candidateUi.updateContent(ciphertext)
        prefs.bufferContent.setValue(ciphertext)
        stateMachine.push(EncryptionCompleted)
    }

    fun appendToBuffer(text: String) {
        if (!isEncrypting) return
        buffer += text
        candidateUi.updateContent(buffer)
        stateMachine.push(BufferUpdated, BufferEmpty to (buffer.isEmpty()))
        prefs.bufferContent.setValue(buffer)
    }

    fun deleteFromBuffer() {
        if (buffer.isEmpty()) return
        buffer = buffer.dropLast(1)
        candidateUi.updateContent(buffer)
        stateMachine.push(BufferUpdated, BufferEmpty to (buffer.isEmpty()))
        prefs.bufferContent.setValue(buffer)
    }

    fun clearBuffer() {
        buffer = ""
        candidateUi.clearContent()
        stateMachine.push(BufferUpdated, BufferEmpty to true)
        prefs.bufferContent.setValue("")
    }

    fun toggleEncryptLock() {
        stateMachine.push(LockToggled)
    }

    fun toggleDecryptLock() {
        isDecryptMode = true
        stateMachine.push(DecryptToggled)
        if (stateMachine.currentState == LOCKED) {
            candidateUi.setDecryptMode()
        }
    }

    fun requestEncrypt() {
        stateMachine.push(EncryptRequested)
    }

    fun requestSend() {
        val text = candidateUi.bufferBar.previewText.text?.toString() ?: return
        if (text.isNotEmpty()) {
            service.commitText(text)
        }
        stateMachine.push(SendRequested)
    }

    fun requestEdit() {
        stateMachine.push(EditRequested)
        buffer = prefs.bufferContent.getValue()
        candidateUi.updateContent(buffer)
        candidateUi.setEncryptMode()
    }

    val currentState: EncryptionBarStateMachine.State
        get() = stateMachine.currentState

    override val view by lazy {
        FrameLayout(context).apply {
            visibility = View.GONE
            addView(candidateUi.root, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            candidateUi.bufferBar.closeButton.setOnClickListener {
                clearBuffer()
            }
            candidateUi.bufferBar.root.setOnClickListener {
                when (currentState) {
                    LOCKED -> requestEncrypt()
                    ENCRYPTED -> requestSend()
                    else -> {}
                }
            }
        }
    }

    private fun restoreState() {
        val saved = prefs.lockState.getValue()
        val state = try {
            EncryptionBarStateMachine.State.valueOf(saved)
        } catch (_: IllegalArgumentException) {
            UNLOCKED
        }
        if (state != UNLOCKED) {
            stateMachine.push(LockToggled)
            val content = prefs.bufferContent.getValue()
            if (content.isNotEmpty()) {
                buffer = content
                candidateUi.updateContent(content)
            }
        }
    }

    companion object {
        const val HEIGHT = 60

        private var cachedTable: CodebookTable? = null

        fun loadDefaultCodebook(): CodebookTable {
            cachedTable?.let { return it }
            val pages = mutableListOf<List<String>>()
            val charToCoord = mutableMapOf<String, List<Int>>()
            // Placeholder: 149 pages x 100 chars, each char maps to itself
            // Real codebook will be loaded from assets in Phase 5
            for (p in 0 until 149) {
                val page = mutableListOf<String>()
                for (i in 0 until 100) {
                    val ch = (0x4E00 + p * 100 + i).toChar().toString()
                    page.add(ch)
                    charToCoord[ch] = listOf(p, i)
                }
                pages.add(page)
            }
            val table = CodebookTable.fromData(pages, charToCoord)
            cachedTable = table
            return table
        }
    }
}