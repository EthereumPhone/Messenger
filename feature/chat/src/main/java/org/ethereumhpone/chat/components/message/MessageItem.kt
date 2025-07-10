package org.ethereumhpone.chat.components.message

import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.pulseOpacity
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.components.ChatItemBubbleV3
import org.ethereumhpone.chat.components.message.parts.MediaBinder
import org.ethereumhpone.chat.components.message.parts.VCardBinder
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumphone.model.Message
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class ComposablePosition(
    var offset: Offset = Offset.Zero,
    var height: Int = 0
)


@Composable
fun MessageItem(
    onAuthorClick: (String) -> Unit,
    msg: Message,
    isSelected: Boolean = false,
    isFirstMessageByAuthor: Boolean,
    composablePositionState: MutableState<ComposablePosition>,
    player: Player?,
    name: String,
    isXMTP: Boolean,
    selectMode: MutableState<Boolean>,
    onPrepareVideo: (Uri) -> Unit,
    onLongClick: () -> Unit = {},
    onSelect: (Message) -> Unit,
    isGroup: Boolean,
    onDoubleClick: () -> Unit,
    isVisible: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    openGLColor: Color
) {

    var positionComp by remember { mutableStateOf(Offset.Zero) }
    var compSize by remember { mutableIntStateOf(0) }


    val alignmessage = Modifier
        .widthIn(max = 300.dp)
        .onGloballyPositioned { coordinates ->
            compSize = coordinates.size.height
            positionComp = coordinates.positionInRoot()
        }

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 32.dp,end = 32.dp),
        horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = alignmessage,
            horizontalAlignment = if(msg.isMe) Alignment.End else Alignment.Start
        ) {

            if (isFirstMessageByAuthor) {
                // Last bubble before next author
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                // Between bubbles
                Spacer(modifier = Modifier.height(4.dp))
            }

            //Animates the bubble based on if its visible or not
            
            ChatItemBubbleV3(
                modifier = alignmessage,
                messageEntity = msg,
                isUserMe = msg.isMe,
                videoPlayer = player,
                onPlayVideo = { onPrepareVideo(it) },
                onLongClick = {
                    composablePositionState.value.height = compSize
                    composablePositionState.value.offset = Offset(positionComp.x, positionComp.y)
                    onLongClick()
                },
                name = name,
                onDoubleClick = onDoubleClick,
                isGroup = isGroup,
                isFirstMessageByAuthor = isFirstMessageByAuthor,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                openGLColor = openGLColor
            )


        }
    }
}



//TIMESTAMP
@Composable
fun AuthorNameTimestamp(
    messageEntity: Message,
    primaryColor: Color,
    secondaryColor: Color,
    isUserMe: Boolean,
    modifier: Modifier = Modifier,
) {

    //Date formating
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = sdf.format(Date(messageEntity.date.toEpochMilliseconds()))

    // Combine author and timestamp for a11y.
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {

        Text(
            text = "$time",
            fontSize = 12.sp,
            fontFamily = SpaceMono,
            modifier = Modifier
                .alignBy(LastBaseline),
            color = if (isUserMe) dgenWhite else primaryColor,
        )

        if (isUserMe){
            Spacer(modifier = Modifier.width(4.dp))

            when {
                messageEntity.isFailedMessage() -> Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = "Go back",
                    tint = dgenWhite,
                    modifier = Modifier.size(16.dp)
                )

                /*
                message.isSending() -> Icon(
                        painter = painterResource(id = R.drawable.unread_icons),//Icons.Filled.CheckCircleOutline,
                        contentDescription = "Go back",
                        tint = Colors.WHITE,
                        modifier = Modifier
                            .size(16.dp)
                            .alpha(0.5f)
                )
                 */


                messageEntity.isDelivered() -> Icon(
                    painter = painterResource(id = R.drawable.read_icons),
                    contentDescription = "Go back",
                    tint = dgenWhite,
                    modifier = Modifier.size(16.dp)
                )
            }

        }

    }
}

 val ChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)
 val UserChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)

 val LastChatBubbleShape = RoundedCornerShape(20.dp, 32.dp, 32.dp, 4.dp)
 val LastUserChatBubbleShape = RoundedCornerShape(32.dp, 20.dp, 4.dp, 32.dp)
 val TxChatBubbleShape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 20.dp)




@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatItemBubble(
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
    primaryColor: Color,
    secondaryColor: Color
) {

    val Bubbleshape = if(isUserMe) {
        if (isFirstMessageByAuthor){
            LastUserChatBubbleShape
        }else{
            UserChatBubbleShape
        }
    } else{
        if (isFirstMessageByAuthor){
            LastChatBubbleShape
        }else{
            ChatBubbleShape
        }
    }

    val messageBrush = when(isUserMe){
        true -> primaryColor
        false -> secondaryColor
    }


    Column (
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier.clip(Bubbleshape)
            .background(messageBrush)
    ){
        val media = emptyList<MessageEntity>() // message.parts.filter { it.isImage() || it.isVideo() }

        if (media.isNotEmpty()) {
            Box(
                modifier = modifier
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 0.dp)
                    .sizeIn(maxHeight = 256.dp, maxWidth = 256.dp))
            {
                MediaBinder(
                    name= name,
                    videoPlayer = videoPlayer,
                    messageEntity = message,
                    onPrepareVideo = { onPlayVideo(it) }
                )
            }
        }

        // vCard
        val contacts = emptyList<MessageEntity>() // message.parts.filter { it.isVCard() }


        if (contacts.isNotEmpty()) {
            Box(
                modifier = modifier
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 0.dp)
                    .sizeIn(maxHeight = 256.dp, maxWidth = 256.dp))
            {
                VCardBinder(message)
            }
        }
        FlowRow (
            modifier = Modifier
                .padding(end = 20.dp, start = 16.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalArrangement = Arrangement.Bottom
        ) {

                val uriHandler = LocalUriHandler.current

                val messageBody = message.body

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
                        messageBrush = messageBrush,
                    )
                }


            Spacer(modifier = Modifier.width(16.dp))

            AuthorNameTimestamp(
                message,
                isUserMe = isUserMe,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )



        }
    }
}



@Composable
fun ClickableMessage(
    messageBrush: Color,
    modifier: Modifier = Modifier,
    styledMessage: AnnotatedString,
    style: TextStyle,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onClick: (Int) -> Unit = {},
    onLongClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},

    ) {

    val maxHeight = 500.dp


    var expanded by remember { mutableStateOf(false) }
    var textHeight by remember { mutableStateOf(0.dp) }

    val animatedHeight by animateDpAsState(targetValue = if (expanded) textHeight else 300.dp)

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val fadeModifier = when(textHeight>maxHeight){
        true -> {
            Modifier.drawWithContent {
                drawContent() // Draw the original content first
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, messageBrush),
                        startY = size.height - 80f, // Start the gradient close to the bottom edge
                        endY = size.height // End exactly at the bottom edge
                    )
                )
            }
        }
        false -> {
            Modifier
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ){
        BasicText(
            text = styledMessage,
            style = style,
            modifier = modifier
                .heightIn(max = animatedHeight) // Setzt die Höhe dynamisch basierend auf dem expandierten Zustand
                .onGloballyPositioned { coordinates ->
                    // Holen der tatsächlichen Höhe des Textes
                    textHeight = coordinates.size.height.dp
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onLongClick()
                        },
                        onTap = { pos ->
                            layoutResult.value?.let { layoutResult ->
                                onClick(layoutResult.getOffsetForPosition(pos))
                            }
                        },
                        onDoubleTap = {
                            onDoubleClick()
                        }

                    )
                }
                .then(
                    if(expanded) {
                        Modifier
                    } else {
                        fadeModifier
                    }

                ),
            onTextLayout = {
                layoutResult.value = it
                onTextLayout(it)
            }
        )

        if (textHeight > maxHeight) {

            Text(
                if (expanded) "Show less" else "Read more...",
                fontSize = 14.sp,
                fontWeight =  FontWeight.SemiBold,
                color = Colors.WHITE,
                fontFamily = Fonts.INTER,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { pos ->
                            expanded = !expanded
                        },


                        )
                }
            )
        }
    }
}





@Preview
@Composable
fun ConversationPreview() {

    /*
    val initialMessages = listOf(
        Message(
            address = "me",
            body = "*txsent,1,0.08,ETH",
            subject = "8:10 PM"
        ),
        Message(
            address = "me",
            body = "Check it out!",
            subject = "8:07 PM"
        ),
        Message(
            address = "me",
            body = "Thank you!",
            subject = "8:06 PM",
            mmsStatus = R.drawable.ethos
        ),
        Message(
            address = "Taylor Brooks",
            body = "You can use all the same stuff",
            subject = "8:05 PM"
        ),
        Message(
            address = "Taylor Brooks",
            body = "@aliconors Take a look at the `Flow.collectAsStateWithLifecycle()` APIs",
            subject = "8:05 PM"
        ),
        Message(
            address = "Taylor Brooks",
            body = "Compose newbie as well, have you looked at the JetNews sample? " +
                    "Most blog posts end up out of date pretty fast but this sample is always up to " +
                    "date and deals with async data loading (it's faked but the same idea " +
                    "applies)  https://goo.gle/jetnews",
            subject = "8:04 PM"
        ),
        Message(
            address = "me",
            body = "Compose newbie: I've scourged the internet for tutorials about async data " +
                    "loading but haven't found any good ones " +
                    "What's the recommended way to load async data and emit composable widgets?",
            subject = "8:03 PM"
        )

    )



    val authorMe = "me"


    LazyColumn(
        reverseLayout = true,
        modifier = Modifier
            .fillMaxSize()
    ){
        for (index in initialMessages.indices) {
            val prevAuthor = initialMessages.getOrNull(index - 1)?.address
            val nextAuthor = initialMessages.getOrNull(index + 1)?.address
            val content = initialMessages[index]
            val isFirstMessageByAuthor = prevAuthor != content.address
            val isLastMessageByAuthor = nextAuthor != content.address
            item {
                ChatItemBubble(
                    message = content,
                    isUserMe = content.address == authorMe,
                    videoPlayer = null,
                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                    onPlayVideo = {},
                    primaryColor = Color.Blue,
                    secondaryColor = Color.LightGray
                )
            }

        }
    }

//    LazyColumn(
//        reverseLayout = true,
//        modifier = Modifier
//            .fillMaxSize()
//    ){
//        initialMessages.forEachIndexed {  index, message ->
//
//            val prevAuthor = initialMessages.getOrNull(index - 1)?.author
//            val nextAuthor = initialMessages.getOrNull(index + 1)?.author
//            val content = initialMessages[index]
//            val isFirstMessageByAuthor = prevAuthor != content.author
//            val isLastMessageByAuthor = nextAuthor != content.author
//
//            item {
//                Message(
//                    onAuthorClick = {  },
//                    msg = message,
//                    isUserMe = message.author == authorMe,
//                    isFirstMessageByAuthor = isFirstMessageByAuthor,
//                    isLastMessageByAuthor = isLastMessageByAuthor
//                )
//            }
//
//        }
//    }

     */
}


