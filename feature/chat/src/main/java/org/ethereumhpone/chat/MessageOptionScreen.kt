package org.ethereumhpone.chat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.ethereumhpone.chat.components.ComposablePosition
import org.ethereumhpone.chat.components.FocusMessage
import org.ethereumhpone.chat.components.Message

@Composable
fun MessageOptionsScreen(
    modifier: Modifier = Modifier,
    message: org.ethereumhpone.database.model.Message,
    composablePositionState: MutableState<ComposablePosition>,
    focusMode: MutableState<Boolean>,
){
   Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                focusMode.value = false //else focusMode = true
            }
        ,
    ) {
       Log.d("DEBUG", message.address)
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
                onLongClick = {
                    focusMode.value = false
                },
                composablePositionState = composablePositionState,
                focusMode = focusMode
            )
        }
    }
}