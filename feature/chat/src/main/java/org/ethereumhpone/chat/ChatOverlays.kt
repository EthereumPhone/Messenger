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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.ethereumhpone.chat.components.OverlaySendScreen
import org.ethereumhpone.chat.components.OverlaySendScreenRoute
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.model.Conversation

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
    secondaryColor: Color
) {
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
                Actions.SEND -> {
                    OverlaySendScreenRoute(
                        onBackClick = { onActionSelected(Actions.IDLE, false) },
                        onDone = {
                            //TODO: Implement Sending
                        },
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        recipientUiState = recipientUiState,
                    )
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