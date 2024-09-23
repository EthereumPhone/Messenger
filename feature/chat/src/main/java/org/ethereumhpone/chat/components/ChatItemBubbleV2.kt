package org.ethereumhpone.chat.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumhpone.chat.components.message.ClickableMessage
import org.ethereumhpone.chat.components.message.parts.MediaBinder
import org.ethereumhpone.chat.components.message.parts.VCardBinder
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.isImage
import org.ethereumhpone.database.model.isText
import org.ethereumhpone.database.model.isVCard
import org.ethereumhpone.database.model.isVideo
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatItemBubbleV2(
    modifier: Modifier = Modifier,
    message: Message,
    isUserMe: Boolean,
    name: String = "",
    isFirstMessageByAuthor: Boolean,
    videoPlayer: Player?,
    onPlayVideo: (Uri) -> Unit,
    onLongClick: () -> Unit = {},
    authorClicked: (String) -> Unit = {},
    onDoubleClick: () -> Unit = {},
    isXMTP: Boolean = false,
    hasReply: Boolean = false
) {

    //TODO: Distinction between reply message and normal message
    val hasReply = hasReply

    val Bubbleshape = if(isUserMe) {
        if (isFirstMessageByAuthor){
            org.ethereumhpone.chat.components.message.LastUserChatBubbleShape
        }else{
            org.ethereumhpone.chat.components.message.UserChatBubbleShape
        }
    } else{
        if (isFirstMessageByAuthor){
            org.ethereumhpone.chat.components.message.LastChatBubbleShape
        }else{
            org.ethereumhpone.chat.components.message.ChatBubbleShape
        }
    }

    val nogradient = Color(0xFF8C7DF7)
    val xmtpgradient = Color(0xFFF83C40)

    val reciepientcolor = Colors.DARK_GRAY


    val messageBrush = when(isUserMe){
        true -> { //message from user

            if(isXMTP) {
                xmtpgradient
            } else {
                nogradient
            }

        }
        false -> { //message not from user
            reciepientcolor
        }
    }


    Column (
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = Modifier
            .clip(Bubbleshape)
            .background(messageBrush)
            .width(IntrinsicSize.Max)
    ){
        val media = message.parts.filter { it.isImage() || it.isVideo() }

        if (media.isNotEmpty()) {
            Box(
                modifier = modifier
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 0.dp)
                    .sizeIn(maxHeight = 256.dp, maxWidth = 256.dp))
            {
                MediaBinder(
                    name= name,
                    videoPlayer = videoPlayer,
                    message = message,
                    onPrepareVideo = { onPlayVideo(it) }
                )
            }
        }

        // vCard
        val contacts = message.parts.filter { it.isVCard() }


        if (contacts.isNotEmpty()) {
            Box(
                modifier = modifier
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 0.dp)
                    .sizeIn(maxHeight = 256.dp, maxWidth = 256.dp))
            {
                VCardBinder(message)
            }
        }


        if (hasReply){
            Column(
                modifier = modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ){
                Row (
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .clickable {  }
                        .drawBehind {
                            drawRoundRect(
                                Colors.BLACK,
                                alpha = 0.3f,
                                cornerRadius = CornerRadius(12.dp.toPx())
                            )

                        }
                ){
                    Box(
                        Modifier
                            .background(if(isUserMe)  Colors.WHITE else Color(0xFF8C7DF7))
                            .width(6.dp)
                            .fillMaxHeight()
                    ){

                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 12.dp, end = 18.dp)

                    ) {


                            Text(
                                text = if (isUserMe) name else "You",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if(!isUserMe) Color(0xFF8C7DF7) else Colors.WHITE,
                                    fontFamily = Fonts.INTER
                                )
                            )



                        BasicText(
                            text = message.body,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Colors.WHITE,
                                fontFamily = Fonts.INTER
                            ),
                            modifier = Modifier
                                .alpha(0.8f)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            onLongClick()
                                        },
                                        onDoubleTap = {
                                            onDoubleClick()
                                        }

                                    )
                                },

                            )
                    }

                }


            }
        }


        FlowRow (
            modifier = Modifier
                .padding(end = 20.dp, start = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalArrangement = Arrangement.Bottom
        ) {

            val uriHandler = LocalUriHandler.current

            val messageBody = when (message.isSms()) {
                true -> {
                    Log.d("messageBody true",message.body)

                    message.body
                }
                false -> {
                    Log.d("messageBody false",message.body)


                    message.parts
                        .filter { part -> part.isText() }
                        .mapNotNull { part -> part.text }
                        .filter { text -> text.isNotBlank() }
                        .joinToString("\n")
                }
            }
            Log.d("messageBody false after",messageBody)
            if (messageBody.isNotBlank()) {


                val styledMessage = messageFormatter(
                    text = messageBody,
                    primary = isUserMe
                )

                ClickableMessage(
                    styledMessage = styledMessage,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight =  FontWeight.Normal,
                        color = Colors.WHITE,
                        fontFamily = Fonts.INTER
                    ),
                    onLongClick = onLongClick,

                    onClick = {

                        styledMessage
                            .getStringAnnotations(start = it, end = it)
                            .firstOrNull()
                            ?.let { annotation ->
                                when (annotation.tag) {
                                    SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                                    SymbolAnnotationType.PERSON.name -> authorClicked(annotation.item)
                                    else -> Unit
                                }
                            }
                    },
                    onDoubleClick = onDoubleClick,
                    messageBrush = messageBrush
                )
            }


            Spacer(modifier = Modifier.width(16.dp))

            AuthorNameTimestamp(message)



        }
    }

//    }
}



@Preview
@Composable
fun ReplyChatItemBubblePreview() {
    val initialMessages = listOf(

        Message(
            address = "me",
            body = "Check it out!",
            subject = "8:07 PM"
        ),
//        Message(
//            address = "me",
//            body = "Thank you!",
//            subject = "8:06 PM",
//            mmsStatus = R.drawable.ethos
//        ),
        Message(
            address = "me",
            body = "You can use all the same stuff",
            subject = "8:05 PM"
        ),

    )

    val authorMe = "me"


    ChatItemBubbleV2(
        message = initialMessages[1],
        isUserMe = true,
        videoPlayer = null,
        isFirstMessageByAuthor = true,
        onPlayVideo = {}
    )

}
