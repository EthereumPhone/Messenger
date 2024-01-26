package org.ethereumhpone.chat.components

import android.app.Notification
import android.view.textclassifier.ConversationActions
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
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
import org.ethereumhpone.chat.model.Message
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts


@Composable
fun UserMessage(){

    Column {
        Surface(
            color = Color.Magenta,
            shape = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
        ) {
            //ClickableText(text = , onClick = )
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

    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp).fillMaxWidth() else Modifier
    val alignmessage = if(isUserMe) Modifier.padding(start = 16.dp) else Modifier.padding(end = 16.dp)
    Row(
        modifier = spaceBetweenAuthors,
        horizontalArrangement = Arrangement.End
    ) {
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
        ChatItemBubble(msg, isUserMe, authorClicked = authorClicked, isLastMessageByAuthor=isLastMessageByAuthor)
        if (isLastMessageByAuthor) {
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
private fun AuthorNameTimestamp(msg: Message) {
    // Combine author and timestamp for a11y.
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {

        Text(
            text = msg.timestamp,
            fontSize = 12.sp,
            fontFamily = Fonts.INTER,
            modifier = Modifier.alignBy(LastBaseline),
            color = Colors.GRAY
        )
    }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 32.dp, 32.dp, 20.dp)
private val UserChatBubbleShape = RoundedCornerShape(32.dp, 4.dp, 20.dp, 32.dp)



@Composable
fun ChatItemBubble(
    message: Message,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit,
    isLastMessageByAuthor: Boolean
) {


    val gradient = Modifier.clip(if(isUserMe) UserChatBubbleShape else ChatBubbleShape).background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF8C7DF7),
                Color(0xFF6555D8)
            )
        )
    )
    val nogradient = Modifier.clip(if(isUserMe) UserChatBubbleShape else ChatBubbleShape).background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF8C7DF7),
                Color(0xFF8C7DF7)
            )
        )
    )

    val usercolor = if(isLastMessageByAuthor) nogradient else gradient

    val reciepientcolor = Modifier.clip(if(isUserMe) UserChatBubbleShape else ChatBubbleShape).background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Colors.DARK_GRAY,
                Colors.DARK_GRAY
            )
        )
    )

    Column(
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            modifier = if(isUserMe) usercolor else reciepientcolor,
            color = Color.Transparent,//backgroundBubbleColor,
            shape = if(isUserMe) UserChatBubbleShape else ChatBubbleShape

        ) {
            ClickableMessage(
                message = message,
                isUserMe = isUserMe,
                authorClicked = authorClicked
            )
        }

        message.image?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = if(isUserMe) usercolor else reciepientcolor,
                color = Color.Transparent,
                shape = if(isUserMe) UserChatBubbleShape else ChatBubbleShape
            ) {
                Image(
                    painter = painterResource(it),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(160.dp),
                    contentDescription = "Attached Image"
                )
            }
        }
    }
}

@Composable
fun ClickableMessage(
    message: Message,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = message.content,
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
            "me",
            "Check it out!",
            "8:07 PM"
        ),
        Message(
            "me",
            "Thank you!",
            "8:06 PM",
            R.drawable.ethos
        ),
        Message(
            "Taylor Brooks",
            "You can use all the same stuff",
            "8:05 PM"
        ),
        Message(
            "Taylor Brooks",
            "@aliconors Take a look at the `Flow.collectAsStateWithLifecycle()` APIs",
            "8:05 PM"
        ),
        Message(
            "Taylor Brooks",
            "Compose newbie as well, have you looked at the JetNews sample? " +
                    "Most blog posts end up out of date pretty fast but this sample is always up to " +
                    "date and deals with async data loading (it's faked but the same idea " +
                    "applies)  https://goo.gle/jetnews",
            "8:04 PM"
        ),
        Message(
            "me",
            "Compose newbie: I’ve scourged the internet for tutorials about async data " +
                    "loading but haven’t found any good ones " +
                    "What’s the recommended way to load async data and emit composable widgets?",
            "8:03 PM"
        )

    )

    val authorMe = "me"


//    LazyColumn(
//        reverseLayout = true,
//        modifier = Modifier
//            .fillMaxSize()
//    ){
//        for (index in initialMessages.indices) {
//            val prevAuthor = initialMessages.getOrNull(index - 1)?.author
//            val nextAuthor = initialMessages.getOrNull(index + 1)?.author
//            val content = initialMessages[index]
//            val isFirstMessageByAuthor = prevAuthor != content.author
//            val isLastMessageByAuthor = nextAuthor != content.author
//            item {
//                Message(
//                    onAuthorClick = {  },
//                    msg = content,
//                    isUserMe = content.author == authorMe,
//                    isFirstMessageByAuthor = isFirstMessageByAuthor,
//                    isLastMessageByAuthor = isLastMessageByAuthor
//                )
//            }
//
//        }
//    }

    LazyColumn(
        reverseLayout = true,
        modifier = Modifier
            .fillMaxSize()
    ){
        initialMessages.forEachIndexed {  index, message ->

            val prevAuthor = initialMessages.getOrNull(index - 1)?.author
            val nextAuthor = initialMessages.getOrNull(index + 1)?.author
            val content = initialMessages[index]
            val isFirstMessageByAuthor = prevAuthor != content.author
            val isLastMessageByAuthor = nextAuthor != content.author

            item {
                Message(
                    onAuthorClick = {  },
                    msg = message,
                    isUserMe = message.author == authorMe,
                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                    isLastMessageByAuthor = isLastMessageByAuthor
                )
            }

        }
    }


}