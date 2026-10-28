package com.example.dottrail.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dottrail.ui.theme.BrandPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = LinearEasing)
        )
        delay(100L)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DOT TRAIL",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Animated 4 dots with connecting trail
            val trailColor = BrandPrimary
            val dotBgColor = MaterialTheme.colorScheme.surfaceVariant
            val textColor = MaterialTheme.colorScheme.onBackground

            Canvas(modifier = Modifier.size(width = 240.dp, height = 50.dp)) {
                val count = 4
                val spacing = size.width / (count + 1)
                val dotRadius = 14f * density
                val cy = size.height / 2f

                val currentAnim = animProgress.value * 4f // 0 to 4

                // Draw completed trail segments
                for (i in 0 until count - 1) {
                    val startX = spacing * (i + 1)
                    val endX = spacing * (i + 2)

                    if (currentAnim > i + 1) {
                        val segmentProgress = (currentAnim - (i + 1)).coerceIn(0f, 1f)
                        val lineEndX = startX + (endX - startX) * segmentProgress
                        drawLine(
                            color = trailColor,
                            start = Offset(startX, cy),
                            end = Offset(lineEndX, cy),
                            strokeWidth = 3f * density,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw dots
                for (i in 0 until count) {
                    val cx = spacing * (i + 1)
                    val appeared = currentAnim >= i * 0.8f

                    if (appeared) {
                        val isConnected = currentAnim >= (i + 1)
                        drawCircle(
                            color = if (isConnected) trailColor else dotBgColor,
                            radius = dotRadius,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = trailColor,
                            radius = dotRadius,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f * density)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Find. Tap. Connect.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
    }
}
