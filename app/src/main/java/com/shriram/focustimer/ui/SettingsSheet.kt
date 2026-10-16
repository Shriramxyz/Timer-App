package com.shriram.focustimer.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.FreeBreakfast
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shriram.focustimer.model.PomodoroSettings
import com.shriram.focustimer.ui.theme.FocusTimerTheme
import kotlin.math.roundToInt

/**
 * Modal bottom sheet for customizing interval lengths, auto-advance options, and notifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    currentSettings: PomodoroSettings,
    onDismiss: () -> Unit,
    onSaveSettings: (PomodoroSettings) -> Unit,
    onResetToDefaults: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetMaxWidth = 560.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        SettingsSheetContent(
            currentSettings = currentSettings,
            onClose = onDismiss,
            onSave = onSaveSettings,
            onResetToDefaults = onResetToDefaults,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

/**
 * Content container for Settings, responsive on phone and tablet up to 560.dp.
 */
@Composable
fun SettingsSheetContent(
    currentSettings: PomodoroSettings,
    onClose: () -> Unit,
    onSave: (PomodoroSettings) -> Unit,
    onResetToDefaults: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focusMinutes by remember(currentSettings) {
        mutableIntStateOf(currentSettings.focusDurationMinutes)
    }
    var shortBreakMinutes by remember(currentSettings) {
        mutableIntStateOf(currentSettings.shortBreakDurationMinutes)
    }
    var longBreakMinutes by remember(currentSettings) {
        mutableIntStateOf(currentSettings.longBreakDurationMinutes)
    }
    var autoStartBreaks by remember(currentSettings) {
        mutableStateOf(currentSettings.autoStartBreaks)
    }
    var autoStartFocus by remember(currentSettings) {
        mutableStateOf(currentSettings.autoStartFocus)
    }
    var soundEnabled by remember(currentSettings) {
        mutableStateOf(currentSettings.soundEnabled)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Timer Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customize intervals & preferences",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Settings"
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Section: Interval Durations
            Text(
                text = "Interval Durations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Focus Duration: 5 to 60 mins
            DurationConfigCard(
                title = "Focus Time",
                subtitle = "Length of each concentration session",
                icon = Icons.Rounded.Timer,
                valueMinutes = focusMinutes,
                minMinutes = 5,
                maxMinutes = 60,
                onValueChange = { focusMinutes = it }
            )

            // Short Break Duration: 1 to 15 mins
            DurationConfigCard(
                title = "Short Break",
                subtitle = "Brief rest between focus sessions",
                icon = Icons.Rounded.Coffee,
                valueMinutes = shortBreakMinutes,
                minMinutes = 1,
                maxMinutes = 15,
                onValueChange = { shortBreakMinutes = it }
            )

            // Long Break Duration: 5 to 30 mins
            DurationConfigCard(
                title = "Long Break",
                subtitle = "Extended break after 4 focus sessions",
                icon = Icons.Rounded.SelfImprovement,
                valueMinutes = longBreakMinutes,
                minMinutes = 5,
                maxMinutes = 30,
                onValueChange = { longBreakMinutes = it }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Section: Automation & Sound Notifications
            Text(
                text = "Automation & Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Auto-start breaks
                    SettingToggleRow(
                        title = "Auto-start Breaks",
                        description = "Automatically start timer when break begins",
                        icon = Icons.Rounded.FreeBreakfast,
                        checked = autoStartBreaks,
                        onCheckedChange = { autoStartBreaks = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Auto-start focus
                    SettingToggleRow(
                        title = "Auto-start Focus",
                        description = "Automatically start next focus session after break",
                        icon = Icons.Rounded.PlayArrow,
                        checked = autoStartFocus,
                        onCheckedChange = { autoStartFocus = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Sound notifications
                    SettingToggleRow(
                        title = "Sound Notifications",
                        description = "Play notification chime when session timer finishes",
                        icon = Icons.Rounded.NotificationsActive,
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons: Reset to Defaults & Save Changes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val defaultSettings = PomodoroSettings()
                        focusMinutes = defaultSettings.focusDurationMinutes
                        shortBreakMinutes = defaultSettings.shortBreakDurationMinutes
                        longBreakMinutes = defaultSettings.longBreakDurationMinutes
                        autoStartBreaks = defaultSettings.autoStartBreaks
                        autoStartFocus = defaultSettings.autoStartFocus
                        soundEnabled = defaultSettings.soundEnabled
                        onResetToDefaults()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Defaults",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = {
                        val updated = PomodoroSettings(
                            focusDurationMinutes = focusMinutes,
                            shortBreakDurationMinutes = shortBreakMinutes,
                            longBreakDurationMinutes = longBreakMinutes,
                            autoStart = autoStartBreaks && autoStartFocus,
                            autoStartBreaks = autoStartBreaks,
                            autoStartFocus = autoStartFocus,
                            soundEnabled = soundEnabled
                        )
                        onSave(updated)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Changes",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Reusable card item with a title, icon, value chip, stepper buttons (- / +), and Material 3 slider.
 */
@Composable
fun DurationConfigCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    valueMinutes: Int,
    minMinutes: Int,
    maxMinutes: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Value Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$valueMinutes min",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper and Slider Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Minus stepper
                OutlinedIconButton(
                    onClick = {
                        val newValue = (valueMinutes - 1).coerceAtLeast(minMinutes)
                        onValueChange(newValue)
                    },
                    enabled = valueMinutes > minMinutes,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Decrease $title",
                        modifier = Modifier.size(18.dp)
                    )
                }

                Slider(
                    value = valueMinutes.toFloat(),
                    onValueChange = { onValueChange(it.roundToInt()) },
                    valueRange = minMinutes.toFloat()..maxMinutes.toFloat(),
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Plus stepper
                OutlinedIconButton(
                    onClick = {
                        val newValue = (valueMinutes + 1).coerceAtMost(maxMinutes)
                        onValueChange(newValue)
                    },
                    enabled = valueMinutes < maxMinutes,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Increase $title",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Row item for a preference toggle with title, subtitle, icon, and Material 3 Switch.
 */
@Composable
fun SettingToggleRow(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Preview(name = "Settings Sheet Light - Pixel 7", device = Devices.PIXEL_7, showBackground = true)
@Composable
fun SettingsSheetLightPreview() {
    FocusTimerTheme(darkTheme = false) {
        Surface {
            SettingsSheetContent(
                currentSettings = PomodoroSettings(),
                onClose = {},
                onSave = {},
                onResetToDefaults = {}
            )
        }
    }
}

@Preview(name = "Settings Sheet Dark - Pixel 7", device = Devices.PIXEL_7, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SettingsSheetDarkPreview() {
    FocusTimerTheme(darkTheme = true) {
        Surface {
            SettingsSheetContent(
                currentSettings = PomodoroSettings(
                    focusDurationMinutes = 30,
                    shortBreakDurationMinutes = 7,
                    autoStartBreaks = true
                ),
                onClose = {},
                onSave = {},
                onResetToDefaults = {}
            )
        }
    }
}
