/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.bar

import android.view.View
import android.widget.FrameLayout
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.theme.Theme
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
import org.fcitx.fcitx5.android.input.dependency.theme

class EncryptionBarComponent : UniqueViewComponent<EncryptionBarComponent, FrameLayout>() {

    private val context by manager.context()
    private val theme by manager.theme()

    private val prefs = AppPrefs.getInstance().secure

    private val candidateUi by lazy { EncryptionCandidateUi(context, theme) }

    private val stateMachine = EncryptionBarStateMachine.new { state ->
        onStateChanged(state)
    }

    private fun onStateChanged(state: EncryptionBarStateMachine.State) {
        when (state) {
            UNLOCKED -> {
                view.visibility = View.GONE
                candidateUi.clearContent()
                prefs.lockState.setValue("UNLOCKED")
            }
            LOCKED -> {
                view.visibility = View.VISIBLE
                candidateUi.setEncryptMode()
                candidateUi.clearContent()
                prefs.lockState.setValue("LOCKED")
            }
            ENCRYPTING -> {
                candidateUi.setEncryptMode()
                prefs.lockState.setValue("ENCRYPTING")
            }
            ENCRYPTED -> {
                candidateUi.setEncryptMode()
                prefs.lockState.setValue("ENCRYPTED")
            }
        }
    }

    fun toggleEncryptLock() {
        stateMachine.push(LockToggled)
    }

    fun toggleDecryptLock() {
        stateMachine.push(DecryptToggled)
        if (stateMachine.currentState == LOCKED) {
            candidateUi.setDecryptMode()
        }
    }

    fun onBufferContentChanged(text: String) {
        val empty = text.isEmpty()
        stateMachine.push(BufferUpdated, BufferEmpty to empty)
        candidateUi.updateContent(text)
        prefs.bufferContent.setValue(text)
    }

    fun requestEncrypt() {
        stateMachine.push(EncryptRequested)
    }

    fun requestSend() {
        stateMachine.push(SendRequested)
    }

    fun requestEdit() {
        stateMachine.push(EditRequested)
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
                candidateUi.updateContent(content)
            }
        }
    }

    companion object {
        const val HEIGHT = 40
    }
}