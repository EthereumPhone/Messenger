package org.ethereumhpone.chat.components

import android.app.Notification
import android.util.Log
import android.view.textclassifier.ConversationActions
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.model.MockMessage

import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.Message
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts



@Composable
fun TxMessage(
    msg: Message,
    onAuthorClick: (String) -> Unit,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    modifier: Modifier = Modifier
){
    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier
        .padding(top = 8.dp)
        .fillMaxWidth() else Modifier
    val alignmessage = if(isUserMe) Modifier.padding(start = 16.dp) else Modifier.padding(end = 16.dp)

    val txmessage = msg.body.split(",")

    Row(
        modifier = spaceBetweenAuthors,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = alignmessage,
            horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start
        ) {
            TxChatItemBubble(
                network=txmessage.get(1),
                amount= txmessage.get(2).toDouble(),
                symbol= txmessage.get(3),
                isUserMe = isUserMe,
                isLastMessageByAuthor=isLastMessageByAuthor,
            )
            if (isFirstMessageByAuthor) {
                AuthorNameTimestamp(msg)
            }
            if (isFirstMessageByAuthor) {
                // Last bubble before next author
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // Between bubbles
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Message(
    modifier: Modifier = Modifier,
    msg: Message, //Message from core/model
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    composablePositionState: MutableState<ComposablePosition>,
    onLongClick: () -> Unit = {}
) {

    val c = LocalContext.current

    var positionComp by remember { mutableStateOf(Offset.Zero) }

    var compSize by remember { mutableIntStateOf(0) }

    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier
        .padding(top = 8.dp)
        .fillMaxWidth() else Modifier

    val alignmessage =
        if(isUserMe) {
            Modifier
                .padding(start = 16.dp)
                .onGloballyPositioned { coordinates ->
                    compSize = coordinates.size.height
                    positionComp = coordinates.positionInRoot()
                }
        } else {
            Modifier
                .padding(end = 16.dp)
                .onGloballyPositioned { coordinates ->
                    compSize = coordinates.size.height
                    positionComp = coordinates.positionInRoot()
                }
        }


    Row(
        modifier = spaceBetweenAuthors,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = alignmessage,
            horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start
        ) {
            ChatItemBubble(
                modifier = Modifier,
                message = msg,
                isUserMe = isUserMe,
                isLastMessageByAuthor=isLastMessageByAuthor,
                isFirstMessageByAuthor=isFirstMessageByAuthor,
                onLongClick = {
                    composablePositionState.value.height = compSize
                    composablePositionState.value.offset = Offset(positionComp.x,positionComp.y)
                    onLongClick()
                }
            )
            if (isFirstMessageByAuthor) {
                AuthorNameTimestamp(msg)
            }
            if (isFirstMessageByAuthor) {
                // Last bubble before next author
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // Between bubbles
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}


data class ComposablePosition(
    var offset: Offset = Offset.Zero,
    var height: Int = 0
)





//TIMESTAMP
@Composable
private fun AuthorNameTimestamp(msg: Message, read: Boolean = true) {
    // Combine author and timestamp for a11y.
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {

        Text(
            text = msg.subject,
            fontSize = 12.sp,
            fontFamily = Fonts.INTER,
            modifier = Modifier.alignBy(LastBaseline),
            color = Colors.WHITE,

        )

        Spacer(modifier = Modifier.width(4.dp))

        if(read){
            Icon(
                imageVector = Icons.Filled.CheckCircle,//Icons.Filled.CheckCircleOutline,
                contentDescription = "Go back",
                tint =  Colors.WHITE,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

private val ChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)
private val UserChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)

private val LastChatBubbleShape = RoundedCornerShape(20.dp, 32.dp, 32.dp, 4.dp)
private val LastUserChatBubbleShape = RoundedCornerShape(32.dp, 20.dp, 4.dp, 32.dp)


private val TxChatBubbleShape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 20.dp)


@Composable
fun TxChatItemBubble(
    network: String,
    symbol: String,
    amount: Double,
    isUserMe: Boolean,
    isLastMessageByAuthor: Boolean,
) {

    val networkname = when(network.toInt()){

        1 -> "Mainnet"
        11155111 -> "Sepolia"
        10 -> "Opimism"
        42161 -> "Arbitrum"
        137 -> "Polygon"
        8453 -> "Base"

        else -> {
            "N/A"
        }
    }
    val gradient = Modifier
        .clip(TxChatBubbleShape)
        .background(
            Colors.WHITE
        )
        .border(1.dp, Color(0xFF8C7DF7), TxChatBubbleShape)
    val nogradient = Modifier
        .clip(TxChatBubbleShape)
        .background(
            Colors.WHITE
        )
        .border(1.dp, Color(0xFF8C7DF7), TxChatBubbleShape)

    val usercolor = if(isLastMessageByAuthor) nogradient else gradient

    val reciepientcolor = Modifier
        .clip(TxChatBubbleShape)
        .background(
            Colors.WHITE
        )
        .border(1.dp, Colors.DARK_GRAY, TxChatBubbleShape)


    Column(
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = if(isUserMe) usercolor else reciepientcolor,
            color = Color.Transparent,//backgroundBubbleColor,
            shape = TxChatBubbleShape

        ) {

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Sent",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = if(isUserMe) Color(0xFF8C7DF7) else Colors.DARK_GRAY,
                        fontFamily = Fonts.INTER
                    )
                )


                Text(
                    text = "$amount ${symbol.uppercase()}",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if(isUserMe) Color(0xFF8C7DF7) else Colors.DARK_GRAY,
                        fontFamily = Fonts.INTER
                    )
                )

                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ){
                    Box ( modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isUserMe) Color(0xFF8C7DF7) else Colors.DARK_GRAY)){}
                    Text(
                        text = networkname,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = if(isUserMe) Color(0xFF8C7DF7) else Colors.DARK_GRAY,
                            fontFamily = Fonts.INTER
                        )
                    )
                }

            }



        }
    }
}


@Composable
fun ChatItemBubble(
    modifier: Modifier = Modifier,
    message: Message,
    isUserMe: Boolean,
    isLastMessageByAuthor: Boolean,
    isFirstMessageByAuthor: Boolean,
    onLongClick: () -> Unit = {}
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

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF8C7DF7),
            Color(0xFF6555D8)
        )
    )

    val nogradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF8C7DF7),
            Color(0xFF8C7DF7)
        )
    )

    val reciepientcolor = Brush.verticalGradient(
        colors = listOf(
            Colors.DARK_GRAY,
            Colors.DARK_GRAY
        )
    )

    val messageBrush = when(isUserMe){
        true -> { //message from user
            if(isLastMessageByAuthor){
                nogradient
            } else {
                gradient
            }
        }
        false -> { //message not from user
            reciepientcolor
        }
    }









    Column(
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        Box (
            modifier = modifier
                .clip(Bubbleshape)
                .background(
                    brush = messageBrush
                )
        ){

            val styledMessage = messageFormatter(
                text = message.body,// timestamp
                primary = isUserMe
            )
            val c = LocalContext.current

            BasicText(
                text = styledMessage,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight =  FontWeight.Normal,
                    color = Colors.WHITE,
                    fontFamily = Fonts.INTER
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                Toast.makeText(c,"long press",Toast.LENGTH_SHORT)
                            },
                            onLongPress = {
                                Toast.makeText(c,"long press",Toast.LENGTH_SHORT)
                                onLongClick()
                            }
                        )
                    }

            )
        }
    }
}




@Composable
fun TxClickableMessage(
    network: String,
    symbol: String,
    amount: Double,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val context =  LocalContext.current

    val styledMessage = messageFormatter(
        text = "Sent $amount ${symbol.uppercase()}",
        primary = isUserMe
    )


    ClickableText(
        text = styledMessage,
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight =  FontWeight.SemiBold,
            color = Colors.WHITE,
            fontFamily = Fonts.INTER
        ),
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = {
            Toast.makeText(context, "This is a Sample Toast", Toast.LENGTH_SHORT).show()


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
        }
    )
}

@Preview
@Composable
fun ConversationPreview() {
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
            body = "Compose newbie: I’ve scourged the internet for tutorials about async data " +
                    "loading but haven’t found any good ones " +
                    "What’s the recommended way to load async data and emit composable widgets?",
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
//                Message(
//                    onAuthorClick = {  },
//                    msg = content,
//                    isUserMe = content.address == authorMe,
//                    isFirstMessageByAuthor = isFirstMessageByAuthor,
//                    isLastMessageByAuthor = isLastMessageByAuthor
//                )
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


}