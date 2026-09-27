package com.train2send.domain.timer

import android.os.SystemClock
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

private sealed class PhaseSpec {
    abstract val durationSec: Int

    data class Prepare(
        override val durationSec: Int
    ) : PhaseSpec()

    data class Running(
        override val durationSec: Int,
        val currentSet: Int,
        val totalSets: Int,
        val isWorkPhase: Boolean,
        val currentRep: Int?,
        val totalReps: Int?
    ) : PhaseSpec()
}

class FlexibleTimerEngine {

    private var job: Job? = null
    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _isPaused = MutableStateFlow(false)

    private val _soundEvents = MutableSharedFlow<SoundEvent>(extraBufferCapacity = 10)
    val soundEvents: SharedFlow<SoundEvent> = _soundEvents.asSharedFlow()

    private val skipTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var totalDuration = 0

    fun startExerciseProtocol(
        scope: CoroutineScope,
        workSec: Int,
        restRepSec: Int,
        reps: Int,
        sets: Int,
        restSetSec: Int,
        prepareSec: Int = 3
    ) {
        job?.cancel()
        _isPaused.value = false

        val phases = mutableListOf<PhaseSpec>()
        if (prepareSec > 0) {
            phases.add(PhaseSpec.Prepare(prepareSec))
        }

        for (set in 1..sets) {
            for (rep in 1..reps) {
                if (workSec > 0) {
                    phases.add(
                        PhaseSpec.Running(
                            durationSec = workSec,
                            currentSet = set,
                            totalSets = sets,
                            isWorkPhase = true,
                            currentRep = rep,
                            totalReps = reps
                        )
                    )
                }
                if (rep < reps && restRepSec > 0) {
                    phases.add(
                        PhaseSpec.Running(
                            durationSec = restRepSec,
                            currentSet = set,
                            totalSets = sets,
                            isWorkPhase = false,
                            currentRep = rep,
                            totalReps = reps
                        )
                    )
                }
            }
            if (set < sets && restSetSec > 0) {
                phases.add(
                    PhaseSpec.Running(
                        durationSec = restSetSec,
                        currentSet = set,
                        totalSets = sets,
                        isWorkPhase = false,
                        currentRep = null,
                        totalReps = null
                    )
                )
            }
        }

        if (phases.isEmpty()) {
            _state.value = TimerState.Finished
            return
        }

        totalDuration = calculateTotalDuration(prepareSec, workSec, restRepSec, reps, sets, restSetSec)

        job = scope.launch {
            runProtocol(phases)
        }
    }

    fun calculateTotalDuration(
        prepareSec: Int,
        workSec: Int,
        restRepSec: Int,
        reps: Int,
        sets: Int,
        restSetSec: Int
    ): Int {
        val workPerSet = reps * workSec + (if (reps > 1) (reps - 1) * restRepSec else 0)
        return prepareSec + sets * workPerSet + (if (sets > 1) (sets - 1) * restSetSec else 0)
    }

    private suspend fun runProtocol(phases: List<PhaseSpec>) {
        var phaseIndex = 0
        var completedPhasesDuration = 0

        var phaseStartTime = SystemClock.elapsedRealtime()
        var pausedAccumulatedMs = 0L
        var pauseStartTime = 0L
        var wasPaused = false
        var lastBeepedSecond: Int? = null
        var lastLoopTickRealtime = SystemClock.elapsedRealtime()

        while (phaseIndex < phases.size) {
            val currentPhase = phases[phaseIndex]
            val durationSec = currentPhase.durationSec

            if (_isPaused.value) {
                if (!wasPaused) {
                    wasPaused = true
                    pauseStartTime = SystemClock.elapsedRealtime()
                }
                val elapsedMsInPhase = pauseStartTime - phaseStartTime - pausedAccumulatedMs
                val elapsedSecInPhase = (elapsedMsInPhase / 1000).toInt().coerceIn(0, durationSec)
                val remainingSec = (durationSec - elapsedSecInPhase).coerceAtLeast(1)
                val totalElapsed = completedPhasesDuration + elapsedSecInPhase

                _state.value = createTimerState(
                    spec = currentPhase,
                    remainingSec = remainingSec,
                    isPaused = true,
                    totalElapsed = totalElapsed,
                    totalDuration = totalDuration
                )

                val skipped = withTimeoutOrNull(200L) {
                    skipTrigger.first()
                    true
                } ?: false

                if (skipped) {
                    _isPaused.value = false
                    wasPaused = false
                    totalDuration -= remainingSec
                    completedPhasesDuration += elapsedSecInPhase
                    phaseIndex++
                    phaseStartTime = SystemClock.elapsedRealtime()
                    pausedAccumulatedMs = 0L
                    lastBeepedSecond = null
                }
                continue
            }

            if (wasPaused) {
                wasPaused = false
                pausedAccumulatedMs += (SystemClock.elapsedRealtime() - pauseStartTime)
            }

            val now = SystemClock.elapsedRealtime()
            val loopInterval = now - lastLoopTickRealtime
            lastLoopTickRealtime = now

            val elapsedMsInPhase = now - phaseStartTime - pausedAccumulatedMs
            val elapsedSecInPhase = (elapsedMsInPhase / 1000).toInt()

            if (elapsedSecInPhase >= durationSec) {
                val overflowMs = elapsedMsInPhase - (durationSec * 1000L)
                completedPhasesDuration += durationSec
                phaseIndex++
                if (phaseIndex < phases.size) {
                    phaseStartTime = now - overflowMs
                    pausedAccumulatedMs = 0L
                    lastBeepedSecond = null
                }
                continue
            }

            val remainingSec = durationSec - elapsedSecInPhase
            val totalElapsed = completedPhasesDuration + elapsedSecInPhase

            _state.value = createTimerState(
                spec = currentPhase,
                remainingSec = remainingSec,
                isPaused = false,
                totalElapsed = totalElapsed,
                totalDuration = totalDuration
            )

            if (lastBeepedSecond != remainingSec) {
                lastBeepedSecond = remainingSec
                // Only trigger beep if engine was awake recently (<1500ms) to prevent beep storm on background wake
                if (loopInterval < 1500L) {
                    if (currentPhase is PhaseSpec.Prepare) {
                        _soundEvents.emit(SoundEvent.BEEP)
                    } else if (currentPhase is PhaseSpec.Running) {
                        if (remainingSec <= 3) {
                            _soundEvents.emit(SoundEvent.BEEP)
                        }
                    }
                }
            }

            val skipped = withTimeoutOrNull(100L) {
                skipTrigger.first()
                true
            } ?: false

            if (skipped) {
                totalDuration -= remainingSec
                completedPhasesDuration += elapsedSecInPhase
                phaseIndex++
                phaseStartTime = SystemClock.elapsedRealtime()
                pausedAccumulatedMs = 0L
                lastBeepedSecond = null
            }
        }

        _soundEvents.emit(SoundEvent.DOUBLE_BEEP)
        _state.value = TimerState.Finished
    }

    private fun createTimerState(
        spec: PhaseSpec,
        remainingSec: Int,
        isPaused: Boolean,
        totalElapsed: Int,
        totalDuration: Int
    ): TimerState {
        return when (spec) {
            is PhaseSpec.Prepare -> TimerState.Preparing(
                remainingSeconds = remainingSec,
                isPaused = isPaused,
                totalElapsedSeconds = totalElapsed,
                totalDurationSeconds = totalDuration
            )
            is PhaseSpec.Running -> TimerState.Running(
                remainingSeconds = remainingSec,
                currentSet = spec.currentSet,
                totalSets = spec.totalSets,
                isWorkPhase = spec.isWorkPhase,
                currentRep = spec.currentRep,
                totalReps = spec.totalReps,
                isPaused = isPaused,
                totalElapsedSeconds = totalElapsed,
                totalDurationSeconds = totalDuration
            )
        }
    }

    fun pause() {
        _isPaused.value = true
    }

    fun resume() {
        _isPaused.value = false
    }

    fun next() {
        if (_isPaused.value) resume()
        skipTrigger.tryEmit(Unit)
    }

    fun stop() {
        job?.cancel()
        _isPaused.value = false
        _state.value = TimerState.Idle
    }
}
