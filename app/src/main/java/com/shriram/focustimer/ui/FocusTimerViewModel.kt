package com.shriram.focustimer.ui

import android.app.Application
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shriram.focustimer.data.PomodoroPreferences
import com.shriram.focustimer.data.PomodoroPreferencesRepository
import com.shriram.focustimer.model.PomodoroMode
import com.shriram.focustimer.model.PomodoroSettings
import com.shriram.focustimer.model.PomodoroStats
import com.shriram.focustimer.model.TimerState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * One-time events emitted by the FocusTimerViewModel.
 */
sealed interface FocusTimerEvent {
    data class TimerFinished(
        val completedMode: PomodoroMode,
        val message: String
    ) : FocusTimerEvent
}

/**
 * UI State representing the complete status of the Focus Timer.
 */
data class FocusTimerUiState(
    val currentMode: PomodoroMode = PomodoroMode.FOCUS,
    val timerState: TimerState = TimerState.IDLE,
    val timeLeftSeconds: Long = PomodoroMode.FOCUS.defaultDurationSeconds,
    val totalDurationSeconds: Long = PomodoroMode.FOCUS.defaultDurationSeconds,
    val stats: PomodoroStats = PomodoroStats(),
    val settings: PomodoroSettings = PomodoroSettings(),
    val cycleCount: Int = 0,
) {
    val progress: Float
        get() = if (totalDurationSeconds > 0) {
            (totalDurationSeconds - timeLeftSeconds).toFloat() / totalDurationSeconds
        } else 0f

    val formattedTime: String
        get() {
            val minutes = timeLeftSeconds / 60
            val seconds = timeLeftSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val isRunning: Boolean get() = timerState == TimerState.RUNNING
    val isPaused: Boolean get() = timerState == TimerState.PAUSED
    val isIdle: Boolean get() = timerState == TimerState.IDLE
}

/**
 * Maps a StateFlow property with immediate synchronous value access and reactive updates.
 */
@OptIn(InternalCoroutinesApi::class)
private fun <T> StateFlow<FocusTimerUiState>.mapState(transform: (FocusTimerUiState) -> T): StateFlow<T> {
    return object : StateFlow<T> {
        override val value: T
            get() = transform(this@mapState.value)
        override val replayCache: List<T>
            get() = listOf(value)
        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            this@mapState
                .map { transform(it) }
                .distinctUntilChanged()
                .collect(collector)
            awaitCancellation()
        }
    }
}

/**
 * ViewModel managing Pomodoro countdown timer, state (idle, running, paused),
 * audio trigger via RingtoneManager, and session stats.
 */
class FocusTimerViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: PomodoroPreferencesRepository = PomodoroPreferences(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FocusTimerUiState())
    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()

    private val _events = Channel<FocusTimerEvent>(Channel.BUFFERED)
    val events: Flow<FocusTimerEvent> = _events.receiveAsFlow()

    val countdown: StateFlow<Long> = _uiState.mapState { it.timeLeftSeconds }
    val timerState: StateFlow<TimerState> = _uiState.mapState { it.timerState }
    val currentMode: StateFlow<PomodoroMode> = _uiState.mapState { it.currentMode }
    val stats: StateFlow<PomodoroStats> = _uiState.mapState { it.stats }
    val settings: StateFlow<PomodoroSettings> = _uiState.mapState { it.settings }

    private var timerJob: Job? = null

    /**
     * Optional custom audio trigger callback, useful for unit testing or custom playback.
     */
    var onAudioTrigger: (() -> Unit)? = null

    init {
        // Collect saved settings and update timer if idle
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collect { newSettings ->
                _uiState.update { currentState ->
                    val modeDurationMinutes = getModeDurationMinutes(currentState.currentMode, newSettings)
                    val modeDurationSeconds = modeDurationMinutes * 60L
                    if (currentState.timerState == TimerState.IDLE) {
                        currentState.copy(
                            settings = newSettings,
                            timeLeftSeconds = modeDurationSeconds,
                            totalDurationSeconds = modeDurationSeconds
                        )
                    } else {
                        currentState.copy(settings = newSettings)
                    }
                }
            }
        }

        // Collect saved stats
        viewModelScope.launch {
            preferencesRepository.statsFlow.collect { newStats ->
                _uiState.update { currentState ->
                    currentState.copy(stats = newStats)
                }
            }
        }
    }

    /**
     * Start or resume the countdown timer.
     */
    fun startTimer() {
        if (_uiState.value.timerState == TimerState.RUNNING) return

        if (_uiState.value.timeLeftSeconds <= 0L) {
            _uiState.update { it.copy(timeLeftSeconds = it.totalDurationSeconds) }
        }

        _uiState.update { it.copy(timerState = TimerState.RUNNING) }
        startCountdownLoop()
    }

    /**
     * Pause the running countdown timer.
     */
    fun pauseTimer() {
        if (_uiState.value.timerState != TimerState.RUNNING) return
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(timerState = TimerState.PAUSED) }
    }

    /**
     * Reset the countdown timer to initial duration for the current mode.
     */
    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            it.copy(
                timerState = TimerState.IDLE,
                timeLeftSeconds = it.totalDurationSeconds
            )
        }
    }

    /**
     * Switch current Pomodoro mode (FOCUS, SHORT_BREAK, LONG_BREAK) and reset timer.
     */
    fun setMode(mode: PomodoroMode) {
        timerJob?.cancel()
        timerJob = null

        val durationMinutes = getModeDurationMinutes(mode, _uiState.value.settings)
        val durationSeconds = durationMinutes * 60L

        _uiState.update {
            it.copy(
                currentMode = mode,
                timerState = TimerState.IDLE,
                timeLeftSeconds = durationSeconds,
                totalDurationSeconds = durationSeconds
            )
        }
    }

    /**
     * Skip the current session and transition to the next mode.
     */
    fun skipSession() {
        timerJob?.cancel()
        timerJob = null
        transitionToNextMode(autoStartOverride = false)
    }

    /**
     * Triggers audio completion notification via Android RingtoneManager.
     */
    fun playAudioTrigger() {
        val customHandler = onAudioTrigger
        if (customHandler != null) {
            customHandler.invoke()
            return
        }

        try {
            val alertUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            alertUri?.let { uri ->
                val ringtone: Ringtone? = RingtoneManager.getRingtone(getApplication(), uri)
                ringtone?.play()
            }
        } catch (e: Throwable) {
            // Gracefully handled for environments where RingtoneManager / AudioService is absent
        }
    }

    private fun startCountdownLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.timeLeftSeconds > 0L) {
                delay(1000L)
                _uiState.update { state ->
                    val remaining = (state.timeLeftSeconds - 1L).coerceAtLeast(0L)
                    state.copy(timeLeftSeconds = remaining)
                }
            }
            if (_uiState.value.timeLeftSeconds <= 0L) {
                onTimerFinished()
            }
        }
    }

    private fun onTimerFinished() {
        timerJob?.cancel()
        timerJob = null

        val finishedMode = _uiState.value.currentMode
        val message = when (finishedMode) {
            PomodoroMode.FOCUS -> "Focus session completed! Great job."
            PomodoroMode.SHORT_BREAK -> "Short break ended! Ready to focus?"
            PomodoroMode.LONG_BREAK -> "Long break ended! Ready to focus?"
        }
        _events.trySend(FocusTimerEvent.TimerFinished(finishedMode, message))

        // Trigger completion sound if enabled in settings
        if (_uiState.value.settings.soundEnabled) {
            playAudioTrigger()
        }

        val isFocus = finishedMode == PomodoroMode.FOCUS
        if (isFocus) {
            val sessionSeconds = _uiState.value.totalDurationSeconds
            viewModelScope.launch {
                preferencesRepository.incrementCompletedCount(sessionSeconds)
            }
        }

        transitionToNextMode(autoStartOverride = null)
    }

    private fun transitionToNextMode(autoStartOverride: Boolean?) {
        val currentMode = _uiState.value.currentMode
        val isFocus = currentMode == PomodoroMode.FOCUS
        val currentCycles = _uiState.value.cycleCount
        val nextCycles = if (isFocus) currentCycles + 1 else currentCycles

        val nextMode = if (isFocus) {
            if (nextCycles > 0 && nextCycles % 4 == 0) {
                PomodoroMode.LONG_BREAK
            } else {
                PomodoroMode.SHORT_BREAK
            }
        } else {
            PomodoroMode.FOCUS
        }

        val nextDurationMinutes = getModeDurationMinutes(nextMode, _uiState.value.settings)
        val nextDurationSeconds = nextDurationMinutes * 60L

        val shouldAutoStart = autoStartOverride ?: if (nextMode == PomodoroMode.FOCUS) {
            _uiState.value.settings.autoStartFocus
        } else {
            _uiState.value.settings.autoStartBreaks
        }

        _uiState.update {
            it.copy(
                currentMode = nextMode,
                timerState = if (shouldAutoStart) TimerState.RUNNING else TimerState.IDLE,
                timeLeftSeconds = nextDurationSeconds,
                totalDurationSeconds = nextDurationSeconds,
                cycleCount = nextCycles
            )
        }

        if (shouldAutoStart) {
            startCountdownLoop()
        }
    }

    private fun getModeDurationMinutes(mode: PomodoroMode, settings: PomodoroSettings): Int {
        return when (mode) {
            PomodoroMode.FOCUS -> settings.focusDurationMinutes
            PomodoroMode.SHORT_BREAK -> settings.shortBreakDurationMinutes
            PomodoroMode.LONG_BREAK -> settings.longBreakDurationMinutes
        }
    }

    // Settings actions
    fun updateFocusDuration(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setFocusDuration(minutes)
        }
    }

    fun updateShortBreakDuration(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setShortBreakDuration(minutes)
        }
    }

    fun updateLongBreakDuration(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setLongBreakDuration(minutes)
        }
    }

    fun updateAutoStart(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoStart(enabled)
        }
    }

    fun updateAutoStartBreaks(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoStartBreaks(enabled)
        }
    }

    fun updateAutoStartFocus(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoStartFocus(enabled)
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(enabled)
        }
    }

    fun saveSettings(settings: PomodoroSettings) {
        viewModelScope.launch {
            preferencesRepository.saveSettings(settings)
        }
    }

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            preferencesRepository.saveSettings(PomodoroSettings())
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            preferencesRepository.resetStats()
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        timerJob = null
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FocusTimerViewModel(application) as T
            }
        }
    }
}
