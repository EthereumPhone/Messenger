package org.ethereumhpone.chat.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
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
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream

@Composable
fun ActionOverlayScreen(
    showOverlay:  MutableState<Boolean>,
    shouldRotate:  MutableState<Boolean>,
    openGallery: () -> Unit,
    openCamera: () -> Unit
){
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
        var alpha3 by remember { mutableStateOf(0f) }
        val delayBetweenTexts = 25

        // Animation spec
        val animationSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)

        // Trigger animations when parent becomes visible
        LaunchedEffect(showOverlay.value) {
            if (showOverlay.value) {
                // Reset states
                alpha1 = 0f
                alpha2 = 0f
                alpha3 = 0f

                // Start sequential animations
                delay(100) // Small initial delay

                // Animate first text
                animate(0f, 1f, animationSpec = animationSpec) { value, _ ->
                    alpha1 = value
                }

                delay(delayBetweenTexts.toLong())

                // Animate second text
                animate(0f, 1f, animationSpec = animationSpec) { value, _ ->
                    alpha2 = value
                }

                delay(delayBetweenTexts.toLong())

                // Animate third text
                animate(0f, 1f, animationSpec = animationSpec) { value, _ ->
                    alpha3 = value
                }
            } else {
                // Reset when hiding
                alpha1 = 0f
                alpha2 = 0f
                alpha3 = 0f
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
                .clickable { showOverlay.value = false },
            contentAlignment = Alignment.BottomStart
        ) {
            // Your overlay content here
            Column(
                modifier = Modifier
                    .fillMaxWidth()

                    //.align(Alignment.Center)
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
                    Text(
                        text = "Camera",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 40.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        modifier = Modifier.alpha(alpha3).pointerInput(Unit) {
                            detectTapGestures{
                                openCamera()
                            }
                        }
                    )
                    Text(
                        text = "Image",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 40.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        modifier = Modifier.alpha(alpha2).pointerInput(Unit) {
                            detectTapGestures{
                                openGallery()
                            }
                        }
                    )
                    Text(
                        text = "Send",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 40.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        modifier = Modifier.alpha(alpha1)
                    )
                }



                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                ) {
                    IconButton(
                        onClick = {
                            showOverlay.value = false
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            Color.Transparent,
                            dgenTurqoise
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {

                        Icon(
                            modifier = Modifier.size(36.dp).graphicsLayer{
                                rotationZ = rotation
                            },
                            imageVector = Icons.Outlined.Add,
                            tint = dgenTurqoise,
                            contentDescription = "collapse"
                        )
                    }
                }
            }
        }
    }
}

