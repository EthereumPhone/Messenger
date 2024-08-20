package org.ethereumhpone.chat

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import org.ethereumhpone.chat.components.FocusChatItemBubble
import org.ethereumhpone.chat.components.FocusClickableMessage
import org.ethereumhpone.chat.components.FocusMessage
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.components.message.LastUserChatBubbleShape
import org.ethereumhpone.chat.components.printFormattedDateInfo
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.isText
import org.ethosmobile.components.library.core.ethOSButton
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MessageOptionsScreen(
    modifier: Modifier = Modifier,
    message: Message,
    composablePositionState: MutableState<ComposablePosition>,
    focusMode: MutableState<Boolean>,
    onDeleteMessage: (String) -> Unit = {},
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
           },
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
               animationSpec = tween(500),
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
    message: Message,
    onDeleteMessage: (String) -> Unit = {},
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageDetailView(
    message: Message,
    isUserMe: Boolean,
    onDismissRequest: () -> Unit,

) {

    val smsTime: Calendar = Calendar.getInstance()
    smsTime.setTimeInMillis(message.date)

    val now: Calendar = Calendar.getInstance()
    //Date formating
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = sdf.format(Date(message.date))

    val day = if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
        "Today"
    } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
        "Yesterday"
    } else {
        printFormattedDateInfo(Date(message.date))
    }

    var expandedAvailable by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val reusableModifier = Modifier
        .heightIn(max = 500.dp)

    Column(
        modifier = Modifier
            .zIndex(11f)
            .fillMaxSize()
            .background(Colors.BLACK)
            .padding(start = 24.dp,end = 24.dp,bottom = 48.dp),

    ) {
        // Header
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            IconButton(
                onClick = { onDismissRequest() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Go back",
                    tint =  Colors.WHITE,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Colors.DARK_GRAY
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {



                val messageBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8C7DF7),
                        Color(0xFF8C7DF7)
                    )
                )

                Column(
                    horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box (
                        modifier = Modifier
                            .clip(LastUserChatBubbleShape)
                            .background(
                                brush = messageBrush
                            ),
                        contentAlignment = Alignment.BottomEnd
                    ){
                        val uriHandler = LocalUriHandler.current
                        val messageBody = when (message.isSms()) {
                            true -> message.body
                            false -> {
                                message.parts
                                    .filter { part -> part.isText() }
                                    .mapNotNull { part -> part.text }
                                    .filter { text -> text.isNotBlank() }
                                    .joinToString { "\n" }
                            }
                        }
                        if (messageBody.isNotBlank()) {
                            val styledMessage = messageFormatter(
                                text = messageBody,
                                primary = isUserMe
                            )

                            FocusClickableMessage(
                                message = message,
                                styledMessage = styledMessage,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight =  FontWeight.Normal,
                                    color = Colors.WHITE,
                                    fontFamily = Fonts.INTER,
                                ),
                                onLongClick = { },
                                isUserMe = isUserMe,
                                onClick = {

                                    styledMessage
                                        .getStringAnnotations(start = it, end = it)
                                        .firstOrNull()
                                        ?.let { annotation ->
                                            when (annotation.tag) {
                                                SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                                                SymbolAnnotationType.PERSON.name -> {

                                                }
                                                else -> Unit
                                            }
                                        }
                                },
                                modifier = Modifier
                                    .onSizeChanged {
                                        expandedAvailable  = it.height > 250.dp.value.toInt()
                                    }
                                    .animateContentSize()
                                    .then(
                                        if (!expanded) {
                                            reusableModifier
                                        } else {
                                            Modifier
                                        }

                                    )

                            )
                        }
                        




                    }
                    if(expandedAvailable){
                        TextButton(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Colors.TRANSPARENT,
                                contentColor = Colors.WHITE,
                            ),
                            onClick = { expanded = !expanded }
                        ) {
                            Text(text = if(expanded) "Show less" else "Show more", fontFamily = Fonts.INTER)
                        }
                    }

                }


            }
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = Colors.DARK_GRAY
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ){
                    Text("Read", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    when {
//                    message.isFailedMessage() -> Icon(
//                        imageVector = Icons.Rounded.Error,//Icons.Filled.CheckCircleOutline,
//                        contentDescription = "Go back",
//                        tint = Colors.WHITE,
//                        modifier = Modifier
//                            .size(16.dp)
//                            .alpha(0.5f)
//                    )

                        message.isSending() -> Icon(
                            painter = painterResource(id = R.drawable.unread_icons),//Icons.Filled.CheckCircleOutline,
                            contentDescription = "Go back",
                            tint = Colors.WHITE,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(0.9f)
                        )

                        message.isDelivered() -> Icon(
                            painter = painterResource(id = R.drawable.read_icons),//Icons.Filled.CheckCircleOutline,
                            contentDescription = "Go back",
                            tint = Colors.WHITE,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(0.9f)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ){
                    Text("Delivered", color = Colors.WHITE, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    when (message.isDelivered()){
                        true -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (day != null) {
                                    Text(day, color = Colors.GRAY,fontSize = 16.sp, fontWeight = FontWeight.Normal)
                                }
                                Text(time, color = Colors.WHITE,fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        false -> {
                            Text("Not Delivered", color = Colors.GRAY,fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

            }
        }

    }
}