/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.bar

import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.BooleanKey.BufferEmpty
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.State.ENCRYPTED
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.State.ENCRYPTING
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.State.LOCKED
import org.fcitx.fcitx5.android.input.bar.EncryptionBarStateMachine.State.UNLOCKED
import org.fcitx.fcitx5.android.utils.BuildTransitionEvent
import org.fcitx.fcitx5.android.utils.EventStateMachine
import org.fcitx.fcitx5.android.utils.TransitionBuildBlock

object EncryptionBarStateMachine {
    enum class State {
        UNLOCKED, LOCKED, ENCRYPTING, ENCRYPTED
    }

    enum class BooleanKey : EventStateMachine.BooleanStateKey {
        BufferEmpty
    }

    enum class TransitionEvent(val builder: TransitionBuildBlock<State, BooleanKey>) :
        EventStateMachine.TransitionEvent<State, BooleanKey> by BuildTransitionEvent(builder) {
        LockToggled({
            from(UNLOCKED) transitTo LOCKED
            from(LOCKED) transitTo UNLOCKED
        }),
        DecryptToggled({
            from(UNLOCKED) transitTo LOCKED
            from(ENCRYPTED) transitTo LOCKED
        }),
        EncryptRequested({
            from(LOCKED) transitTo ENCRYPTING onF {
                it(BufferEmpty) == false
            }
        }),
        EncryptionCompleted({
            from(ENCRYPTING) transitTo ENCRYPTED
        }),
        SendRequested({
            from(ENCRYPTED) transitTo UNLOCKED
        }),
        EditRequested({
            from(ENCRYPTED) transitTo LOCKED
        }),
        BufferUpdated({
            // no state change on buffer update — just tracks the boolean key
        }),
    }

    fun new(block: (State) -> Unit) =
        EventStateMachine<State, TransitionEvent, BooleanKey>(
            initialState = UNLOCKED,
            externalBooleanStates = mutableMapOf(
                BufferEmpty to true
            )
        ).apply {
            onNewStateListener = block
        }

}