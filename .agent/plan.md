# Project Plan

Create a "Pomodoro"-style Focus Timer application using Jetpack Compose with custom intervals, auto-start settings, session tracking, and tomato motif.

## Project Brief

# Focus Timer - Project Brief

A minimalist, engaging "Pomodoro"-style focus timer application built for Android with Jetpack Compose, featuring customizable intervals, session automation, productivity tracking, and a warm tomato motif.

---

## Features

1. **Tomato-Themed Pomodoro Timer**: Visual circular countdown timer with custom tomato motif and warm light-red styling, supporting standard cycles (Focus, Short Break, Long Break) with start, pause, skip, and reset actions.
2. **Customizable Durations & Auto-Start**: Configurable interval lengths for focus and break sessions, with toggleable options to automatically advance and start next sessions.
3. **Session Tracking & Statistics**: Real-time tracking of completed pomodoro cycles, current cycle progress, total focused minutes, and session history summaries.
4. **Auditory & Haptic Feedback**: Sensory cues and haptic alerts notifying users when focus and break intervals transition.

---

## High-Level Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3 (custom tomato/light-red color palette)
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven navigation) and Compose Material Adaptive library for responsive screen layouts
- **Concurrency & State Management**: Kotlin Coroutines & StateFlow for precise timer ticks and reactive state handling
- **Architecture**: MVVM with Jetpack ViewModel and Unidirectional Data Flow (UDF)

---

## UI Design Image

![UI Design](C:/Users/HP/AndroidStudioProjects/FocusTimer/input_images/pomodoro_timer_ui_mockup.jpg)
Image path = C:/Users/HP/AndroidStudioProjects/FocusTimer/input_images/pomodoro_timer_ui_mockup.jpg

## Implementation Steps

### Task_1_DataAndTimerEngine: Implement core data models, preferences for timer settings and stats, and the Pomodoro timer engine using Coroutines/StateFlow supporting focus and break cycles, auto-start, and auditory/haptic alerts.
- **Status:** COMPLETED
- **Updates:** Fatal crash resolved. Added @JvmOverloads and explicit FocusTimerViewModel.Factory in MainActivity. All unit tests passed and assembleDebug built successfully.
- **Acceptance Criteria:**
  - Timer state management and models for Focus, Short Break, and Long Break cycles
  - Coroutine timer engine supporting start, pause, skip, reset, customizable durations, and auto-start
  - Sound and haptic feedback triggers on interval transition
  - Project builds successfully

### Task_2_TimerUIAndControls: Build the main Pomodoro screen in Jetpack Compose featuring a warm tomato theme, segmented mode selection, circular progress countdown display, cycle indicators, and playback controls.
- **Status:** COMPLETED
- **Updates:** Task_2_TimerUIAndControls completed successfully. Implemented warm tomato Material 3 theme in Color.kt and Theme.kt, created PomodoroModeSelector with Segmented Buttons for Focus/Short Break/Long Break, CircularTimerDisplay with 14dp stroke and smooth progress animation, bold MM:SS countdown and 4-dot cycle indicator, large Play/Pause FloatingActionButton with secondary Reset and Skip buttons, Scaffold with SnackbarHost for completion alerts and RingtoneManager audio trigger. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - Warm tomato / light-red theme and styling applied
  - Segmented mode selector for Focus, Short Break, and Long Break
  - Circular countdown timer progress and timer control buttons (Start/Pause, Skip, Reset)
  - Integration with timer ViewModel and reactive state flow
  - The implemented UI must match the design provided in C:/Users/HP/AndroidStudioProjects/FocusTimer/input_images/pomodoro_timer_ui_mockup.jpg

### Task_3_SettingsAndStats: Implement settings and productivity statistics screens/dialogs for customizing interval lengths, auto-advance options, notifications, and tracking completed pomodoro sessions.
- **Status:** COMPLETED
- **Updates:** Task_3_SettingsAndStats completed successfully. Implemented TopAppBar actions for Statistics and Settings, SettingsSheet with interactive duration sliders/steppers (5-60m focus, 1-15m short break, 5-30m long break) and switches for auto-start breaks, auto-start focus, and sound notifications. Implemented StatisticsSheet with daily goal progress, completed pomodoros, total focus time, streak cycle insights, and reset statistics dialog. Ensured responsive layout with width constraints for large screens and tablets. All unit tests passed and assembleDebug built successfully.
- **Acceptance Criteria:**
  - Customizable interval duration settings and auto-start preferences persisted
  - Session tracking and statistics display showing completed pomodoros and total focus minutes
  - Snackbar and cues on session completion
  - The implemented UI must match the design provided in C:/Users/HP/AndroidStudioProjects/FocusTimer/input_images/pomodoro_timer_ui_mockup.jpg

### Task_4_RunAndVerify: Run and verify the complete Focus Timer app, ensuring timer accuracy, interval transitions, audio/haptic feedback, and compliance with user requirements and UI design.
- **Status:** IN_PROGRESS
- **Updates:** App crashed on launch due to NoSuchMethodException in FocusTimerViewModel constructor reflection. Reopening for fix.
- **Acceptance Criteria:**
  - Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - The implemented UI must match the design provided in C:/Users/HP/AndroidStudioProjects/FocusTimer/input_images/pomodoro_timer_ui_mockup.jpg
- **StartTime:** 2026-10-17 00:58:49 IST

