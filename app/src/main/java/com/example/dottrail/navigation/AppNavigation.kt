package com.example.dottrail.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dottrail.game.DotTrailViewModel
import com.example.dottrail.model.GameState
import com.example.dottrail.ui.screens.AboutScreen
import com.example.dottrail.ui.screens.GameScreen
import com.example.dottrail.ui.screens.HomeScreen
import com.example.dottrail.ui.screens.ResultScreen
import com.example.dottrail.ui.screens.SettingsScreen
import com.example.dottrail.ui.screens.SplashScreen

@Composable
fun AppNavigation(
    viewModel: DotTrailViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats by viewModel.statsFlow.collectAsState()
    val settings by viewModel.settingsFlow.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                selectedDifficulty = uiState.difficulty,
                stats = stats,
                onSelectDifficulty = { diff ->
                    viewModel.setDifficulty(diff)
                },
                onPlayClicked = {
                    viewModel.startLevel(
                        level = 1,
                        difficulty = uiState.difficulty,
                        regenerateNewLayout = true
                    )
                    navController.navigate(NavRoutes.GAME)
                },
                onSettingsClicked = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onAboutClicked = {
                    navController.navigate(NavRoutes.ABOUT)
                }
            )
        }

        composable(NavRoutes.GAME) {
            GameScreen(
                viewModel = viewModel,
                onNavigateToResult = {
                    navController.navigate(NavRoutes.RESULT) {
                        popUpTo(NavRoutes.GAME) { inclusive = true }
                    }
                },
                onExitToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.GAME) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.RESULT) {
            ResultScreen(
                result = uiState.gameResult,
                stats = stats,
                onNextLevel = {
                    viewModel.startNextLevel()
                    navController.navigate(NavRoutes.GAME) {
                        popUpTo(NavRoutes.RESULT) { inclusive = true }
                    }
                },
                onPlayAgain = {
                    viewModel.restartCurrentLevel()
                    navController.navigate(NavRoutes.GAME) {
                        popUpTo(NavRoutes.RESULT) { inclusive = true }
                    }
                },
                onGoHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.RESULT) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onThemeChanged = { theme -> viewModel.preferencesManager.setTheme(theme) },
                onSoundChanged = { enabled -> viewModel.preferencesManager.setSoundEnabled(enabled) },
                onHapticsChanged = { enabled -> viewModel.preferencesManager.setHapticsEnabled(enabled) },
                onTimerStyleChanged = { style -> viewModel.preferencesManager.setTimerStyle(style) },
                onReduceMotionChanged = { reduce -> viewModel.preferencesManager.setReduceMotion(reduce) },
                onResetStats = { viewModel.preferencesManager.resetStatistics() },
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ABOUT) {
            AboutScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
