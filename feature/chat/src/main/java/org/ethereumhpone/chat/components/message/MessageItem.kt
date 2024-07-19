package org.ethereumhpone.chat.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.components.message.parts.MediaBinder

import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.isText
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class ComposablePosition(
    var offset: Offset = Offset.Zero,
    var height: Int = 0
)

@Composable
fun TxMessage(
    amount: Double,
    txUrl: String,
    isUserMe: Boolean,
    networkName: String,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    modifier: Modifier = Modifier
){
    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier
        .padding(top = 8.dp)
        .fillMaxWidth() else Modifier


    Row(
        modifier = spaceBetweenAuthors,
        horizontalArrangement = Arrangement.End
    ) {
//        Text(text = ""+ isFirstMessageByAuthor)
        Column(
            modifier = modifier,
            horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start
        ) {
            TxChatItemBubble(
                amount = amount,
                txUrl = txUrl,
                isUserMe = isUserMe,
                networkName = networkName,
                isLastMessageByAuthor=isLastMessageByAuthor,
            )
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

@Composable
fun MessageItem(
    onAuthorClick: (String) -> Unit,
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    composablePositionState: MutableState<ComposablePosition>,
    player: Player? = null,
    onLongClick: () -> Unit = {}
) {

    val context = LocalContext.current
    var positionComp by remember { mutableStateOf(Offset.Zero) }
    var compSize by remember { mutableIntStateOf(0) }

    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier
        .padding(top = 8.dp)
        .fillMaxWidth() else Modifier

    val alignmessage = if(isUserMe) {
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


            // Handle media parts
            MediaBinder(videoPlayer = player, message = msg) {

            }

            //TODO: handle vCard parts




            ChatItemBubble(
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
//            if (isFirstMessageByAuthor) {
//                AuthorNameTimestamp(msg)
//            }
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



//TIMESTAMP
@Composable
private fun AuthorNameTimestamp(
    msg: Message,
    isUserMe: Boolean,
    modifier: Modifier = Modifier,
) {

    //Date formating
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = sdf.format(Date(msg.date))

    val mod = when(isUserMe){
        true -> Modifier.background(Color.Red).padding(end = 16.dp, start = 24.dp).semantics(mergeDescendants = true) {}
        false -> Modifier.background(Color.Green).padding(end = 24.dp).semantics(mergeDescendants = true) {}
    }

    // Combine author and timestamp for a11y.
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.background(Color.Red)
    ) {

        Text(
            text = "$time",
            fontSize = 12.sp,
            fontFamily = Fonts.INTER,
            modifier = Modifier.alignBy(LastBaseline),//.padding(start = 32.dp),
            color = Colors.WHITE,


            )

        Spacer(modifier = Modifier.width(8.dp))

        if(isUserMe){
            if(msg.read){
                Icon(
                    painter = painterResource(id = R.drawable.unread_icons),//Icons.Filled.CheckCircleOutline,
                    contentDescription = "Go back",
                    tint =  Colors.WHITE,
                    modifier = Modifier.size(16.dp)
                )
            }else{
                Icon(
                    painter = painterResource(id = R.drawable.unread_icons),//Icons.Filled.CheckCircleOutline,
                    contentDescription = "Go back",
                    tint =  Colors.WHITE,
                    modifier = Modifier.size(16.dp)
                )
            }
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
    symbol: String = "ETH",
    txUrl: String,
    networkName: String,
    amount: Double,
    isUserMe: Boolean,
    isLastMessageByAuthor: Boolean,
) {

    val uriHandler = LocalUriHandler.current

    val gradient = Modifier
        .clip(TxChatBubbleShape)
        .background(Colors.WHITE)
        .border(1.dp, Color(0xFF8C7DF7), TxChatBubbleShape)

    val nogradient = Modifier
        .clip(TxChatBubbleShape)
        .background(Colors.WHITE)
        .border(1.dp, Color(0xFF8C7DF7), TxChatBubbleShape)

    val usercolor = nogradient

    val reciepientcolor = Modifier
        .clip(TxChatBubbleShape)
        .background(
            Colors.WHITE
        )
        .border(1.dp, Colors.DARK_GRAY, TxChatBubbleShape)


    Column(
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                uriHandler.openUri(txUrl)
            }
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
                val decimalFormat = DecimalFormat("#.#######", DecimalFormatSymbols(Locale.US).apply {
                    decimalSeparator = '.'
                })

                Text(
                    text = "${decimalFormat.format(amount)} ${symbol.uppercase()}",
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
                        text = networkName,
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

//        message.image?.let {
//            Spacer(modifier = Modifier.height(4.dp))
//            Surface(
//                modifier = if(isUserMe) usercolor else reciepientcolor,
//                color = Color.Transparent,
//                shape = if(isUserMe) UserChatBubbleShape else ChatBubbleShape
//            ) {
//                Image(
//                    painter = painterResource(it),
//                    contentScale = ContentScale.Fit,
//                    modifier = Modifier.size(160.dp),
//                    contentDescription = "Attached Image"
//                )
//            }
//        }
    }
}


@Composable
fun ChatItemBubble(
    modifier: Modifier = Modifier,
    message: Message,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit = {},
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
            nogradient

            /*
            if(isLastMessageByAuthor){
                nogradient
            } else {
                gradient
            }
             */

        }
        false -> { //message not from user
            reciepientcolor
        }
    }

    val context = LocalContext.current


    var row1Size by remember { mutableStateOf(IntSize.Zero) }
    var row2Size by remember { mutableStateOf(IntSize.Zero) }

    Column(
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        Box (
            modifier = modifier
                .clip(Bubbleshape)
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

                    ClickableMessage(
                        message = message,
                        isLastMessageByAuthor = isLastMessageByAuthor,
                        styledMessage = styledMessage,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight =  FontWeight.Normal,
                            color = Colors.WHITE,
                            fontFamily = Fonts.INTER
                        ),
                        onLongClick = onLongClick,
                        isUserMe = isUserMe,
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

                    )
                }

        }

    }
}


@Composable
fun ClickableMessage(
    modifier: Modifier = Modifier,
    isLastMessageByAuthor: Boolean,
    message: Message,
    isUserMe: Boolean,
    styledMessage: AnnotatedString,
    style: TextStyle,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onClick: (Int) -> Unit = {},
    onLongClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},

) {

    var row1Size by remember { mutableStateOf(IntSize.Zero) }
    var row2Size by remember { mutableStateOf(IntSize.Zero) }


    val messageModifier = when(isUserMe){
        true -> {
            if (isLastMessageByAuthor){
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            }else {
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            }

        }
        false -> Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    }

    val timeModifier = Modifier.padding(end = 64.dp)



//        Box(
//            modifier = Modifier.padding(16.dp),
//        ){
            val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

            val context = LocalContext.current
            //SelectionContainer {
            BasicText(
                text = styledMessage,
                style = style,
                modifier = Modifier

                    .padding(end = 20.dp,start = 16.dp,top = 16.dp, bottom = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                //Toast.makeText(context,"long press",Toast.LENGTH_SHORT).show()
                                onLongClick()
                            },
                            onTap = { pos ->
                                layoutResult.value?.let { layoutResult ->
                                    //Toast.makeText(context,"tap",Toast.LENGTH_SHORT).show()
                                    onClick(layoutResult.getOffsetForPosition(pos))
                                }
                            }

                        )
                    }
                    .onGloballyPositioned { coordinates ->
                        row1Size = coordinates.size
                    }.then(timeModifier)
                ,
                onTextLayout = {
                    layoutResult.value = it
                    onTextLayout(it)
                }
            )
    

    //Date formating
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = sdf.format(Date(message.date))

    // Combine author and timestamp for a11y.
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(end = 18.dp, bottom = 8.dp).semantics(mergeDescendants = true) {}.onGloballyPositioned { coordinates ->
            row2Size = coordinates.size
        }
    ) {

        Text(
            text = "$time",
            fontSize = 12.sp,
            fontFamily = Fonts.INTER,
            modifier = Modifier.alignBy(LastBaseline).alpha(0.5f),//.padding(start = 32.dp),
            color = Colors.WHITE,
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (isUserMe) {
            if (message.read) {
                Icon(
                    painter = painterResource(id = R.drawable.read_icons),//Icons.Filled.CheckCircleOutline,
                    contentDescription = "Go back",
                    tint = Colors.WHITE,
                    modifier = Modifier.size(16.dp).alpha(0.5f)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.unread_icons),//Icons.Filled.CheckCircleOutline,
                    contentDescription = "Go back",
                    tint = Colors.WHITE,
                    modifier = Modifier.size(16.dp).alpha(0.5f)
                )
            }
        }

    }
}
@Preview
@Composable
fun previewTxClickableMessage() {
    TxClickableMessage(
        txUrl = "eth-mainnet",
        amount = 0.0008,
        isUserMe = true,
    )

}

@Composable
fun TxClickableMessage(
    txUrl: String,
    symbol: String = "ETH",
    amount: Double,
    isUserMe: Boolean,
) {
    val uriHandler = LocalUriHandler.current

    val decimalFormat = DecimalFormat("#.#######", DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
    })


    val styledMessage = messageFormatter(
        text = "Sent ${decimalFormat.format(amount)} ${symbol.uppercase()}",
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
            // Open txUrl in browser
            uriHandler.openUri(txUrl)
        }

    )
}


@Preview
@Composable
fun previewTChatItemBubble() {
    ChatItemBubble(
        message = Message(
            address = "me",
            body = "Check it out!",
            subject = "8:07 PM"
        ),
        isUserMe = true,
        authorClicked = {},
        isLastMessageByAuthor = false,
        isFirstMessageByAuthor= true,
        onLongClick = {}

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
                ChatItemBubble(
                    message = content,
                    isUserMe = content.address == authorMe,
                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                    isLastMessageByAuthor = isLastMessageByAuthor
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


}

