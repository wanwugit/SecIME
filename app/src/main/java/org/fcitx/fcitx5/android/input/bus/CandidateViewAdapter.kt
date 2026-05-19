package org.fcitx.fcitx5.android.input.bus

import org.fcitx.fcitx5.android.input.SecLogger
import org.secureime.sect9.bus.CandidatePipeline
import org.secureime.sect9.bus.CandidateState
import org.fcitx.fcitx5.android.input.candidates.horizontal.HorizontalCandidateComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CandidateViewAdapter(
    private val pipeline: CandidatePipeline,
    private val horizontalCandidate: HorizontalCandidateComponent
) {
    fun startCollecting(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            pipeline.state.collect { state ->
                SecLogger.d("CandAdapter", "collect: candidates.size=${state.candidates.size}, total=${state.total}")
                if (state.candidates.isEmpty()) {
                    horizontalCandidate.view.visibility = android.view.View.GONE
                } else {
                    horizontalCandidate.updateFromPipeline(state.candidates.toTypedArray(), state.total)
                    horizontalCandidate.view.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}