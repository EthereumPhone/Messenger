package org.ethereumhpone.chat.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.ComposablePosition
import org.ethereumhpone.chat.MessageActionList
import org.ethereumhpone.chat.MessageReactions
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.Message
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import kotlin.math.roundToInt

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun FocusMessage(
//    onAuthorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusMode: MutableState<Boolean>,
    msg: Message, //Message from core/model
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    composablePositionState: MutableState<ComposablePosition>,
    onLongClick: () -> Unit = {}

) {

//    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier
//        .padding(top = 8.dp)
//        .fillMaxWidth() else Modifier


    //animate

    val configuration = LocalConfiguration.current
    val screenMidddleHeight = configuration.screenHeightDp/2//mitte des screens

    val pxYToMove = with(LocalDensity.current) {
//        val move = (screenMidddleHeight.dp.toPx() - composablePositionState.value.offset.y.roundToInt())
//        println("move: ${move}, " +
//                "cal: ${screenMidddleHeight.dp.toPx() - composablePositionState.value.offset.y.roundToInt()}, " +
//                "creenMidddleHeight.dp: ${screenMidddleHeight.dp.toPx()}, " +
//                "positionState.value.y): ${composablePositionState.value.offset.y.roundToInt()}")
        val move = (screenMidddleHeight.dp.toPx() - composablePositionState.value.offset.y.roundToInt()) - (composablePositionState.value.height/2).dp.toPx().roundToInt()

        println("move focus: ${move}, ")
        move.roundToInt()
    }

    val animatedProgress = remember { Animatable(composablePositionState.value.offset.y) }//position


    LaunchedEffect(animatedProgress) {
        //composablePositionState.value.offset.y - 60.dp.value - composablePositionState.value.height.toFloat(),
        animatedProgress.animateTo(composablePositionState.value.offset.y + pxYToMove.toFloat(),
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 240
            )
        )
    }


    val composableScope = rememberCoroutineScope()


    if(!focusMode.value){
        composableScope.launch {
            animatedProgress.animateTo(composablePositionState.value.offset.y,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 0
                )
            )


        }

    }

    //aligns messsge and positions it
    val alignmessage =
        if(isUserMe){
            Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(0, animatedProgress.value.toInt()-60.dp.toPx().roundToInt())
                }
        } else {
            Modifier.offset {
                IntOffset(0,animatedProgress.value.toInt()-60.dp.toPx().roundToInt())
            }
        }

//    var positionComp by remember { mutableStateOf(Offset.Zero) }
//
//    var compSize by remember { mutableStateOf(0) }

    val Bubbleshape = if(isUserMe) {
        if (isFirstMessageByAuthor){
            LastUserChatBubbleShape
        }else{
            UserChatBubbleShape
        }
    }
    else{
        if (isFirstMessageByAuthor){
            LastChatBubbleShape
        }else{
            ChatBubbleShape
        }
        //LastChatBubbleShape

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
            modifier = alignmessage,
            horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageReactions()//focusMode = focusMode, composablePositionState = composablePositionState)

            Column(
                //modifier = alignmessage,
                horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start
            ) {
                Surface(
                    modifier = Modifier
                        .clip(Bubbleshape)
                        .background(
                            brush = messageBrush
                        )
                    ,
                    color = Color.Transparent,//backgroundBubbleColor,
                    shape = Bubbleshape

                ) {
                    Column {
                        ClickableMessage(
                            message = msg,
                            isUserMe = isUserMe,
                            onLongClick = {

                                onLongClick()
                            }

                        )
                    }
                }




                if (isFirstMessageByAuthor) {
                    FocusAuthorNameTimestamp(msg)
                }
                if (isFirstMessageByAuthor) {
                    // Last bubble before next author
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    // Between bubbles
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            MessageActionList(
//                focusMode = focusMode, composablePositionState = composablePositionState
//                Modifier
//                    .width((200f * animatedWidth.value).toInt().dp)
            )

        }

}

private val ChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)
private val UserChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)

private val LastChatBubbleShape = RoundedCornerShape(20.dp, 32.dp, 32.dp, 4.dp)
private val LastUserChatBubbleShape = RoundedCornerShape(32.dp, 20.dp, 4.dp, 32.dp)



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusChatItemBubble(
    modifier: Modifier = Modifier,
    message: Message,
    isUserMe: Boolean,
    isLastMessageByAuthor: Boolean,
    isFirstMessageByAuthor: Boolean,
    //composablePositionState: MutableState<ComposablePosition>,
    onLongClick: () -> Unit = {}
) {

    var positionComp by remember { mutableStateOf(Offset.Zero) }

    var compSize by remember { mutableStateOf(0) }

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
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .combinedClickable(
                    onClick = {

                    },
                    onLongClick = {
                        print("Clicked ClickableText")
                    }
                )
                .clip(Bubbleshape)
                .background(
                    brush = messageBrush
                )
                .onGloballyPositioned { coordinates ->
                    compSize = coordinates.size.height
                    positionComp = coordinates.positionInRoot()
                }
            ,
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


//                ClickableMessage(
//                    message = message,
//                    isUserMe = isUserMe,
//                    onLongClick = onLongClick
//
//                )

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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusClickableMessage(
    message:Message,
    isUserMe: Boolean,
    onLongClick: () -> Unit = {}
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
        modifier = Modifier
            .padding(16.dp)
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    onLongClick()
                },
            )
        ,
        onClick = {
            //Toast.makeText(context, "This is a Sample Toast", Toast.LENGTH_SHORT).show()
            onLongClick()

//            styledMessage
//                .getStringAnnotations(start = it, end = it)
//                .firstOrNull()
//                ?.let { annotation ->
//                    when (annotation.tag) {
//                        SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
//                        SymbolAnnotationType.PERSON.name -> authorClicked(annotation.item)
//                        else -> Unit
//                    }
//                }
        }
    )
}


@Composable
private fun FocusAuthorNameTimestamp(msg: Message, read: Boolean = true) {
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
