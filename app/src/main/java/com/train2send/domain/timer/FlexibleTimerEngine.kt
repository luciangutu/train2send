package com.train2send.domain.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlexibleTimerEngine {

    private var job: Job? = null
    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    fun startProtocol(
        scope: CoroutineScope,
        workSec: Int,
        restSec: Int,
        totalSets: Int
    ) {
        job?.cancel()
        job = scope.launch {
            for (set in 1..totalSets) {
                runPhase(workSec, set, totalSets, isWork = true)
                if (set < totalSets && restSec > 0) {
                    runPhase(restSec, set, totalSets, isWork = false)
                }
            }
            _state.value = TimerState.Finished
        }
    }

    private suspend fun runPhase(
        seconds: Int,
        currentSet: Int,
        totalSets: Int,
        isWork: Boolean
    ) {
        for (sec in seconds downTo 1) {
            _state.value = TimerState.Running(sec, currentSet, totalSets, isWork)
            delay(1000L)
        }
    }

    fun stop() {
        job?.cancel()
        _state.value = TimerState.Idle
    }

    fun reset() {
        job?.cancel()
        _state.value = TimerState.Idle
    }
}
