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

data class DualCannonParticle(
    val initialX: Float,
    val initialY: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val shapeType: Int, // 0: Circle, 1: Square, 2: Ribbon
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
            Color(0xFFFF1744), // Bright Red
            Color(0xFFFFD600), // Gold Yellow
            Color(0xFF00E676), // Neon Green
            Color(0xFF2979FF), // Vivid Blue
            Color(0xFFA000FF), // Deep Purple
            Color(0xFFFF6D00), // Energetic Orange
            Color(0xFFE91E63)  // Pink
        )

        // Generate 100 particles: half from left cannon (top-left), half from right cannon (top-right)
        List(100) { index ->
            val isLeftCannon = index % 2 == 0
            val spawnX = if (isLeftCannon) Random.nextFloat() * 0.15f + 0.05f else Random.nextFloat() * 0.15f + 0.80f
            val spawnY = Random.nextFloat() * 0.15f + 0.05f

            // Left cannon shoots right-upwards (angle 30° to 75°), Right cannon shoots left-upwards (angle 105° to 150°)
            val angleDeg = if (isLeftCannon) Random.nextDouble(25.0, 70.0) else Random.nextDouble(110.0, 155.0)
            val angleRad = Math.toRadians(angleDeg)
            val speed = Random.nextFloat() * 0.55f + 0.25f

            DualCannonParticle(
                initialX = spawnX,
                initialY = spawnY,
                vx = (cos(angleRad) * speed).toFloat(),
                vy = (-sin(angleRad) * speed * 0.8f).toFloat(), // Upward initial velocity
                size = Random.nextFloat() * 16f + 10f,
                color = colors.random(),
                shapeType = Random.nextInt(0, 3),
                rotSpeed = Random.nextFloat() * 400f - 200f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti_dual")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            // Physics trajectory: X moves according to initial velocity, Y arc upwards then gravity pulls down (0.5 * g * t^2)
            val posX = p.initialX * width + p.vx * progress * width * 0.7f
            val posY = p.initialY * height + p.vy * progress * height * 0.6f + 0.5f * 9.8f * progress * progress * 80f
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
                            size = Size(p.size, p.size * 1.5f)
                        )
                    }
                    else -> {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(posX - p.size / 3, posY - p.size / 2),
                            size = Size(p.size * 1.8f, p.size / 2.2f)
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
