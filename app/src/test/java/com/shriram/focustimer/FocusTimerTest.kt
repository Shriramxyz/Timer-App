package com.shriram.focustimer

import android.app.Application
import com.shriram.focustimer.data.PomodoroPreferencesRepository
import com.shriram.focustimer.model.PomodoroMode
import com.shriram.focustimer.model.PomodoroSettings
import com.shriram.focustimer.model.PomodoroStats
import com.shriram.focustimer.model.TimerState
import com.shriram.focustimer.ui.FocusTimerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

class FakePomodoroPreferencesRepository : PomodoroPreferencesRepository {
    private val _settingsFlow = MutableStateFlow(PomodoroSettings())
    override val settingsFlow: Flow<PomodoroSettings> = _settingsFlow.asStateFlow()

    private val _statsFlow = MutableStateFlow(PomodoroStats())
    override val statsFlow: Flow<PomodoroStats> = _statsFlow.asStateFlow()
    val currentStats: PomodoroStats get() = _statsFlow.value

    override suspend fun saveSettings(settings: PomodoroSettings) {
        _settingsFlow.value = settings
    }

    override suspend fun setFocusDuration(minutes: Int) {
        _settingsFlow.value = _settingsFlow.value.copy(focusDurationMinutes = minutes)
    }

    override suspend fun setShortBreakDuration(minutes: Int) {
        _settingsFlow.value = _settingsFlow.value.copy(shortBreakDurationMinutes = minutes)
    }

    override suspend fun setLongBreakDuration(minutes: Int) {
        _settingsFlow.value = _settingsFlow.value.copy(longBreakDurationMinutes = minutes)
    }

    override suspend fun setAutoStart(enabled: Boolean) {
        _settingsFlow.value = _settingsFlow.value.copy(
            autoStart = enabled,
            autoStartBreaks = enabled,
            autoStartFocus = enabled
        )
    }

    override suspend fun setAutoStartBreaks(enabled: Boolean) {
        _settingsFlow.value = _settingsFlow.value.copy(autoStartBreaks = enabled)
    }

    override suspend fun setAutoStartFocus(enabled: Boolean) {
        _settingsFlow.value = _settingsFlow.value.copy(autoStartFocus = enabled)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        _settingsFlow.value = _settingsFlow.value.copy(soundEnabled = enabled)
    }

    override suspend fun incrementCompletedCount(focusDurationSeconds: Long) {
        val current = _statsFlow.value
        _statsFlow.value = current.copy(
            completedCount = current.completedCount + 1,
            totalFocusTimeSeconds = current.totalFocusTimeSeconds + focusDurationSeconds
        )
    }

    override suspend fun resetStats() {
        _statsFlow.value = PomodoroStats()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class FocusTimerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakePomodoroPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePomodoroPreferencesRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPomodoroMode_defaultDurations() {
        assertEquals(25, PomodoroMode.FOCUS.defaultDurationMinutes)
        assertEquals(1500L, PomodoroMode.FOCUS.defaultDurationSeconds)

        assertEquals(5, PomodoroMode.SHORT_BREAK.defaultDurationMinutes)
        assertEquals(300L, PomodoroMode.SHORT_BREAK.defaultDurationSeconds)

        assertEquals(15, PomodoroMode.LONG_BREAK.defaultDurationMinutes)
        assertEquals(900L, PomodoroMode.LONG_BREAK.defaultDurationSeconds)
    }

    @Test
    fun testViewModel_initialState() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PomodoroMode.FOCUS, viewModel.currentMode.value)
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
        assertEquals(1500L, viewModel.countdown.value)
        assertEquals(0, viewModel.stats.value.completedCount)
        assertEquals(0L, viewModel.stats.value.totalFocusTimeSeconds)
    }

    @Test
    fun testViewModel_timerStateTransitions() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Start timer
        viewModel.startTimer()
        assertEquals(TimerState.RUNNING, viewModel.timerState.value)

        // Advance 3 seconds
        testDispatcher.scheduler.advanceTimeBy(3000L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1497L, viewModel.countdown.value)

        // Pause timer
        viewModel.pauseTimer()
        assertEquals(TimerState.PAUSED, viewModel.timerState.value)

        // Advancing time should not change countdown while paused
        testDispatcher.scheduler.advanceTimeBy(3000L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1497L, viewModel.countdown.value)

        // Reset timer
        viewModel.resetTimer()
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
        assertEquals(1500L, viewModel.countdown.value)
    }

    @Test
    fun testViewModel_setMode() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setMode(PomodoroMode.SHORT_BREAK)
        assertEquals(PomodoroMode.SHORT_BREAK, viewModel.currentMode.value)
        assertEquals(300L, viewModel.countdown.value)

        viewModel.setMode(PomodoroMode.LONG_BREAK)
        assertEquals(PomodoroMode.LONG_BREAK, viewModel.currentMode.value)
        assertEquals(900L, viewModel.countdown.value)
    }

    @Test
    fun testViewModel_audioTriggerAndStatsOnCompletion() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set short duration for test (1 minute = 60s)
        viewModel.updateFocusDuration(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(60L, viewModel.countdown.value)

        var audioTriggered = false
        viewModel.onAudioTrigger = {
            audioTriggered = true
        }

        viewModel.startTimer()
        // Fast forward 60 seconds
        testDispatcher.scheduler.advanceTimeBy(60000L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Audio trigger should be called on completion", audioTriggered)
        assertEquals(1, fakeRepository.currentStats.completedCount)
        assertEquals(60L, fakeRepository.currentStats.totalFocusTimeSeconds)
        assertEquals(PomodoroMode.SHORT_BREAK, viewModel.currentMode.value)
    }

    @Test
    fun testViewModel_eventsEmittedOnSessionCompletion() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateFocusDuration(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val events = mutableListOf<com.shriram.focustimer.ui.FocusTimerEvent>()
        backgroundScope.launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.startTimer()
        testDispatcher.scheduler.advanceTimeBy(60000L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        val event = events.first() as com.shriram.focustimer.ui.FocusTimerEvent.TimerFinished
        assertEquals(PomodoroMode.FOCUS, event.completedMode)
        assertTrue(event.message.contains("Focus session completed"))
    }

    @Test
    fun testViewModel_soundDisabled_noAudioTrigger() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateFocusDuration(1)
        viewModel.updateSoundEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        var audioTriggered = false
        viewModel.onAudioTrigger = {
            audioTriggered = true
        }

        viewModel.startTimer()
        testDispatcher.scheduler.advanceTimeBy(60000L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse("Audio trigger should NOT be called when sound is disabled", audioTriggered)
        assertEquals(1, fakeRepository.currentStats.completedCount)
    }

    @Test
    fun testViewModel_saveSettingsUpdatesIdleTimer() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1500L, viewModel.countdown.value)

        // Save customized settings with 30-min focus
        viewModel.saveSettings(
            PomodoroSettings(
                focusDurationMinutes = 30,
                shortBreakDurationMinutes = 8,
                longBreakDurationMinutes = 20,
                autoStartBreaks = true,
                autoStartFocus = true,
                soundEnabled = true
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(30 * 60L, viewModel.countdown.value)
        assertEquals(30 * 60L, viewModel.uiState.value.totalDurationSeconds)
        assertEquals(30, viewModel.settings.value.focusDurationMinutes)
        assertEquals(8, viewModel.settings.value.shortBreakDurationMinutes)
        assertEquals(20, viewModel.settings.value.longBreakDurationMinutes)
        assertTrue(viewModel.settings.value.autoStartBreaks)
        assertTrue(viewModel.settings.value.autoStartFocus)
    }

    @Test
    fun testViewModel_resetSettingsToDefaults() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Change settings
        viewModel.updateFocusDuration(45)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(45, viewModel.settings.value.focusDurationMinutes)
        assertEquals(45 * 60L, viewModel.countdown.value)

        // Reset to defaults
        viewModel.resetSettingsToDefaults()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(25, viewModel.settings.value.focusDurationMinutes)
        assertEquals(25 * 60L, viewModel.countdown.value)
    }

    @Test
    fun testViewModel_resetStats() = runTest(testDispatcher) {
        val application = Application()
        val viewModel = FocusTimerViewModel(application, fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepository.incrementCompletedCount(1500L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.stats.value.completedCount)
        assertEquals(1500L, viewModel.stats.value.totalFocusTimeSeconds)

        viewModel.resetStats()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.stats.value.completedCount)
        assertEquals(0L, viewModel.stats.value.totalFocusTimeSeconds)
    }

    @Test
    fun testPomodoroStats_formatting() {
        val stats1 = PomodoroStats(completedCount = 2, totalFocusTimeSeconds = 3000L) // 50m
        assertEquals("50m", stats1.formattedTotalTime)
        assertEquals("50 mins", stats1.formattedDuration)

        val stats2 = PomodoroStats(completedCount = 4, totalFocusTimeSeconds = 7200L) // 2h
        assertEquals("2h 0m", stats2.formattedTotalTime)
        assertEquals("2 hrs", stats2.formattedDuration)

        val stats3 = PomodoroStats(completedCount = 5, totalFocusTimeSeconds = 8100L) // 2h 15m
        assertEquals("2h 15m", stats3.formattedTotalTime)
        assertEquals("2 hrs 15 mins", stats3.formattedDuration)
    }

    @Test
    fun testViewModel_applicationConstructorReflection() {
        // Verifies AndroidViewModelFactory reflection compatibility for FocusTimerViewModel(Application)
        val constructor = FocusTimerViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(constructor)

        val constructorWithRepo = FocusTimerViewModel::class.java.getConstructor(
            Application::class.java,
            PomodoroPreferencesRepository::class.java
        )
        assertNotNull(constructorWithRepo)
    }

    @Test
    fun testViewModel_factoryCreation() {
        val application = Application()
        val factory = FocusTimerViewModel.Factory(application)
        assertNotNull(factory)
    }
}


