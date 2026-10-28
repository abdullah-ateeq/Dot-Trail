package com.example.dottrail.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.dottrail.ui.theme.BrandPrimary
import com.example.dottrail.ui.theme.BrandPrimaryLight
import com.example.dottrail.ui.theme.BrandStreak
import com.example.dottrail.ui.theme.BrandWarning
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val speedX: Float,
    val speedY: Float,
    val size: Float,
    val color: Color
)

@Composable
fun WinParticleEffect(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
        )
    }

    val colors = listOf(
        BrandPrimary,
        BrandPrimaryLight,
        BrandWarning,
        BrandStreak,
        Color.White
    )

    val particles = remember {
        val random = Random(42)
        List(36) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 120f + random.nextFloat() * 260f
            Particle(
                initialX = 0.5f,
                initialY = 0.45f,
                speedX = cos(angle) * speed,
                speedY = sin(angle) * speed,
                size = 3.5f + random.nextFloat() * 4.5f,
                color = colors[random.nextInt(colors.size)]
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val alpha = (1f - t * 0.95f).coerceIn(0f, 1f)
        val gravity = 180f * t * t

        particles.forEach { p ->
            val cx = size.width * p.initialX + p.speedX * t
            val cy = size.height * p.initialY + p.speedY * t + gravity
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size * density,
                center = Offset(cx, cy)
            )
        }
    }
}
