package org.ethereumhpone.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ethereumhpone.chat.components.RequestTransactionOverlay
import org.ethereumhpone.chat.components.DebugSendPayload
import org.ethereumhpone.chat.components.SendTransactionOverlay
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.model.TransactionReference
import org.ethereumphone.model.TransactionReferenceMetadata
import org.ethereumphone.model.TransactionTypes
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun SendTransactionScreenRoute(
    onBackClick: () -> Unit,
    sendViewModel: ChatSendViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val assetsUiState by sendViewModel.tokenAssetState.collectAsStateWithLifecycle()
    val conversationState by sendViewModel.conversation.collectAsStateWithLifecycle()

    val recipientDisplay = remember(conversationState) {
        if (conversationState is ConversationUiState.Success) {
            (conversationState as ConversationUiState.Success).conversation.getHeader()
        } else {
            ""
        }
    }

    DisposableEffect(Unit) {
        onDispose { sendViewModel.onScreenClosed() }
    }

    SendTransactionScreen(
        assetsUiState = assetsUiState,
        recipientDisplay = recipientDisplay,
        onBackClick = onBackClick,
        onReadyToSendChanged = { ready ->
            if (ready) {
                sendViewModel.onScreenOpened()
            } else {
                sendViewModel.onScreenClosed()
            }
        },
        onDebugSend = { payload ->
            chatViewModel.sendTransactionReference(buildTransactionReference(payload))
            onBackClick()
        }
    )
}

@Composable
fun SendTransactionScreen(
    assetsUiState: AssetsUiState,
    recipientDisplay: String,
    onBackClick: () -> Unit,
    onReadyToSendChanged: (Boolean) -> Unit,
    onDebugSend: (DebugSendPayload) -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    SendTransactionOverlay(
        onDismiss = onBackClick,
        onSendTransaction = {},
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        assetsUiState = assetsUiState,
        recipientDisplay = recipientDisplay,
        onReadyToSendChanged = onReadyToSendChanged,
        showDebugAction = true,
        onDebugSend = onDebugSend
    )
}

@Composable
fun RequestTransactionScreenRoute(
    onBackClick: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val recipientUiState by chatViewModel.recipients.collectAsStateWithLifecycle()
    val conversationState by chatViewModel.conversation.collectAsStateWithLifecycle()
    val currentRecipients = recipientUiState

    val recipientAddress = when (currentRecipients) {
        is RecipientUiState.Success -> {
            currentRecipients.recipients.firstOrNull()?.address ?: ""
        }
        else -> ""
    }

    val recipientName = remember(conversationState) {
        if (conversationState is ConversationUiState.Success) {
            (conversationState as ConversationUiState.Success).conversation.getHeader()
        } else {
            null
        }
    }

    RequestTransactionOverlay(
        recipientAddress = recipientAddress,
        recipientName = recipientName,
        onDismiss = onBackClick,
        onSendRequest = { request ->
            chatViewModel.sendTransactionRequest(request)
            onBackClick()
        },
        primaryColor = SystemColorManager.primaryColor
    )
}

private fun buildTransactionReference(payload: DebugSendPayload): TransactionReference {
    val amount = payload.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val baseUnits = try {
        amount
            .setScale(payload.tokenDecimals, RoundingMode.DOWN)
            .multiply(BigDecimal.TEN.pow(payload.tokenDecimals))
            .toLong()
    } catch (_: Exception) {
        0L
    }
    val reference = "0x" + "0".repeat(64)

    return TransactionReference(
        namespace = "eip155",
        networkId = payload.chainId,
        reference = reference,
        metadata = TransactionReferenceMetadata(
            transactionType = TransactionTypes.TRANSFER,
            currency = payload.tokenSymbol,
            amount = baseUnits,
            decimals = payload.tokenDecimals
        )
    )
}
