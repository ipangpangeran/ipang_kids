package com.smartkids.academy.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var color: Color,
    var alpha: Float = 1f,
    var rotation: Float = 0f,
    var vRot: Float = 0f
)

@Composable
fun ConfettiCanvas(
    modifier: Modifier = Modifier,
    isVisible: Boolean
) {
    if (!isVisible) return

    val particles = remember {
        val colors = listOf(
            Color(0xFFFFC107), // Gold
            Color(0xFFE91E63), // Pink
            Color(0xFF4CAF50), // Green
            Color(0xFF2196F3), // Blue
            Color(0xFF9C27B0), // Purple
            Color(0xFFFF5722)  // Orange
        )
        List(45) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.3f, // start near top
                vx = (Random.nextFloat() - 0.5f) * 0.015f,
                vy = Random.nextFloat() * 0.02f + 0.008f,
                radius = Random.nextFloat() * 12f + 8f,
                color = colors.random(),
                alpha = 1f,
                vRot = Random.nextFloat() * 10f - 5f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { p ->
            val px = (p.x * canvasWidth + progress * p.vx * canvasWidth) % canvasWidth
            val py = (p.y * canvasHeight + progress * p.vy * canvasHeight * 1.5f) % canvasHeight
            val alpha = (1f - progress).coerceIn(0f, 1f)

            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.radius,
                center = Offset(px, py)
            )
            drawRect(
                color = p.color.copy(alpha = alpha),
                topLeft = Offset(px - p.radius / 2, py - p.radius / 2),
                size = Size(p.radius, p.radius * 1.5f)
            )
        }
    }
}

fun Modifier.shake(trigger: Boolean): Modifier = composed {
    val shakeOffset by animateFloatAsState(
        targetValue = if (trigger) 0f else 0f,
        animationSpec = repeatable(
            iterations = 4,
            animation = tween(durationMillis = 50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )
    this.graphicsLayer {
        translationX = shakeOffset
    }
}
