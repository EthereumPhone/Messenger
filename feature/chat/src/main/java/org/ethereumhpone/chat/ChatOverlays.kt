package org.ethereumhpone.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.ethereumhpone.chat.components.ActionOverlayScreen
import org.ethereumhpone.chat.components.TransactionAction
import org.ethereumphone.dgenlibrary.theme.dgenBlack

@Composable
fun ChatOverlays(
    showPicker: Boolean,
    currentActions: Actions,
    onActionSelected: (Actions, Boolean) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
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
                
                else -> {}
            }
        }
    }
}