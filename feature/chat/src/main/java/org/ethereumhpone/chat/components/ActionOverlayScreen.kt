package org.ethereumhpone.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import kotlinx.coroutines.delay

/**
 * Enum for transaction action types shown in the action overlay
 */
enum class TransactionAction {
    TRANSFER,
    TRANSFER_REQUEST
}

@Composable
fun ActionOverlayScreen(
    showOverlay:  MutableState<Boolean>,
    shouldRotate:  MutableState<Boolean>,
    openImage: () -> Unit,
    openVideo: () -> Unit,
    openSend: () -> Unit,
    openCamera: () -> Unit,
    primaryColor: Color
){
    ActionOverlayScreen(
        showOverlay = showOverlay,
        shouldRotate = shouldRotate,
        onActionSelected = { action ->
            when (action) {
                TransactionAction.TRANSFER -> openSend()
                TransactionAction.TRANSFER_REQUEST -> openSend() // Default fallback
            }
        },
        onDismiss = { showOverlay.value = false },
        primaryColor = primaryColor
    )
}

/**
 * Main action overlay screen that shows Transfer and Transfer Request options
 */
@Composable
fun ActionOverlayScreen(
    showOverlay: MutableState<Boolean>,
    shouldRotate: MutableState<Boolean>,
    onActionSelected: (TransactionAction) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color
) {
    AnimatedVisibility(
        visible = showOverlay.value,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = Modifier.fillMaxSize()
    ) {

        LaunchedEffect(showOverlay) {
            if (showOverlay.value) {
                delay(300)
                shouldRotate.value = true
            } else {
                shouldRotate.value = false
            }
        }

        var alpha1 by remember { mutableStateOf(0f) }
        var alpha2 by remember { mutableStateOf(0f) }
        val delayBetweenTexts = 50

        // Animation spec
        val animationSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)

        // Trigger animations when parent becomes visible
        LaunchedEffect(showOverlay.value) {
            if (showOverlay.value) {
                // Reset states
                alpha1 = 0f
                alpha2 = 0f

                // Start sequential animations
                delay(100) // Small initial delay

                // Animate first text (Transfer)
                animate(0f, 1f, animationSpec = animationSpec) { value, _ ->
                    alpha1 = value
                }

                delay(delayBetweenTexts.toLong())

                // Animate second text (Transfer Request)
                animate(0f, 1f, animationSpec = animationSpec) { value, _ ->
                    alpha2 = value
                }
            } else {
                // Reset when hiding
                alpha1 = 0f
                alpha2 = 0f
            }
        }

        // Create animated rotation value
        val rotation by animateFloatAsState(
            targetValue = if (shouldRotate.value) 45f else 0f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dgenBlack)
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomStart
        ) {
            // Your overlay content here
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Prevent clicks on the content from closing the overlay
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Do nothing to prevent propagation */ },
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Transfer option
                    Text(
                        text = "Transfer",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 40.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        modifier = Modifier
                            .alpha(alpha1)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    onActionSelected(TransactionAction.TRANSFER)
                                }
                            }
                    )
                    
                    // Transfer Request option
                    Text(
                        text = "Request",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 40.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        modifier = Modifier
                            .alpha(alpha2)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    onActionSelected(TransactionAction.TRANSFER_REQUEST)
                                }
                            }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                ) {
                    IconButton(
                        onClick = { onDismiss() },
                        colors = IconButtonDefaults.iconButtonColors(
                            Color.Transparent,
                            primaryColor
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    rotationZ = rotation
                                },
                            imageVector = Icons.Outlined.Add,
                            tint = primaryColor,
                            contentDescription = "collapse"
                        )
                    }
                }
            }
        }
    }
}

