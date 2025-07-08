package org.ethereumphone.dgenlibrary.components

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.extraLargeEnterDuration
import com.example.dgenlibrary.ui.theme.extraLargeExitDuration
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.example.dgenlibrary.ui.theme.mediumExitDuration
import com.example.dgenlibrary.ui.theme.pulseOpacity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenGreen
import org.ethereumphone.dgenlibrary.theme.dgenRed
import kotlin.coroutines.cancellation.CancellationException
import coil.ImageLoader
import coil.compose.AsyncImage

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILURE
}

@Composable
fun TransactionStatusOverlay(
    status: TransactionStatus?,
    gifLoader: ImageLoader, // Actual Composable uses this
    primaryColor: Color,
    secondaryColor: Color,
    onDismiss: () -> Unit,
    dismissDelay: Long = 5000L
) {
    var displayStatus by remember { mutableStateOf<TransactionStatus?>(null) }

    // Sync external status to our internal display status, but only when it's not null.
    // This makes our internal state "sticky" for the success/failure message.
    LaunchedEffect(status) {
        if (status != null) {
            displayStatus = status
        }
    }

    // This effect handles the dismissal logic based on our *internal* state.
    // It won't be cancelled prematurely by the external status becoming null.
    LaunchedEffect(displayStatus) {
        if (displayStatus == TransactionStatus.SUCCESS || displayStatus == TransactionStatus.FAILURE) {
            delay(dismissDelay)
            onDismiss()
            displayStatus = null // Hide the overlay after the delay.
        }
    }

    AnimatedVisibility(
        visible = displayStatus != null,
        enter = fadeIn(animationSpec = tween(durationMillis = mediumEnterDuration)),
        exit = fadeOut(animationSpec = tween(durationMillis = mediumExitDuration))
    ) {
        // The rest of the UI is driven by our non-null internal state.
        val currentStatus = displayStatus ?: return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dgenBlack)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        Log.d("TransactionOverlay", "Overlay tapped - dismissing")
                        //onDismiss()
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val pulsatingAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = pulseOpacity,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                val blinkingAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f, // Not fully off, less aggressive
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                val targetColor = when (currentStatus) {
                    TransactionStatus.PENDING -> primaryColor
                    TransactionStatus.SUCCESS -> dgenGreen
                    TransactionStatus.FAILURE -> dgenRed
                }

                val animatedBaseColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(durationMillis = 2000), // Long fade
                    label = "baseColorAnimation"
                )

                AsyncImage(
                    modifier = Modifier
                        .size(350.dp)
                        .aspectRatio(1f),
                    imageLoader = gifLoader,
                    colorFilter = ColorFilter.tint(animatedBaseColor.copy(alpha = pulsatingAlpha)),
                    model = R.drawable.globe_transfer_thick,
                    contentDescription = "Status Animation"
                )

                AnimatedContent(
                    targetState = currentStatus,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(extraLargeEnterDuration)
                        ) togetherWith fadeOut(animationSpec = tween(extraLargeExitDuration))
                    },
                    label = "textAnimation"
                ) { targetStatus ->
                    val text = when (targetStatus) {
                        TransactionStatus.PENDING -> "Transaction Pending..."
                        TransactionStatus.SUCCESS -> "Transaction Confirmed!"
                        TransactionStatus.FAILURE -> "Transaction Failed"
                    }
                    Text(
                        text = text.uppercase(),
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor.copy(alpha = blinkingAlpha),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .offset(y = -48.dp)
                            .padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

// Previews for different states
@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 420, heightDp = 420)
@Composable
fun TransactionStatusOverlayPreviewPending() {
    // For preview, we pass a dummy ImageLoader or use a placeholder directly
    // Here, we'll directly use an Image composable for simplicity in preview
    val context = LocalContext.current
    var status: TransactionStatus? = TransactionStatus.PENDING

//    val gifEnabledLoader = ImageLoader.Builder(context)
//        .components {
//            if ( SDK_INT >= 28 ) {
//                add(ImageDecoderDecoder.Factory())
//            } else {
//                add(GifDecoder.Factory())
//            }
//        }.build()

//    TransactionStatusOverlay(
//        status = status,
//        gifLoader = gifEnabledLoader,
//        onDismiss = { status = null }
//    )
}
