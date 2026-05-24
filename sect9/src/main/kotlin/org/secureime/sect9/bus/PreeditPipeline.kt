package org.secureime.sect9.bus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PreeditState(
    val rawInput: String = "",
    val decodedDisplay: String = "",
    val auxiliary: List<String> = emptyList(),
    val fcitxPreedit: String = ""
)

class PreeditPipeline {
    private val _state = MutableStateFlow(PreeditState())
    val state: StateFlow<PreeditState> = _state.asStateFlow()

    fun publish(state: PreeditState) {
        _state.value = state
    }

    fun clear() {
        _state.value = PreeditState()
    }
}