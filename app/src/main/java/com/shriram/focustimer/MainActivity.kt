package com.shriram.focustimer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shriram.focustimer.ui.FocusTimerScreen
import com.shriram.focustimer.ui.FocusTimerScreenContent
import com.shriram.focustimer.ui.FocusTimerUiState
import com.shriram.focustimer.ui.FocusTimerViewModel
import com.shriram.focustimer.ui.theme.FocusTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val viewModel: FocusTimerViewModel = viewModel(
                        factory = FocusTimerViewModel.Factory(application)
                    )
                    FocusTimerScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Preview(name = "MainActivity Light Preview", device = Devices.PIXEL_7, showBackground = true)
@Composable
fun MainActivityPreview() {
    FocusTimerTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FocusTimerScreenContent(
                uiState = FocusTimerUiState(),
                onStart = {},
                onPause = {},
                onReset = {},
                onSkip = {},
                onModeSelected = {},
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
    }
}

@Preview(name = "MainActivity Dark Preview", device = Devices.PIXEL_7, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainActivityDarkPreview() {
    FocusTimerTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FocusTimerScreenContent(
                uiState = FocusTimerUiState(),
                onStart = {},
                onPause = {},
                onReset = {},
                onSkip = {},
                onModeSelected = {},
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
    }
}
