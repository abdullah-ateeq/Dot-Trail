package com.example.dottrail.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import com.example.dottrail.model.DotTarget
import com.example.dottrail.model.GameState
import com.example.dottrail.ui.theme.BrandError
import com.example.dottrail.ui.theme.BrandPrimary
import kotlin.math.sin

@Composable
fun GameBoardCanvas(
    dots: List<DotTarget>,
    currentTarget: Int,
    gameState: GameState,
    trailAnimationProgress: Float,
    lastWrongDotId: Int?,
    wrongTapFeedbackTrigger: Long,
    reduceMotion: Boolean,
    onDotTapped: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    // Theme adaptive colors
    val trailColor = BrandPrimary
    val completedFillColor = if (isDark) Color(0xFF142C24) else Color(0xFFD1FAE5)
    val completedBorderColor = BrandPrimary.copy(alpha = 0.6f)
    val regularFillColor = if (isDark) Color(0xFF16202E) else Color(0xFFF1F5F9)
    val regularBorderColor = if (isDark) Color(0xFF334664) else Color(0xFFCBD5E1)
    val targetRingColor = BrandPrimary
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val errorColor = BrandError

    // Subtle pulsing animation for current target ring
    val infiniteTransition = rememberInfiniteTransition(label = "TargetRingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (reduceMotion) 1.0f else 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (reduceMotion) 0.5f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Cached Text Paints
    val textPaint = remember(isDark) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = textColor.toArgb()
        }
    }

    val completedTextPaint = remember(isDark) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = trailColor.toArgb()
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(gameState) {
                if (gameState == GameState.PLAYING) {
                    detectTapGestures { offset ->
                        onDotTapped(offset.x, offset.y)
                    }
                }
            }
    ) {
        val completedDots = dots.filter { it.completed }.sortedBy { it.number }

        // 1. Draw connecting trails between completed dots
        if (completedDots.isNotEmpty()) {
            val strokeWidthPx = 4f * density

            for (i in 0 until completedDots.size - 1) {
                val start = completedDots[i]
                val end = completedDots[i + 1]

                // If drawing the very last segment, interpolate with trailAnimationProgress
                val isLatestSegment = (i == completedDots.size - 2)
                val targetEndOffset = if (isLatestSegment && trailAnimationProgress < 1f) {
                    val progress = trailAnimationProgress.coerceIn(0f, 1f)
                    Offset(
                        x = start.x + (end.x - start.x) * progress,
                        y = start.y + (end.y - start.y) * progress
                    )
                } else {
                    Offset(end.x, end.y)
                }

                // Draw trail line
                drawLine(
                    color = trailColor,
                    start = Offset(start.x, start.y),
                    end = targetEndOffset,
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. Draw all dots
        val currentTimeMs = System.currentTimeMillis()

        dots.forEach { dot ->
            var dotCenterX = dot.x
            val dotCenterY = dot.y

            // Shake feedback if this dot was tapped incorrectly
            if (dot.id == lastWrongDotId && !reduceMotion) {
                val elapsedSinceWrongTap = currentTimeMs - wrongTapFeedbackTrigger
                if (elapsedSinceWrongTap in 0..350) {
                    val shakeFrequency = 0.05f
                    val amplitude = 14f * (1f - (elapsedSinceWrongTap / 350f))
                    dotCenterX += sin(elapsedSinceWrongTap * shakeFrequency) * amplitude
                }
            }

            val isCurrent = (dot.number == currentTarget) && !dot.completed

            if (dot.completed) {
                // Completed dot
                drawCircle(
                    color = completedFillColor,
                    radius = dot.radius,
                    center = Offset(dotCenterX, dotCenterY)
                )
                drawCircle(
                    color = completedBorderColor,
                    radius = dot.radius,
                    center = Offset(dotCenterX, dotCenterY),
                    style = Stroke(width = 2.5f * density)
                )

                // Draw checkmark icon in center
                drawCheckmark(
                    center = Offset(dotCenterX, dotCenterY),
                    size = dot.radius * 0.9f,
                    color = trailColor
                )
            } else if (isCurrent) {
                // Current Target dot: Pulsing emphasis ring
                drawCircle(
                    color = targetRingColor.copy(alpha = pulseAlpha),
                    radius = dot.radius * pulseScale,
                    center = Offset(dotCenterX, dotCenterY),
                    style = Stroke(width = 2.5f * density)
                )

                // Target dot fill
                drawCircle(
                    color = regularFillColor,
                    radius = dot.radius,
                    center = Offset(dotCenterX, dotCenterY)
                )
                // Target dot high-contrast border
                drawCircle(
                    color = targetRingColor,
                    radius = dot.radius,
                    center = Offset(dotCenterX, dotCenterY),
                    style = Stroke(width = 3f * density)
                )

                // Dot number
                drawDotNumber(
                    text = dot.number.toString(),
                    centerX = dotCenterX,
                    centerY = dotCenterY,
                    radius = dot.radius,
                    paint = textPaint
                )
            } else {
                // Regular remaining dot
                val isWrongFlash = (dot.id == lastWrongDotId && (currentTimeMs - wrongTapFeedbackTrigger) in 0..250)

                drawCircle(
                    color = if (isWrongFlash) errorColor.copy(alpha = 0.25f) else regularFillColor,
                    radius = dot.radius,
                    center = Offset(dotCenterX, dotCenterY)
                )
                drawCircle(
                    color = if (isWrongFlash) errorColor else regularBorderColor,
                    radius = dot.radius,
                    center = Offset(dotCenterX, dotCenterY),
                    style = Stroke(width = 2f * density)
                )

                // Dot number
                drawDotNumber(
                    text = dot.number.toString(),
                    centerX = dotCenterX,
                    centerY = dotCenterY,
                    radius = dot.radius,
                    paint = textPaint
                )
            }
        }
    }
}

private fun DrawScope.drawDotNumber(
    text: String,
    centerX: Float,
    centerY: Float,
    radius: Float,
    paint: Paint
) {
    paint.textSize = radius * 1.05f
    val fontMetrics = paint.fontMetrics
    val textY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f
    drawContext.canvas.nativeCanvas.drawText(text, centerX, textY, paint)
}

private fun DrawScope.drawCheckmark(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        val startX = center.x - size * 0.35f
        val startY = center.y + size * 0.02f
        val midX = center.x - size * 0.08f
        val midY = center.y + size * 0.28f
        val endX = center.x + size * 0.36f
        val endY = center.y - size * 0.28f

        moveTo(startX, startY)
        lineTo(midX, midY)
        lineTo(endX, endY)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 3.5f * density,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
