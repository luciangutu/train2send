package com.train2send.domain.timer

sealed class TimerState {
    data object Idle : TimerState()
    data class Running(
        val remainingSeconds: Int,
        val currentSet: Int,
        val totalSets: Int,
        val isWorkPhase: Boolean
    ) : TimerState()
    data object Paused : TimerState()
    data object Finished : TimerState()
}
