package org.ethereumhpone.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ethereumhpone.chat.components.RequestTransactionOverlay
import org.ethereumhpone.chat.components.SendTransactionOverlay
import org.ethereumphone.dgenlibrary.SystemColorManager

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
        onDebugSend = { message ->
            chatViewModel.sendMessage(message)
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
    onDebugSend: (String) -> Unit
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
            val amount = request.metadata?.tokenAmount?.takeIf { it.isNotBlank() } ?: "0"
            val symbol = request.metadata?.tokenSymbol?.takeIf { it.isNotBlank() } ?: "TOKEN"
            val target = recipientName?.takeIf { it.isNotBlank() } ?: recipientAddress
            val message = "Request $amount $symbol from $target"
            chatViewModel.sendMessage(message)
            onBackClick()
        },
        primaryColor = SystemColorManager.primaryColor
    )
}
