package org.ethereumhpone.chat.components

import android.app.Notification
import android.view.textclassifier.ConversationActions
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
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
//        Text(text = ""+ isFirstMessageByAuthor)
        Column(
            modifier = modifier,
            horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start
        ) {
            TxChatItemBubble(
                network=txmessage.get(1),
                amount= txmessage.get(2).toDouble(),
                symbol= txmessage.get(3),
                isUserMe = isUserMe,
                authorClicked = onAuthorClick,
                isLastMessageByAuthor=isLastMessageByAuthor,
                isFirstMessageByAuthor=isFirstMessageByAuthor
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

@Composable
fun Message(
    onAuthorClick: (String) -> Unit,
    msg: Message, //Message from core/model
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
) {

    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier
        .padding(top = 8.dp)
        .fillMaxWidth() else Modifier
    val alignmessage = if(isUserMe) Modifier.padding(start = 16.dp) else Modifier.padding(end = 16.dp)
    Row(
        modifier = spaceBetweenAuthors,
        horizontalArrangement = Arrangement.End
    ) {
//        Text(text = ""+ isFirstMessageByAuthor)
        AuthorAndTextMessage(
            msg = msg,
            isUserMe = isUserMe,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor = isLastMessageByAuthor,
            authorClicked = onAuthorClick,
            modifier = alignmessage
        )
    }
}

@Composable
fun AuthorAndTextMessage(
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    authorClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start
    ) {
        ChatItemBubble(msg, isUserMe, authorClicked = authorClicked, isLastMessageByAuthor=isLastMessageByAuthor, isFirstMessageByAuthor=isFirstMessageByAuthor)
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
    authorClicked: (String) -> Unit,
    isLastMessageByAuthor: Boolean,
    isFirstMessageByAuthor: Boolean
) {



    val gradient = Modifier
        .clip(TxChatBubbleShape)
        .background(
            Colors.WHITE
        )
        .border(1.dp,Color(0xFF8C7DF7),TxChatBubbleShape)
    val nogradient = Modifier
        .clip(TxChatBubbleShape)
        .background(
            Colors.WHITE
        )
        .border(1.dp,Color(0xFF8C7DF7),TxChatBubbleShape)

    val usercolor = if(isLastMessageByAuthor) nogradient else gradient

    val reciepientcolor = Modifier
        .clip(TxChatBubbleShape)
        .background(
            Colors.WHITE
        )
        .border(1.dp,Colors.DARK_GRAY,TxChatBubbleShape)


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
////                    Text
//                    message.mmsStatus?.let {
//                        Box(
//                            contentAlignment = Alignment.Center,
//                            modifier = Modifier.padding(end = 4.dp,start = 4.dp, top=4.dp)
//                        ){
//                            Image(
//                                painter = painterResource(it),
//                                contentScale = ContentScale.Fit,
//                                modifier = Modifier
//                                    .sizeIn(maxWidth = 240.dp)
//                                    .clip(RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)),
//                                contentDescription = "Attached Image"
//                            )
//                        }
//
//
//                    }

                Text(
                    text = "Sent",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = if(isUserMe) Color(0xFF8C7DF7) else Colors.DARK_GRAY,
                        fontFamily = Fonts.INTER
                    )
                )

//                TxClickableMessage(
//                    network = network,
//                    symbol = symbol,
//                    amount = amount,
//                    isUserMe = isUserMe,
//                    authorClicked = authorClicked
//                )


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
//                    modifier = Modifier.weight()
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)

                ){
                    Box ( modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if(isUserMe) Color(0xFF8C7DF7) else Colors.DARK_GRAY)){}
                    Text(
                        text = "Mainnet",
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
    message: Message,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit,
    isLastMessageByAuthor: Boolean,
    isFirstMessageByAuthor: Boolean
) {

    val Bubbleshape = if(isUserMe) {
        if (isFirstMessageByAuthor){
            LastUserChatBubbleShape
        }else{
            UserChatBubbleShape
        }
        //LastUserChatBubbleShape

    } else{
        if (isFirstMessageByAuthor){
            LastChatBubbleShape
        }else{
            ChatBubbleShape
        }
        //LastChatBubbleShape

    }

    val gradient = Modifier
        .clip(Bubbleshape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF8C7DF7),
                    Color(0xFF6555D8)
                )
            )
        )
    val nogradient = Modifier
        .clip(Bubbleshape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF8C7DF7),
                    Color(0xFF8C7DF7)
                )
            )
        )

    val usercolor = if(isLastMessageByAuthor) nogradient else gradient

    val reciepientcolor = Modifier
        .clip(Bubbleshape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Colors.DARK_GRAY,
                    Colors.DARK_GRAY
                )
            )
        )


    Column(
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = if(isUserMe) usercolor else reciepientcolor,
            color = Color.Transparent,//backgroundBubbleColor,
            shape = Bubbleshape

        ) {

                Column {
////                    Text
//                    message.mmsStatus?.let {
//                        Box(
//                            contentAlignment = Alignment.Center,
//                            modifier = Modifier.padding(end = 4.dp,start = 4.dp, top=4.dp)
//                        ){
//                            Image(
//                                painter = painterResource(it),
//                                contentScale = ContentScale.Fit,
//                                modifier = Modifier
//                                    .sizeIn(maxWidth = 240.dp)
//                                    .clip(RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)),
//                                contentDescription = "Attached Image"
//                            )
//                        }
//
//
//                    }


                    ClickableMessage(
                        message = message,
                        isUserMe = isUserMe,
                        authorClicked = authorClicked
                    )
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
fun ClickableMessage(
    message:Message,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val context =  LocalContext.current

    val styledMessage = messageFormatter(
        text = message.body,// timestamp
        primary = isUserMe
    )

    ClickableText(
        text = styledMessage,
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight =  FontWeight.Normal,
            color = Colors.WHITE,
            fontFamily = Fonts.INTER
        ),
        modifier = Modifier.padding(16.dp),
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
                Message(
                    onAuthorClick = {  },
                    msg = content,
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