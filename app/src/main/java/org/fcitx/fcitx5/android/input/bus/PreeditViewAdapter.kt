package org.fcitx.fcitx5.android.input.bus

import org.secureime.sect9.bus.PreeditPipeline
import org.secureime.sect9.bus.PreeditState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreeditViewAdapter(
    private val pipeline: PreeditPipeline
) {
    var onPreeditUpdate: ((PreeditState) -> Unit)? = null

    fun startCollecting(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            pipeline.state.collect { state ->
                onPreeditUpdate?.invoke(state)
            }
        }
    }
}