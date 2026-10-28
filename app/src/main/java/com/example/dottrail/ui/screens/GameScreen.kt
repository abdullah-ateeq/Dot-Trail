package com.example.dottrail.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dottrail.game.DotTrailViewModel
import com.example.dottrail.model.GameState
import com.example.dottrail.model.TimerStyle
import com.example.dottrail.ui.components.CountdownOverlay
import com.example.dottrail.ui.components.GameBoardCanvas
import com.example.dottrail.ui.components.PauseDialog
import com.example.dottrail.ui.components.WinParticleEffect
import com.example.dottrail.ui.theme.BrandError
import com.example.dottrail.ui.theme.BrandPrimary
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: DotTrailViewModel,
    onNavigateToResult: () -> Unit,
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settingsFlow.collectAsState()
    val density = LocalDensity.current.density

    // Intercept back button during gameplay
    BackHandler {
        if (uiState.gameState == GameState.PLAYING) {
            viewModel.pauseGame()
        } else if (uiState.gameState == GameState.PAUSED) {
            viewModel.resumeGame()
        } else {
            onExitToHome()
        }
    }

    // Auto navigate to result on Win or Game Over
    LaunchedEffect(uiState.gameState) {
        if (uiState.gameState == GameState.WON) {
            delay(1200L) // allow win celebration & particles to be enjoyed
            onNavigateToResult()
        } else if (uiState.gameState == GameState.GAME_OVER) {
            delay(600L)
            onNavigateToResult()
        }
    }

    // Timer urgency pulse animation when remaining <= 5s
    val infiniteTransition = rememberInfiniteTransition(label = "WarningTimerPulse")
    val timerScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.isWarningTime && !settings.reduceMotion) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerScale"
    )

    val remainingSec = uiState.remainingTimeMs / 1000f
    val timerText = String.format(Locale.US, "%.2f", remainingSec)
    val timerColor = if (uiState.isWarningTime) BrandError else MaterialTheme.colorScheme.onBackground

    val completedDotsCount = uiState.dots.count { it.completed }
    val totalDotsCount = uiState.config.dotCount
    val progress = if (totalDotsCount > 0) completedDotsCount.toFloat() / totalDotsCount.toFloat() else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Level Badge
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "LEVEL ${uiState.level}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = BrandPrimary
                        )
                    }

                    // Timer Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(timerScale)
                    ) {
                        Text(
                            text = timerText,
                            fontSize = if (settings.timerStyle == TimerStyle.DIGITAL) 28.sp else 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = timerColor
                        )
                        if (uiState.isWarningTime) {
                            Text(
                                text = "HURRY!",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandError
                            )
                        }
                    }

                    // Pause Button
                    IconButton(
                        onClick = { viewModel.pauseGame() },
                        enabled = uiState.gameState == GameState.PLAYING
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar & Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Next Target Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NEXT: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.currentTarget <= totalDotsCount) uiState.currentTarget.toString() else "✓",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Progress text
                    Text(
                        text = "$completedDotsCount / $totalDotsCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Mistakes Counter
                    if (uiState.mistakes > 0) {
                        Text(
                            text = "Mistakes: ${uiState.mistakes}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BrandPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Playable Canvas Board Area (takes remaining space, no scrolling)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                val boardWidth = constraints.maxWidth.toFloat()
                val boardHeight = constraints.maxHeight.toFloat()

                LaunchedEffect(boardWidth, boardHeight) {
                    if (boardWidth > 0f && boardHeight > 0f) {
                        viewModel.onBoardSizeChanged(boardWidth, boardHeight, density)
                    }
                }

                GameBoardCanvas(
                    dots = uiState.dots,
                    currentTarget = uiState.currentTarget,
                    gameState = uiState.gameState,
                    trailAnimationProgress = uiState.trailAnimationProgress,
                    lastWrongDotId = uiState.lastWrongDotId,
                    wrongTapFeedbackTrigger = uiState.wrongTapFeedbackTrigger,
                    reduceMotion = settings.reduceMotion,
                    onDotTapped = { x, y -> viewModel.onCanvasTouch(x, y) },
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Wrong Tap Penalty Banner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.showPenaltyPopup,
                        enter = fadeIn() + slideInVertically { -it },
                        exit = fadeOut() + slideOutVertically { -it }
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandError),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "WRONG -${String.format(Locale.US, "%.1f", uiState.penaltyAmountSec)}s",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Countdown Overlay before gameplay starts
        if (uiState.gameState == GameState.COUNTDOWN) {
            CountdownOverlay(countdownNumber = uiState.countdownNumber)
        }

        // Win Particles
        if (uiState.gameState == GameState.WON) {
            WinParticleEffect()
        }

        // Pause Dialog
        if (uiState.gameState == GameState.PAUSED) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartCurrentLevel() },
                onExit = onExitToHome
            )
        }
    }
}
