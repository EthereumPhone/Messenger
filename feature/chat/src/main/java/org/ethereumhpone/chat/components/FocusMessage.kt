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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.Message
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

    val pxYToMove = with(LocalDensity.current) {
        val move = (screenMidddleHeight.dp.toPx() - composablePositionState.value.offset.y.roundToInt()) - (composablePositionState.value.height/2).dp.toPx().roundToInt()
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
                    IntOffset(0, animatedProgress.value.toInt()-60.dp.toPx().roundToInt())
                }
        } else {
            Modifier.offset {
                IntOffset(0,animatedProgress.value.toInt()-60.dp.toPx().roundToInt())
            }
        }


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
        //TODO: Add Reaction
        //MessageReactions()

        FocusChatItemBubble(
            message = msg,
            isUserMe = isUserMe,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            bubbleshape = Bubbleshape,
            messageBrush = messageBrush,
            onLongClick = onLongClick
        )

       MessageActionList(message = msg, focusMode = focusMode, onDeleteMessage = onDeleteMessage,onDetailMessage = onDetailMessage)

    }

}

private val ChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)
private val UserChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)

private val LastChatBubbleShape = RoundedCornerShape(20.dp, 32.dp, 32.dp, 4.dp)
private val LastUserChatBubbleShape = RoundedCornerShape(32.dp, 20.dp, 4.dp, 32.dp)

@Composable
fun FocusedClickableMessage(
    styledMessage: AnnotatedString,
    style: TextStyle,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onClick: (Int) -> Unit = {},
    onLongClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
) {

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }


    BasicText(
        text = styledMessage,
        style = style,
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        //Toast.makeText(context,"double press",Toast.LENGTH_SHORT)
                        onDoubleClick()
                    },
                    onTap = {
                        //Toast.makeText(context,"long press",Toast.LENGTH_SHORT)
                    },
                    onLongPress = {
                        //Toast.makeText(context,"long press",Toast.LENGTH_SHORT)
                        onLongClick()
                    }
                )
            }
            .pointerInput(onClick) {
                detectTapGestures { pos ->
                    layoutResult.value?.let { layoutResult ->
                        onClick(layoutResult.getOffsetForPosition(pos))
                    }
                }
            },
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        }


    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusChatItemBubble(
    message: Message,
    isUserMe: Boolean,
    messageBrush: Brush,
    bubbleshape:  RoundedCornerShape,
    isFirstMessageByAuthor: Boolean,
    onLongClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(bubbleshape)
                .background(
                    brush = messageBrush
                )
        ) {
            val styledMessage = messageFormatter(
                text = message.body,// timestamp
                primary = isUserMe
            )

            FocusedClickableMessage(
                styledMessage = styledMessage,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight =  FontWeight.Normal,
                    color = Colors.WHITE,
                    fontFamily = Fonts.INTER
                ),
                onLongClick = {
                    onLongClick()
                }
            )
        }
    }
    Column {

        if (isFirstMessageByAuthor) {
            FocusAuthorNameTimestamp(message)
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