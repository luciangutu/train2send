package com.train2send.domain.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class SoundEvent {
    BEEP,
    DOUBLE_BEEP
}

class FlexibleTimerEngine {

    private var job: Job? = null
    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _soundEvents = MutableSharedFlow<SoundEvent>(extraBufferCapacity = 10)
    val soundEvents: SharedFlow<SoundEvent> = _soundEvents.asSharedFlow()

    private val skipTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun startExerciseProtocol(
        scope: CoroutineScope,
        workSec: Int,
        restRepSec: Int,
        reps: Int,
        sets: Int,
        restSetSec: Int
    ) {
        job?.cancel()
        job = scope.launch {
            // Prepare Phase
            runPrepare(3)
            
            for (set in 1..sets) {
                for (rep in 1..reps) {
                    runPhase(
                        seconds = workSec,
                        currentSet = set,
                        totalSets = sets,
                        isWork = true,
                        currentRep = rep,
                        totalReps = reps
                    )
                    
                    if (rep < reps && restRepSec > 0) {
                        runPhase(
                            seconds = restRepSec,
                            currentSet = set,
                            totalSets = sets,
                            isWork = false,
                            currentRep = rep,
                            totalReps = reps
                        )
                    }
                }
                
                if (set < sets && restSetSec > 0) {
                    runPhase(
                        seconds = restSetSec,
                        currentSet = set,
                        totalSets = sets,
                        isWork = false,
                        currentRep = null,
                        totalReps = null
                    )
                }
            }
            _soundEvents.emit(SoundEvent.DOUBLE_BEEP)
            _state.value = TimerState.Finished
        }
    }

    private suspend fun runPrepare(seconds: Int) {
        for (sec in seconds downTo 1) {
            _state.value = TimerState.Preparing(sec)
            _soundEvents.emit(SoundEvent.BEEP)
            val skipped = withTimeoutOrNull(1000L) {
                skipTrigger.first()
                true
            } ?: false
            if (skipped) break
        }
    }

    private suspend fun runPhase(
        seconds: Int,
        currentSet: Int,
        totalSets: Int,
        isWork: Boolean,
        currentRep: Int? = null,
        totalReps: Int? = null
    ) {
        if (seconds <= 0) return
        for (sec in seconds downTo 1) {
            _state.value = TimerState.Running(sec, currentSet, totalSets, isWork, currentRep, totalReps)
            if (sec <= 3) {
                _soundEvents.emit(SoundEvent.BEEP)
            }
            val skipped = withTimeoutOrNull(1000L) {
                skipTrigger.first()
                true
            } ?: false
            if (skipped) break
        }
    }

    fun next() {
        skipTrigger.tryEmit(Unit)
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
