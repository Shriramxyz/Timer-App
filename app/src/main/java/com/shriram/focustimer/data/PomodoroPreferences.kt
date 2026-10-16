package com.shriram.focustimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shriram.focustimer.model.PomodoroMode
import com.shriram.focustimer.model.PomodoroSettings
import com.shriram.focustimer.model.PomodoroStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.pomodoroDataStore: DataStore<Preferences> by preferencesDataStore(name = "pomodoro_preferences")

interface PomodoroPreferencesRepository {
    val settingsFlow: Flow<PomodoroSettings>
    val statsFlow: Flow<PomodoroStats>
    suspend fun saveSettings(settings: PomodoroSettings)
    suspend fun setFocusDuration(minutes: Int)
    suspend fun setShortBreakDuration(minutes: Int)
    suspend fun setLongBreakDuration(minutes: Int)
    suspend fun setAutoStart(enabled: Boolean)
    suspend fun setAutoStartBreaks(enabled: Boolean)
    suspend fun setAutoStartFocus(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun incrementCompletedCount(focusDurationSeconds: Long)
    suspend fun resetStats()
}

/**
 * DataStore-backed repository for saving settings (durations, auto-start)
 * and stats (completed count, total focus time).
 */
class PomodoroPreferences(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.pomodoroDataStore,
) : PomodoroPreferencesRepository {

    companion object {
        val KEY_FOCUS_DURATION = intPreferencesKey("focus_duration_minutes")
        val KEY_SHORT_BREAK_DURATION = intPreferencesKey("short_break_duration_minutes")
        val KEY_LONG_BREAK_DURATION = intPreferencesKey("long_break_duration_minutes")
        val KEY_AUTO_START = booleanPreferencesKey("auto_start")
        val KEY_AUTO_START_BREAKS = booleanPreferencesKey("auto_start_breaks")
        val KEY_AUTO_START_FOCUS = booleanPreferencesKey("auto_start_focus")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_COMPLETED_COUNT = intPreferencesKey("completed_count")
        val KEY_TOTAL_FOCUS_TIME = longPreferencesKey("total_focus_time_seconds")
    }

    override val settingsFlow: Flow<PomodoroSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val autoStart = preferences[KEY_AUTO_START] ?: false
            PomodoroSettings(
                focusDurationMinutes = preferences[KEY_FOCUS_DURATION] ?: PomodoroMode.FOCUS.defaultDurationMinutes,
                shortBreakDurationMinutes = preferences[KEY_SHORT_BREAK_DURATION] ?: PomodoroMode.SHORT_BREAK.defaultDurationMinutes,
                longBreakDurationMinutes = preferences[KEY_LONG_BREAK_DURATION] ?: PomodoroMode.LONG_BREAK.defaultDurationMinutes,
                autoStart = autoStart,
                autoStartBreaks = preferences[KEY_AUTO_START_BREAKS] ?: autoStart,
                autoStartFocus = preferences[KEY_AUTO_START_FOCUS] ?: autoStart,
                soundEnabled = preferences[KEY_SOUND_ENABLED] ?: true
            )
        }

    override val statsFlow: Flow<PomodoroStats> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            PomodoroStats(
                completedCount = preferences[KEY_COMPLETED_COUNT] ?: 0,
                totalFocusTimeSeconds = preferences[KEY_TOTAL_FOCUS_TIME] ?: 0L
            )
        }

    override suspend fun saveSettings(settings: PomodoroSettings) {
        dataStore.edit { preferences ->
            preferences[KEY_FOCUS_DURATION] = settings.focusDurationMinutes
            preferences[KEY_SHORT_BREAK_DURATION] = settings.shortBreakDurationMinutes
            preferences[KEY_LONG_BREAK_DURATION] = settings.longBreakDurationMinutes
            preferences[KEY_AUTO_START] = settings.autoStart
            preferences[KEY_AUTO_START_BREAKS] = settings.autoStartBreaks
            preferences[KEY_AUTO_START_FOCUS] = settings.autoStartFocus
            preferences[KEY_SOUND_ENABLED] = settings.soundEnabled
        }
    }

    override suspend fun setFocusDuration(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_FOCUS_DURATION] = minutes
        }
    }

    override suspend fun setShortBreakDuration(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_SHORT_BREAK_DURATION] = minutes
        }
    }

    override suspend fun setLongBreakDuration(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_LONG_BREAK_DURATION] = minutes
        }
    }

    override suspend fun setAutoStart(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_START] = enabled
            preferences[KEY_AUTO_START_BREAKS] = enabled
            preferences[KEY_AUTO_START_FOCUS] = enabled
        }
    }

    override suspend fun setAutoStartBreaks(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_START_BREAKS] = enabled
        }
    }

    override suspend fun setAutoStartFocus(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_START_FOCUS] = enabled
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SOUND_ENABLED] = enabled
        }
    }

    override suspend fun incrementCompletedCount(focusDurationSeconds: Long) {
        dataStore.edit { preferences ->
            val currentCount = preferences[KEY_COMPLETED_COUNT] ?: 0
            val currentTotalTime = preferences[KEY_TOTAL_FOCUS_TIME] ?: 0L
            preferences[KEY_COMPLETED_COUNT] = currentCount + 1
            preferences[KEY_TOTAL_FOCUS_TIME] = currentTotalTime + focusDurationSeconds
        }
    }

    override suspend fun resetStats() {
        dataStore.edit { preferences ->
            preferences[KEY_COMPLETED_COUNT] = 0
            preferences[KEY_TOTAL_FOCUS_TIME] = 0L
        }
    }
}
