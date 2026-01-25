package org.ethereumhpone.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ethereumhpone.chat.components.DebugSendPayload
import org.ethereumhpone.chat.components.TransactionMode
import org.ethereumhpone.chat.components.UnifiedTransactionOverlayRoute
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.model.TransactionReference
import org.ethereumphone.model.TransactionReferenceMetadata
import org.ethereumphone.model.TransactionTypes
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Route for the Send Transaction screen using the unified overlay
 */
@Composable
fun SendTransactionScreenRoute(
    onBackClick: () -> Unit,
    sendViewModel: ChatSendViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    // Ensure terminal button is removed when navigating away from this screen
    DisposableEffect(Unit) {
        onDispose {
            sendViewModel.onScreenClosed()
        }
    }

    UnifiedTransactionOverlayRoute(
        mode = TransactionMode.SEND,
        onDismiss = {
            sendViewModel.onScreenClosed()
            onBackClick()
        },
        onSendTransaction = { request ->
            // Convert TransactionRequest to TransactionReference for sending
            val payload = DebugSendPayload(
                amount = request.metadata?.tokenAmount ?: "0",
                tokenSymbol = request.metadata?.tokenSymbol ?: "ETH",
                tokenDecimals = request.metadata?.tokenDecimals ?: 18,
                chainId = request.chainId,
                description = request.metadata?.description ?: ""
            )
            chatViewModel.sendTransactionReference(buildTransactionReference(payload))
            sendViewModel.onScreenClosed()
            onBackClick()
        },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor
    )
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

private fun buildTransactionReference(payload: DebugSendPayload): TransactionReference {
    val amount = payload.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    // Convert human-readable amount to base units (e.g., 10000 tokens with 18 decimals = 10^22)
    // Using String to avoid Long overflow for large amounts
    val baseUnitsStr = try {
        amount
            .setScale(payload.tokenDecimals, RoundingMode.DOWN)
            .multiply(BigDecimal.TEN.pow(payload.tokenDecimals))
            .toBigInteger()
            .toString()
    } catch (_: Exception) {
        "0"
    }
    
    val reference = "0x" + "0".repeat(64)

    return TransactionReference(
        namespace = "eip155",
        networkId = payload.chainId,
        reference = reference,
        metadata = TransactionReferenceMetadata(
            transactionType = TransactionTypes.TRANSFER,
            currency = payload.tokenSymbol,
            amount = baseUnitsStr,
            decimals = payload.tokenDecimals,
            description = payload.description.takeIf { it.isNotBlank() }
        )
    )
}
