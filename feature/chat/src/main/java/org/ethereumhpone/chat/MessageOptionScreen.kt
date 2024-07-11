package org.ethereumhpone.chat

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.components.FocusMessage
import org.ethosmobile.components.library.core.ethOSButton

@Composable
fun MessageOptionsScreen(
    modifier: Modifier = Modifier,
    message: org.ethereumhpone.database.model.Message,
    composablePositionState: MutableState<ComposablePosition>,
    focusMode: MutableState<Boolean>,
    onDeleteMessage: (Long) -> Unit = {},
    onDetailMessage: () -> Unit = {}
){
    var deleteConfirmation = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

   Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                focusMode.value = false //else focusMode = true
            }
        ,
    ) {
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
                focusMode = focusMode,
                onDeleteMessage = {
                    deleteConfirmation.value = true
                },
                onDetailMessage = onDetailMessage
            )
        }

       AnimatedVisibility(
           deleteConfirmation.value,
           enter = fadeIn(
               animationSpec = tween(300),
           ),
           exit = fadeOut(
               animationSpec = tween(500,),
           ),
       ){
           DeleteMessage(
               deleteConfirmation,
               message,
               {
                   onDeleteMessage(message.id)
                   Toast.makeText(context,"Message deleted",Toast.LENGTH_SHORT).show()
               },
               focusMode
           )

       }

    }


}


@Composable
fun DeleteMessage(
    deleteConfirmation: MutableState<Boolean>,
    message: org.ethereumhpone.database.model.Message,
    onDeleteMessage: (Long) -> Unit = {},
    focusMode: MutableState<Boolean>,
){
    Dialog(onDismissRequest = {
        deleteConfirmation.value = false
//           onDeleteMessage(message.id)
    }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black,
            contentColor = Color.White,
            border = BorderStroke(width = 1.dp, Color.White),
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),

                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)

                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "",
                            tint = Color.Transparent,//(0xFF24303D),
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)

                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            text = "Remove message",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9FA2A5),
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clickable {
                                    deleteConfirmation.value = false
                                    focusMode.value = false
                                }
                        )
                    }

                    Text(
                        text = "Do you want remove this message?",
                        fontSize = 16.sp,
                        color = Color(0xFF9FA2A5),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal,
                    )

                    ethOSButton(text = "Delete", enabled = true, onClick = {
                        onDeleteMessage(message.id)
                        focusMode.value = false

                    })

                }
            }
        }
    }
}