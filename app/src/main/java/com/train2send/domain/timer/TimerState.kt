package com.train2send.domain.timer

sealed class TimerState {
    data object Idle : TimerState()
    data class Preparing(val remainingSeconds: Int) : TimerState()
    data class Running(
        val remainingSeconds: Int,
        val currentSet: Int,
        val totalSets: Int,
        val isWorkPhase: Boolean,
        val currentRep: Int? = null,
        val totalReps: Int? = null
    ) : TimerState()
    data object Paused : TimerState()
    data object Finished : TimerState()
}
