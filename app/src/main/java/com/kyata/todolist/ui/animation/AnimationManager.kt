package com.kyata.todolist.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.delay
import kotlin.math.sin
import android.graphics.Paint as AndroidPaint

object AnimationManager {

    // 1. Pulse Animation cho các thẻ
    @Composable
    fun PulseAnimation(content: @Composable () -> Unit) {
        val infiniteTransition = rememberInfiniteTransition()
        val pulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(modifier = Modifier.scale(pulse)) {
            content()
        }
    }

    // 2. Neon Glow Effect cho progress indicators
    @Composable
    fun NeonGlowProgressIndicator(
        progress: Float,
        color: Color,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit = {}
    ) {
        val glowIntensity by rememberInfiniteTransition().animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(modifier = modifier) {
            // Glow effect
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f * glowIntensity),
                            color.copy(alpha = 0.1f * glowIntensity),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.minDimension * 0.6f
                    )
                )
            }

            // Content
            content()
        }
    }

    // 3. Data Stream Background
    @Composable
    fun DataStreamBackground(
        modifier: Modifier = Modifier,
        color: Color,
        lineCount: Int = 6,
        animationDuration: Int = 2000
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val streamOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Canvas(modifier = modifier) {
            // Vẽ các đường dữ liệu chuyển động
            for (i in 0 until lineCount) {
                val y = size.height * (i / lineCount.toFloat())
                val xOffset = size.width * streamOffset

                drawLine(
                    color = color.copy(alpha = 0.3f),
                    start = Offset(-xOffset + i * 30f, y),
                    end = Offset(size.width - xOffset + i * 30f, y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
                )
            }
        }
    }

    // 4. Holographic Effect
    @Composable
    fun HolographicEffect(
        modifier: Modifier = Modifier,
        animationDuration: Int = 3000
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val hologramShift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Canvas(modifier = modifier) {
            // Scan lines
            for (i in 0 until size.height.toInt() step 4) {
                val alpha = (Math.sin(((i + size.height * hologramShift) / 20f).toDouble()) * 0.5 + 0.5).toFloat() * 0.1f
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(0f, i.toFloat()),
                    end = Offset(size.width, i.toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }

    // 5. Particle System
    @Composable
    fun ParticleSystem(
        modifier: Modifier = Modifier,
        color: Color,
        particleCount: Int = 50,
        particleSizeRange: ClosedFloatingPointRange<Float> = 1f..4f
    ) {
        val particles = remember { List(particleCount) { createParticle(particleSizeRange) } }
        val animatedParticles by produceState(particles) {
            while (true) {
                delay(16) // ~60fps
                value = value.map { particle ->
                    var newPosition = particle.position + particle.velocity
                    // Wrap around screen edges
                    if (newPosition.x < 0) newPosition = newPosition.copy(x = 1f)
                    if (newPosition.x > 1) newPosition = newPosition.copy(x = 0f)
                    if (newPosition.y < 0) newPosition = newPosition.copy(y = 1f)
                    if (newPosition.y > 1) newPosition = newPosition.copy(y = 0f)

                    particle.copy(
                        position = newPosition,
                        life = (particle.life + 0.01f) % 1f
                    )
                }
            }
        }

        Canvas(modifier = modifier) {
            animatedParticles.forEach { particle ->
                val alpha = (Math.sin(particle.life * Math.PI * 2).toFloat() * 0.5f + 0.5f) * 0.3f
                drawCircle(
                    color = color.copy(alpha = alpha),
                    center = Offset(particle.position.x * size.width, particle.position.y * size.height),
                    radius = particle.size.dp.toPx()
                )
            }
        }
    }

    // 6. Digital Rain Effect (Matrix Style)
    @Composable
    fun DigitalRainEffect(
        modifier: Modifier = Modifier,
        color: Color,
        columns: Int = 15,
        fontSize: TextUnit = 14.sp
    ) {
        val symbols = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val raindrops = remember { MutableList(columns) { Random.nextInt(-10, 0) } }

        LaunchedEffect(Unit) {
            while (true) {
                delay(100)
                raindrops[Random.nextInt(columns)] = 0
            }
        }

        val textSizePx = with(LocalDensity.current) { fontSize.toPx() }

        Canvas(modifier = modifier) {
            raindrops.forEachIndexed { index, position ->
                if (position >= 0) {
                    val x = size.width * index / columns
                    val y = size.height * position / 20f

                    drawDigitalChar(
                        char = symbols[Random.nextInt(symbols.size)],
                        position = Offset(x, y),
                        progress = position / 20f,
                        color = color,
                        textSize = textSizePx
                    )

                    raindrops[index] = (position + 1) % 21
                }
            }
        }
    }

    // 7. Morphing Waves Effect
    @Composable
    fun MorphingWaves(
        modifier: Modifier = Modifier,
        color: Color,
        waveHeight: Dp = 20.dp,
        animationDuration: Int = 4000
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val morphProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        val waveHeightPx = with(LocalDensity.current) { waveHeight.toPx() }

        Canvas(modifier = modifier) {
            val path = Path()
            val points = 10

            path.moveTo(0f, size.height / 2)

            for (i in 0..points) {
                val x = size.width * i / points.toFloat()
                val y = size.height / 2 +
                        sin(x / size.width * 4 * Math.PI + morphProgress * 2 * Math.PI).toFloat() * waveHeightPx *
                        sin(morphProgress * Math.PI).toFloat()

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = size.height
                )
            )
        }
    }

    // 8. Circular Wave Animation
    @Composable
    fun CircularWaveAnimation(
        modifier: Modifier = Modifier,
        color: Color,
        circleCount: Int = 3,
        animationDuration: Int = 2000
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val waveProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Canvas(modifier = modifier) {
            for (i in 0 until circleCount) {
                val progress = (waveProgress + i * 0.3f) % 1f
                val alpha = 1f - progress
                val radius = size.minDimension * 0.5f * progress

                drawCircle(
                    color = color.copy(alpha = alpha * 0.3f),
                    center = center,
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }

    // Helper functions
    private fun createParticle(sizeRange: ClosedFloatingPointRange<Float>): Particle {
        return Particle(
            position = Offset(Random.nextFloat(), Random.nextFloat()),
            velocity = Offset(Random.nextFloat() - 0.5f, Random.nextFloat() - 0.5f) * 0.01f,
            size = Random.nextFloat() * (sizeRange.endInclusive - sizeRange.start) + sizeRange.start,
            life = Random.nextFloat()
        )
    }

    private fun DrawScope.drawDigitalChar(
        char: Char,
        position: Offset,
        progress: Float,
        color: Color,
        textSize: Float
    ) {
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                char.toString(),
                position.x,
                position.y,
                AndroidPaint().apply {
                    this.color = android.graphics.Color.argb(
                        (255 * (1 - progress)).toInt(),
                        (color.red * 255).toInt(),
                        (color.green * 255).toInt(),
                        (color.blue * 255).toInt()
                    )
                    this.textSize = textSize
                }
            )
        }
    }

    data class Particle(
        val position: Offset,
        val velocity: Offset,
        val size: Float,
        val life: Float
    )
}