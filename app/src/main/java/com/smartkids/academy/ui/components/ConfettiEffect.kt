package com.smartkids.academy.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class EnhancedParticle(
    val initialX: Float,
    val initialY: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val shapeType: Int, // 0: Circle, 1: Rectangle, 2: Star/Ribbon
    val rotSpeed: Float
)

@Composable
fun ConfettiCanvas(
    modifier: Modifier = Modifier,
    isVisible: Boolean
) {
    if (!isVisible) return

    val particles = remember {
        val colors = listOf(
            Color(0xFFFF1744), // Red
            Color(0xFFFFD600), // Yellow
            Color(0xFF00E676), // Green
            Color(0xFF2979FF), // Blue
            Color(0xFFA000FF), // Purple
            Color(0xFFFF6D00)  // Orange
        )
        List(90) {
            val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
            val speed = Random.nextFloat() * 0.4f + 0.1f
            EnhancedParticle(
                initialX = Random.nextFloat() * 0.8f + 0.1f,
                initialY = Random.nextFloat() * 0.2f + 0.1f,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed + 0.3f).toFloat(),
                size = Random.nextFloat() * 16f + 10f,
                color = colors.random(),
                shapeType = Random.nextInt(0, 3),
                rotSpeed = Random.nextFloat() * 360f - 180f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti_anim")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            val posX = p.initialX * width + p.vx * progress * width * 0.5f
            val posY = p.initialY * height + p.vy * progress * height * 0.8f + 0.5f * 9.8f * progress * progress * 40f
            val alpha = (1.2f - progress).coerceIn(0f, 1f)
            val currentRot = progress * p.rotSpeed

            withTransform({
                rotate(currentRot, pivot = Offset(posX, posY))
            }) {
                when (p.shapeType) {
                    0 -> {
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = p.size / 2,
                            center = Offset(posX, posY)
                        )
                    }
                    1 -> {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(posX - p.size / 2, posY - p.size / 2),
                            size = Size(p.size, p.size * 1.6f)
                        )
                    }
                    else -> {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(posX - p.size / 3, posY - p.size / 2),
                            size = Size(p.size * 1.8f, p.size / 2)
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.shake(trigger: Boolean): Modifier = composed {
    val shakeOffset by animateFloatAsState(
        targetValue = if (trigger) 12f else 0f,
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
