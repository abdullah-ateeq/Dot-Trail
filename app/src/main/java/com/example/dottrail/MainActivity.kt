package com.example.dottrail

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.dottrail.game.DotTrailViewModel
import com.example.dottrail.navigation.AppNavigation
import com.example.dottrail.ui.theme.DotTrailTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DotTrailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check system reduced motion setting on initial launch
        checkSystemReducedMotion()

        setContent {
            val userSettings by viewModel.settingsFlow.collectAsState()

            DotTrailTheme(appTheme = userSettings.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkSystemReducedMotion() {
        try {
            val scale = Settings.Global.getFloat(
                contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            if (scale == 0f) {
                viewModel.preferencesManager.setReduceMotion(true)
            }
        } catch (_: Exception) {
            // Permission or platform fallback
        }
    }
}
