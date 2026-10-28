package com.example.dottrail.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.dottrail.ui.theme.BrandPrimary

@Composable
fun CountdownOverlay(
    countdownNumber: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = countdownNumber,
            transitionSpec = {
                (fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.4f, animationSpec = tween(220)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 1.3f, animationSpec = tween(180))
                    )
            },
            label = "CountdownAnimation"
        ) { number ->
            val text = if (number == 0) "GO!" else number.toString()
            val color = if (number == 0) BrandPrimary else Color.White

            Text(
                text = text,
                fontSize = if (number == 0) 72.sp else 96.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = color
            )
        }
    }
}
