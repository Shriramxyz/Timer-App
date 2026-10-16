package com.shriram.focustimer.ui

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shriram.focustimer.model.PomodoroMode
import com.shriram.focustimer.model.PomodoroStats
import com.shriram.focustimer.model.TimerState
import com.shriram.focustimer.ui.theme.FocusTimerTheme

/**
 * Stateful entry composable for the Focus Timer screen.
 */
@Composable
fun FocusTimerScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusTimerViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var showStatsSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FocusTimerEvent.TimerFinished -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    FocusTimerScreenContent(
        uiState = uiState,
        onStart = viewModel::startTimer,
        onPause = viewModel::pauseTimer,
        onReset = viewModel::resetTimer,
        onSkip = viewModel::skipSession,
        onModeSelected = viewModel::setMode,
        onOpenSettings = { showSettingsSheet = true },
        onOpenStats = { showStatsSheet = true },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )

    if (showSettingsSheet) {
        SettingsBottomSheet(
            currentSettings = uiState.settings,
            onDismiss = { showSettingsSheet = false },
            onSaveSettings = { updatedSettings ->
                viewModel.saveSettings(updatedSettings)
                showSettingsSheet = false
            },
            onResetToDefaults = {
                viewModel.resetSettingsToDefaults()
                showSettingsSheet = false
            }
        )
    }

    if (showStatsSheet) {
        StatisticsBottomSheet(
            stats = uiState.stats,
            onDismiss = { showStatsSheet = false },
            onResetStats = {
                viewModel.resetStats()
            }
        )
    }
}

/**
 * Stateless presentation composable for the Focus Timer screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreenContent(
    uiState: FocusTimerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onModeSelected: (PomodoroMode) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    onOpenStats: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "Focus Timer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenStats) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Segmented Mode Selector
                PomodoroModeSelector(
                    currentMode = uiState.currentMode,
                    onModeSelected = onModeSelected,
                    modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
                )

                // Circular Progress Countdown Timer
                CircularTimerDisplay(
                    uiState = uiState,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Playback Controls (Reset, Play/Pause, Skip)
                TimerControls(
                    isRunning = uiState.isRunning,
                    onStart = onStart,
                    onPause = onPause,
                    onReset = onReset,
                    onSkip = onSkip,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Stats / Session Info Card
            StatsSummaryCard(
                stats = uiState.stats,
                onClick = onOpenStats,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

/**
 * Segmented pill row for switching between Focus, Short Break, and Long Break modes.
 */
@Composable
fun PomodoroModeSelector(
    currentMode: PomodoroMode,
    onModeSelected: (PomodoroMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        Triple(PomodoroMode.FOCUS, "Focus", "25 min"),
        Triple(PomodoroMode.SHORT_BREAK, "Short Break", "5 min"),
        Triple(PomodoroMode.LONG_BREAK, "Long Break", "15 min")
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        modes.forEachIndexed { index, (mode, label, duration) ->
            val isSelected = currentMode == mode
            SegmentedButton(
                selected = isSelected,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    activeBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                icon = {},
                label = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = duration,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            },
                            maxLines = 1
                        )
                    }
                }
            )
        }
    }
}

/**
 * Prominently centered circular progress indicator with countdown text and cycle dots.
 */
@Composable
fun CircularTimerDisplay(
    uiState: FocusTimerUiState,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "CircularTimerProgress"
    )

    val strokeWidth = 14.dp
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val progressColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(280.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Circular Canvas Track & Progress Arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension
            val radius = (diameter - strokeWidthPx) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background circle track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )

            // Dynamic progress arc with rounded end caps
            val sweepAngle = 360f * animatedProgress
            if (sweepAngle > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Inside display contents
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mode Subtitle / State Indicator
            val modeTitle = when (uiState.currentMode) {
                PomodoroMode.FOCUS -> "Focus Time"
                PomodoroMode.SHORT_BREAK -> "Short Break"
                PomodoroMode.LONG_BREAK -> "Long Break"
            }
            Text(
                text = modeTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Large MM:SS Countdown Timer
            Text(
                text = uiState.formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4-Dot Cycle Indicator
            CycleIndicatorDots(
                cycleCount = uiState.cycleCount,
                isRunning = uiState.isRunning
            )
        }
    }
}

/**
 * Visual dots showing the current cycle in a 4-Pomodoro interval.
 */
@Composable
fun CycleIndicatorDots(
    cycleCount: Int,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val completedInInterval = cycleCount % 4

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isCompleted = i < completedInInterval
                val isCurrent = (i == completedInInterval) && isRunning
                val dotColor = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 12.dp else 10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .then(
                            if (isCurrent) {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                            } else Modifier
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Interval ${completedInInterval + 1}/4",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Playback controls: Smaller Reset and Skip buttons, and prominent Play/Pause FAB.
 */
@Composable
fun TimerControls(
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Button
        OutlinedIconButton(
            onClick = onReset,
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Reset Timer",
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        // Large prominent Play/Pause FAB
        FloatingActionButton(
            onClick = if (isRunning) onPause else onStart,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isRunning) "Pause Timer" else "Start Timer",
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        // Skip Button
        OutlinedIconButton(
            onClick = onSkip,
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Skip to Next Mode",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Summary card showing session statistics (completed sessions & total focus time).
 */
@Composable
fun StatsSummaryCard(
    stats: PomodoroStats,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = stats.completedCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = stats.formattedTotalTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Focus Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode - Pixel 7", device = Devices.PIXEL_7, showBackground = true)
@Composable
fun FocusTimerScreenLightPreview() {
    FocusTimerTheme(darkTheme = false) {
        FocusTimerScreenContent(
            uiState = FocusTimerUiState(
                currentMode = PomodoroMode.FOCUS,
                timerState = TimerState.RUNNING,
                timeLeftSeconds = 1420L,
                totalDurationSeconds = 1500L,
                cycleCount = 2,
                stats = PomodoroStats(completedCount = 3, totalFocusTimeSeconds = 4500L)
            ),
            onStart = {},
            onPause = {},
            onReset = {},
            onSkip = {},
            onModeSelected = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Dark Mode - Pixel 7", device = Devices.PIXEL_7, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun FocusTimerScreenDarkPreview() {
    FocusTimerTheme(darkTheme = true) {
        FocusTimerScreenContent(
            uiState = FocusTimerUiState(
                currentMode = PomodoroMode.SHORT_BREAK,
                timerState = TimerState.PAUSED,
                timeLeftSeconds = 240L,
                totalDurationSeconds = 300L,
                cycleCount = 1,
                stats = PomodoroStats(completedCount = 1, totalFocusTimeSeconds = 1500L)
            ),
            onStart = {},
            onPause = {},
            onReset = {},
            onSkip = {},
            onModeSelected = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
