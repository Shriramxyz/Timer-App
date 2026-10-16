package com.shriram.focustimer.model

/**
 * Modes of the Pomodoro timer with default durations.
 * - FOCUS: 25 minutes
 * - SHORT_BREAK: 5 minutes
 * - LONG_BREAK: 15 minutes
 */
enum class PomodoroMode(
    val title: String,
    val defaultDurationMinutes: Int,
) {
    FOCUS("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15);

    val defaultDurationSeconds: Long
        get() = defaultDurationMinutes * 60L
}

/**
 * State representing current status of the countdown timer.
 */
enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED
}

/**
 * User-configurable settings for Pomodoro sessions.
 */
data class PomodoroSettings(
    val focusDurationMinutes: Int = PomodoroMode.FOCUS.defaultDurationMinutes,
    val shortBreakDurationMinutes: Int = PomodoroMode.SHORT_BREAK.defaultDurationMinutes,
    val longBreakDurationMinutes: Int = PomodoroMode.LONG_BREAK.defaultDurationMinutes,
    val autoStart: Boolean = false,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val soundEnabled: Boolean = true
)

/**
 * Usage statistics for completed sessions and focus time.
 */
data class PomodoroStats(
    val completedCount: Int = 0,
    val totalFocusTimeSeconds: Long = 0L
) {
    val totalFocusTime: Long
        get() = totalFocusTimeSeconds

    val totalFocusMinutes: Long
        get() = totalFocusTimeSeconds / 60L

    val formattedTotalTime: String
        get() {
            val hours = totalFocusTimeSeconds / 3600
            val minutes = (totalFocusTimeSeconds % 3600) / 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }

    val formattedDuration: String
        get() {
            val hours = totalFocusTimeSeconds / 3600
            val minutes = (totalFocusTimeSeconds % 3600) / 60
            return when {
                hours > 0 && minutes > 0 -> "$hours hrs $minutes mins"
                hours > 0 -> "$hours hrs"
                else -> "$minutes mins"
            }
        }
}

