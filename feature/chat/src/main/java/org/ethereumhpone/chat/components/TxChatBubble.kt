package org.ethereumhpone.chat.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenWhite
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumhpone.chat.components.message.ClickableMessage
import org.ethereumhpone.chat.components.message.TxChatBubbleShape
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.chat.util.colorFor
import org.ethereumphone.model.Message
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TxBubble(
    modifier: Modifier = Modifier,
    messageEntity: Message,
    isUserMe: Boolean,
    name: String = "",
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
    val usergradient = dgenOcean

    val reciepientcolor = Colors.DARK_GRAY


    val messageBrush = when(isUserMe){
        true ->  usergradient
        false -> reciepientcolor
    }


    Column (
        verticalArrangement = Arrangement.Bottom,
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
                isUserMe,
                modifier = Modifier.padding(start=16.dp, top=4.dp).fillMaxHeight()
            )



        }
    }
}
