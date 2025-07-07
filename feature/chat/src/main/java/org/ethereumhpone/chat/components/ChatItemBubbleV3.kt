package org.ethereumhpone.chat.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.dgenlibrary.ui.theme.PitagonsSans
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import kotlinx.datetime.Instant
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumhpone.chat.components.message.ClickableMessage
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.chat.util.colorFor
import org.ethereumphone.model.Contact
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import kotlin.time.Duration.Companion.seconds


val BubbleShape = RoundedCornerShape(8.dp,8.dp,8.dp,8.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatItemBubbleV3(
    modifier: Modifier = Modifier,
    messageEntity: Message,
    isUserMe: Boolean,
    name: String = "",
    videoPlayer: Player?,
    onPlayVideo: (Uri) -> Unit,
    onLongClick: () -> Unit = {},
    authorClicked: (String) -> Unit = {},
    onDoubleClick: () -> Unit = {},
    hasReply: Boolean = false,
    isGroup: Boolean,
    isFirstMessageByAuthor: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    openGLColor: Color
) {

    //TODO: Distinction between reply message and normal message
    val hasReply = hasReply

    val Bubbleshape = BubbleShape

    val messageBrush = when(isUserMe){
        true -> openGLColor
        false -> secondaryColor
    }

    Column (
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = if (isUserMe) Alignment.End else Alignment.Start,
        modifier = Modifier
            .clip(Bubbleshape)
            .background(messageBrush)
            .padding(end = 12.dp, start = 12.dp, top = 8.dp, bottom = 4.dp)
    ){
        if (isGroup && !isUserMe && isFirstMessageByAuthor){
            Text(
                messageEntity.recipient.contact?.name.toString(),
                style = TextStyle(
                    textAlign = TextAlign.Start ,
                    fontFamily = PitagonsSans,
                    color = colorFor(messageEntity.recipient),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }


        //TODO: Add Images and Videos

        /*
            if (videoPlayer == null) {
                        Box(
                            modifier = modifier.padding(bottom = 6.dp).clip(BubbleShape).size(218.dp).aspectRatio(1f).background(
                                dgenBlack.copy(0.25f)
                            )
                        ) {

                        }
                    }
         */





        FlowRow (
            modifier = Modifier,
            horizontalArrangement = Arrangement.End,
            verticalArrangement = Arrangement.Bottom
        ) {

            val uriHandler = LocalUriHandler.current

            val messageBody = messageEntity.body

            if (messageBody.isNotBlank()) {
                val styledMessage = messageFormatter(
                    text = messageBody,
                    primary = isUserMe
                )

                ClickableMessage(
                    modifier= Modifier.widthIn(max = 300.dp),
                    styledMessage = styledMessage,
                    style = TextStyle(
                        textAlign = TextAlign.Start ,
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
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
                                    SymbolAnnotationType.TOKEN.name -> uriHandler.openUri("https://www.coingecko.com/en/coins/boop-2")
                                    else -> Unit
                                }
                            }
                    },
                    onDoubleClick = onDoubleClick,
                    messageBrush = messageBrush
                )
            }
            AuthorNameTimestamp(
                messageEntity,
                isUserMe = isUserMe,
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .fillMaxHeight(),
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }

    }
}

@Composable
@Preview
fun ChatItemBubbleV3Preview(){
    val recipientB = Recipient(
        id = "userB",
        address = "0x54h5CryptoBroWallet",
        ens = "ski.eth",
        contact = Contact("lk1", "Bob", null, "0x423")
    )
    val now = Instant.parse("2025-04-17T12:23:05Z")

    val recipientA = Recipient(
        id = "userA",
        address = "0xAbC123CryptoBroWallet",
        ens = "bro.eth",
        contact = Contact("lk1", "Timothy", null, "0x423")
    )


    val msg = Message("19", "thread123", recipientA, now - (35 * 60).seconds, now - (35 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Of course.")

    ChatItemBubbleV3(
        messageEntity = msg,
        isUserMe = true,
        videoPlayer = null,
        onPlayVideo = {  },
        onLongClick = {},
        name = recipientA.contact?.name.toString(),
        onDoubleClick = { },
        isGroup = true,
        isFirstMessageByAuthor = false,
        primaryColor = Color.Blue,
        secondaryColor = Color.LightGray,
        openGLColor = Color.Green
    )

}