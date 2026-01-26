package org.ethereumhpone.chat

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import kotlinx.coroutines.delay
import org.ethereumhpone.chat.components.TransactionMode
import org.ethereumhpone.chat.components.UnifiedTransactionOverlayRoute
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.TransactionStatus
import org.ethereumphone.dgenlibrary.components.TransactionStatusOverlay
import org.ethereumphone.dgenlibrary.theme.dgenBlack

/**
 * Timing constants for transaction status display
 */
private object TransactionTiming {
    const val SUCCESS_DISPLAY_DURATION = 3000L // 3 seconds to show success
    const val FAILURE_DISPLAY_DURATION = 2500L // 2.5 seconds to show failure
    const val FADE_TRANSITION_DURATION = 500L  // Fade transition overlap
}

/**
 * Route for the Send Transaction screen using the unified overlay
 */
@Composable
fun SendTransactionScreenRoute(
    onBackClick: () -> Unit,
    sendViewModel: ChatSendViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor
    
    // Observe transaction status from ChatViewModel
    val transactionStatus by chatViewModel.transactionStatus.collectAsStateWithLifecycle()
    
    // Track if transaction has been initiated
    var transactionInitiated by remember { mutableStateOf(false) }
    
    // GIF-enabled image loader for the overlay
    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()
    }

    // Ensure terminal button is removed when navigating away from this screen
    DisposableEffect(Unit) {
        onDispose {
            sendViewModel.onScreenClosed()
        }
    }
    
    // Handle transaction status changes - navigate back after showing result
    LaunchedEffect(transactionStatus) {
        when (transactionStatus) {
            is TransactionStatus.SUCCESS -> {
                // Show success for a moment, then navigate back
                delay(TransactionTiming.SUCCESS_DISPLAY_DURATION)
                sendViewModel.onScreenClosed()
                onBackClick()
                delay(TransactionTiming.FADE_TRANSITION_DURATION)
                chatViewModel.clearTransactionStatus()
            }
            is TransactionStatus.FAILURE -> {
                // Show failure for a moment, then navigate back
                delay(TransactionTiming.FAILURE_DISPLAY_DURATION)
                sendViewModel.onScreenClosed()
                onBackClick()
                delay(TransactionTiming.FADE_TRANSITION_DURATION)
                chatViewModel.clearTransactionStatus()
            }
            else -> { /* PENDING or null - do nothing */ }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        // Show the transaction input overlay only when transaction hasn't been initiated
        if (!transactionInitiated) {
            UnifiedTransactionOverlayRoute(
                mode = TransactionMode.SEND,
                onDismiss = {
                    // Only navigate back if transaction hasn't been initiated
                    // (user cancelled vs transaction in progress)
                    if (!transactionInitiated) {
                        sendViewModel.onScreenClosed()
                        onBackClick()
                    }
                },
                onSendTransaction = { request ->
                    // Mark transaction as initiated - this will hide the input overlay
                    // and show the TransactionStatusOverlay
                    transactionInitiated = true
                    // Execute the actual transaction - TransactionReference will only be sent
                    // after the transaction succeeds (inside executeTransaction)
                    chatViewModel.executeTransaction(request)
                },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }
        
        // Show transaction status overlay when transaction is in progress or completed
        // This overlay handles its own visibility based on the status
        TransactionStatusOverlay(
            status = transactionStatus,
            gifLoader = gifEnabledLoader,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            onDismiss = {
                // Don't clear status here - let the LaunchedEffect handle navigation
            }
        )
    }
}

/**
 * Route for the Request Transaction screen using the unified overlay
 */
@Composable
fun RequestTransactionScreenRoute(
    onBackClick: () -> Unit,
    sendViewModel: ChatSendViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    // Ensure terminal button is removed when navigating away from this screen
    DisposableEffect(Unit) {
        onDispose {
            sendViewModel.onRequestScreenClosed()
        }
    }

    UnifiedTransactionOverlayRoute(
        mode = TransactionMode.REQUEST,
        onDismiss = {
            sendViewModel.onRequestScreenClosed()
            onBackClick()
        },
        onSendRequest = { request ->
            chatViewModel.sendTransactionRequest(request)
            sendViewModel.onRequestScreenClosed()
            onBackClick()
        },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor
    )
}
