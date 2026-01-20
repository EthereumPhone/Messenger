package org.ethereumhpone.chat

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.ethereumhpone.chat.components.ActionOverlayScreen
import org.ethereumhpone.chat.components.TransactionAction
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.TransactionRequest

@Composable
fun ChatOverlays(
    showPicker: Boolean,
    currentActions: Actions,
    onActionSelected: (Actions, Boolean) -> Unit,
    chatConversion: Conversation?,
    recipientUiState: RecipientUiState,
    media: List<Uri>,
    selectedIndex: Int,
    prevMedia: () -> Unit,
    nextMedia: () -> Unit,
    selectMedia: (Int) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onSendTransaction: () -> Unit = {},
    onSendTransactionRequest: (TransactionRequest) -> Unit = {}
) {
    val showOverlay = remember { mutableStateOf(showPicker) }
    val shouldRotate = remember { mutableStateOf(false) }
    
    // Keep showOverlay synced with showPicker
    androidx.compose.runtime.LaunchedEffect(showPicker) {
        showOverlay.value = showPicker
    }
    
    AnimatedVisibility(
        visible = showPicker,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(dgenBlack)
        ) {
            when (currentActions) {
                Actions.IDLE -> {}
                
                Actions.ACTION_MENU -> {
                    // Show the action menu with Transfer and Request options
                    ActionOverlayScreen(
                        showOverlay = showOverlay,
                        shouldRotate = shouldRotate,
                        onActionSelected = { action ->
                            when (action) {
                                TransactionAction.TRANSFER -> {
                                    onActionSelected(Actions.SEND, true)
                                }
                                TransactionAction.TRANSFER_REQUEST -> {
                                    onActionSelected(Actions.TRANSFER_REQUEST, true)
                                }
                            }
                        },
                        onDismiss = { onActionSelected(Actions.IDLE, false) },
                        primaryColor = primaryColor
                    )
                }
                
                Actions.SEND -> {
                    // Send now navigates to a full screen route
                }
                
                Actions.TRANSFER_REQUEST -> {
                    // Request now navigates to a full screen route
                }
                
                Actions.PHOTO -> {
                    //TODO: Add ImageSelection
                }
                Actions.VIDEO -> {
                    //TODO: add Videopicker
                }
                Actions.CONTACT -> {
                    chatConversion?.isGroup?.let {
                        OverlayContactScreen(
                            onBackClick = { onActionSelected(Actions.IDLE, false) },
                            title = chatConversion.getHeader(),
                            isGroup = it,
                            media = media,
                            transactions = emptyList(), //TODO: Add Transaction
                            recipientUiState = recipientUiState,
                            deleteGroup = {},
                            leaveGroup = {},
                            deleteContact = {},
                            deleteMember = {},
                            selectedIndex = selectedIndex,
                            prev = prevMedia,
                            next = nextMedia,
                            select = selectMedia,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    }
                }
            }
        }
    }
}