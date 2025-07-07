package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowRightAlt
import androidx.compose.material3.Icon
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
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
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
import org.ethosmobile.components.library.theme.Colors
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TxBubble(
    modifier: Modifier = Modifier,
    messageEntity: Message,
    amount: Double = 0.0,
    token: String = "USD",
    isUserMe: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    sendingReceipient: Recipient,
    receivingReceipient: Recipient,
    onLongClick: () -> Unit = {},
    authorClicked: (String) -> Unit = {},
    onDoubleClick: () -> Unit = {},
    hasReply: Boolean = false,
    isGroup: Boolean,
    isFirstMessageByAuthor: Boolean
) {

    //TODO: Distinction between reply message and normal message
    val hasReply = hasReply

    val Bubbleshape = BubbleShape

    val messageBrush = when(isUserMe){
        true ->  primaryColor
        false -> secondaryColor
    }


    Column (
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = Modifier
            .clip(Bubbleshape)
            .background(messageBrush)
            .padding(end = 12.dp, start = 12.dp, top = 8.dp, bottom = 4.dp).height(IntrinsicSize.Min)
    ){


            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    sendingReceipient.contact?.name.toString(),
                    style = TextStyle(
                        textAlign = TextAlign.Start ,
                        fontFamily = SpaceMono,
                        color = colorFor(sendingReceipient),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                )
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = Icons.Outlined.ArrowRightAlt,
                    tint = dgenTurqoise,
                    contentDescription = "collapse"
                )
                Text(
                    receivingReceipient.contact?.name.toString(),
                    style = TextStyle(
                        textAlign = TextAlign.Start ,
                        fontFamily = SpaceMono,
                        color = colorFor(receivingReceipient),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                )

            }

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "$amount",
                style = TextStyle(
                    textAlign = TextAlign.Start ,
                    fontFamily = PitagonsSans,
                    color = dgenWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
            )
            Text(
                text = "$token",
                style = TextStyle(
                    textAlign = TextAlign.Start ,
                    fontFamily = PitagonsSans,
                    color = dgenWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
            )
        }




       

        FlowRow (
            modifier = Modifier,
            horizontalArrangement = Arrangement.End,
            verticalArrangement = Arrangement.Center
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
                secondaryColor = secondaryColor,
                primaryColor = primaryColor
            )



        }
    }
}

@Composable
@Preview //(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun TxPreviewBubble(){

    val now = Instant.parse("2025-04-17T12:23:05Z")

    val recipientA = Recipient(
        id = "userA",
        address = "0xAbC123CryptoBroWallet",
        ens = "bro.eth",
        contact = Contact("lk1", "Timothy", null, "0x423")
    )

    val recipientB = Recipient(
        id = "userB",
        address = "0x54h5CryptoBroWallet",
        ens = "ski.eth",
        contact = Contact("lk1", "Bob", null, "0x423")
    )
    val msg = Message("19", "thread123", recipientA, now - (35 * 60).seconds, now - (35 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Of course.")

    TxBubble(
        messageEntity = msg,
        isUserMe = true,
        onLongClick = {},
        sendingReceipient = recipientA,
        receivingReceipient = recipientB,
        onDoubleClick = { },
        isGroup = true,
        isFirstMessageByAuthor = false,
        primaryColor = Color.Red,
        secondaryColor = Color.Blue,
    )

}
