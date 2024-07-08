package org.ethereumhpone.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ethereumhpone.chat.components.Message.ComposablePosition
import org.ethereumhpone.chat.components.FocusMessage

@Composable
fun MessageDetailsScreen(
    modifier: Modifier = Modifier,
    message: org.ethereumhpone.database.model.Message,
    composablePositionState: MutableState<ComposablePosition>,
    focusMode: MutableState<Boolean>,
    onDeleteMessage: (Long) -> Unit = {},
    onDetailMessage: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            ,
    ) {
        Row {

        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment =  if (message.address != "me") Alignment.Start else Alignment.End ,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            FocusMessage(
                msg = message,
                isUserMe = message.isMe(),
                isFirstMessageByAuthor = true,
                isLastMessageByAuthor = false,
                onLongClick = {},
                composablePositionState = composablePositionState,
                focusMode = focusMode,
                onDeleteMessage = {
                    onDeleteMessage(message.id)
                },
                onDetailMessage = onDetailMessage
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {

        }

    }
}