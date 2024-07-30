package org.ethereumhpone.chat.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumhpone.chat.components.message.ChatItemBubble
import org.ethereumhpone.chat.components.message.ClickableMessage
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.isText
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import kotlin.math.roundToInt

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun FocusMessage(
    modifier: Modifier = Modifier,
    focusMode: MutableState<Boolean>,
    msg: Message, //Message from core/model
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    composablePositionState: MutableState<ComposablePosition>,
    onLongClick: () -> Unit = {},
    onDeleteMessage: () -> Unit = {},
    onDetailMessage: () -> Unit = {}

) {

    //animation

    val configuration = LocalConfiguration.current
    val screenMidddleHeight = configuration.screenHeightDp/2//mitte des screens

    val extraheight = if ((composablePositionState.value.height/2).dp > 250.dp) {
        250.dp
    } else{
        (composablePositionState.value.height/2).dp
    }
    val pxYToMove = with(LocalDensity.current) {
        val move = (screenMidddleHeight.dp.toPx() - composablePositionState.value.offset.y.roundToInt()) - extraheight.toPx().roundToInt()
        move.roundToInt()
    }

    val animatedProgress = remember { Animatable(composablePositionState.value.offset.y) }//position


    LaunchedEffect(animatedProgress) {
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
                    IntOffset(
                        0,
                        animatedProgress.value.toInt() - 60.dp
                            .toPx()
                            .roundToInt()
                    )
                }
        } else {
            Modifier.offset {
                IntOffset(0,animatedProgress.value.toInt()-60.dp.toPx().roundToInt())
            }
        }




    Column(
        modifier = alignmessage,
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //TODO: Add Reaction
        //MessageReactions()

        FocusChatItemBubble(
            message = msg,
            isUserMe = isUserMe,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor=isLastMessageByAuthor,
            onLongClick = onLongClick
        )

       MessageActionList(isUserMe = isUserMe, message = msg, focusMode = focusMode, onDeleteMessage = onDeleteMessage,onDetailMessage = onDetailMessage)

    }

}

private val ChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)
private val UserChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)

private val LastChatBubbleShape = RoundedCornerShape(20.dp, 32.dp, 32.dp, 4.dp)
private val LastUserChatBubbleShape = RoundedCornerShape(32.dp, 20.dp, 4.dp, 32.dp)

@Composable
fun FocusClickableMessage(
    modifier: Modifier = Modifier,
    message: Message,
    isUserMe: Boolean,
    styledMessage: AnnotatedString,
    style: TextStyle,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onClick: (Int) -> Unit = {},
    onLongClick: () -> Unit = {},
) {


    val timeModifier = modifier.padding(end = 64.dp)
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    //SelectionContainer {
    BasicText(
        text = styledMessage,
        style = style,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier

            .padding(end = 20.dp, start = 16.dp, top = 8.dp, bottom = 8.dp)
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
//                    .onGloballyPositioned { coordinates ->
//                        row1Size = coordinates.size
//                    }
            .then(timeModifier)
        ,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        }
    )

    AuthorNameTimestamp(message,isUserMe)



}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusChatItemBubble(
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

                FocusClickableMessage(
                    message = message,
                    styledMessage = styledMessage,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight =  FontWeight.Normal,
                        color = Colors.WHITE,
                        fontFamily = Fonts.INTER,
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
                                    SymbolAnnotationType.PERSON.name -> {
                                        authorClicked(annotation.item)
                                    }
                                    else -> Unit
                                }
                            }
                    },
                    modifier = Modifier.heightIn(max = 500.dp)

                )
            }

        }

    }
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