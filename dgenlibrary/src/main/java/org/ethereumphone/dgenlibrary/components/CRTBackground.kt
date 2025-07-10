package org.ethereumphone.dgenlibrary.components

//region Imports
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.SystemColorManager
//endregion

/**
 * A reusable full-screen background that mimics an old CRT monitor.
 *
 * 1.  Solid black base (dgenBlack)
 * 2.  Subtle primary-colored grid overlay
 * 3.  Animated scanline that sweeps vertically
 * 4.  Very soft, slowly pulsing radial glow
 */
@Composable
fun CRTBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = SystemColorManager.primaryColor,
    content: @Composable () -> Unit
) {
    // Infinite animations for scanline offset and glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "crt-bg-transition")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "scan-offset"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow-pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Base background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = dgenBlack)
        }

        // Grid overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 24.dp.toPx()
            val lineColor = primaryColor.copy(alpha = 0.08f)

            // Vertical lines
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height)
                )
                x += gridSpacing
            }
            // Horizontal lines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y)
                )
                y += gridSpacing
            }
        }

        // Moving scanline (single pixel-ish height)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineY = ((scanOffset + 1f) * 0.5f) * size.height // map -1..1 to 0..height
            drawRect(
                color = primaryColor.copy(alpha = 0.05f),
                topLeft = Offset(0f, lineY),
                size = Size(width = size.width, height = 2.dp.toPx())
            )
        }

        // Pulsing radial glow overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.06f * glowPulse), Color.Transparent),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension
                ),
                size = size
            )
        }

        // Screen content
        content()
    }
}

/**
 * Wrap any composable with this to give it a subtle CRT/glitch feel.
 *  - Adds a glowing shadow around text
 *  - Occasionally shifts the content horizontally for a few frames
 *  - Draws random horizontal glitch bars during the shift
 */
@Composable
fun GlitchEffect(
    modifier: Modifier = Modifier,
    primaryColor: Color = SystemColorManager.primaryColor,
    glitchFrequencyMs: Long = 3000L,
    content: @Composable () -> Unit
) {
    // Offset for horizontal glitch shift
    val offsetAnim = rememberInfiniteTransition(label = "glitch-shift")

    // This animates between 0 and 1 where 1 means active glitch frame
    val glitchPhase by offsetAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                // Stay calm for most of the duration
                durationMillis = glitchFrequencyMs.toInt()
                0f at 0
                0f at (glitchFrequencyMs - 120).toInt()
                1f at (glitchFrequencyMs - 60).toInt()
                0f at glitchFrequencyMs.toInt()
            },
            repeatMode = RepeatMode.Restart
        ), label = "glitch-phase"
    )

    // Map phase to pixel offset (active only during glitch window)
    val pxShift = if (glitchPhase > 0.5f) Random.nextInt(-4, 4).toFloat() else 0f

    Box(
        modifier = modifier.offset { IntOffset(pxShift.roundToInt(), 0) }
    ) {
        // Main content
        content()

        // Draw glitch bars when active
        if (pxShift != 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                repeat(3) {
                    val y = Random.nextFloat() * size.height
                    drawRect(
                        color = primaryColor.copy(alpha = 0.15f),
                        topLeft = Offset(0f, y),
                        size = Size(size.width, 1.dp.toPx())
                    )
                }
            }
        }
    }
} 