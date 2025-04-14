package org.ethereumphone.dgenlibrary.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@Composable
fun RotatingIcon(
    icon: @Composable () -> Unit,
    isRotated: Boolean,
    delayMillis: Int = 300
) {
    // State to control the actual rotation after delay
    var shouldRotate by remember { mutableStateOf(false) }

    // Apply the delay when isRotated changes to true
    LaunchedEffect(isRotated) {
        if (isRotated) {
            delay(delayMillis.toLong())
            shouldRotate = true
        } else {
            shouldRotate = false
        }
    }

    // Create animated rotation value
    val rotation by animateFloatAsState(
        targetValue = if (shouldRotate) 45f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "rotation"
    )

    // Apply the rotation to the icon
    Box(
        modifier = Modifier.graphicsLayer {
            rotationZ = rotation
        }
    ) {
        icon()
    }
}