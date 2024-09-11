package org.ethereumhpone.chat.components

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumhpone.chat.components.message.ChatItemBubble
import org.ethereumhpone.chat.components.message.ClickableMessage
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.components.message.parts.MediaBinder
import org.ethereumhpone.chat.components.message.parts.VCardBinder
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.isImage
import org.ethereumhpone.database.model.isText
import org.ethereumhpone.database.model.isVCard
import org.ethereumhpone.database.model.isVideo
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
            //isLastMessageByAuthor=isLastMessageByAuthor,
            onLongClick = onLongClick,
            videoPlayer = null,
            onPlayVideo = {}
        )

       MessageActionList(isUserMe = isUserMe, message = msg, focusMode = focusMode, onDeleteMessage = onDeleteMessage,onDetailMessage = onDetailMessage)

    }

}

private val ChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)
private val UserChatBubbleShape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp)

private val LastChatBubbleShape = RoundedCornerShape(20.dp, 32.dp, 32.dp, 4.dp)
private val LastUserChatBubbleShape = RoundedCornerShape(32.dp, 20.dp, 4.dp, 32.dp)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusChatItemBubble(
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
) {

    //TODO: Add replies

    val Bubbleshape = if(isUserMe) {
        if (isFirstMessageByAuthor){
            org.ethereumhpone.chat.components.message.LastUserChatBubbleShape
        }else{
            org.ethereumhpone.chat.components.message.UserChatBubbleShape
        }
    } else{
        if (isFirstMessageByAuthor){
            org.ethereumhpone.chat.components.message.LastChatBubbleShape
        }else{
            org.ethereumhpone.chat.components.message.ChatBubbleShape
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


    Column (
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier
            .clip(Bubbleshape)
            .background(
                brush = messageBrush
            ),

        ){
        val media = message.parts.filter { it.isImage() || it.isVideo() }

        if (media.isNotEmpty()) {
            Box(
                modifier = modifier
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 0.dp)
                    .sizeIn(maxHeight = 256.dp, maxWidth = 256.dp))
            {
                MediaBinder(
                    name= name,
                    videoPlayer = videoPlayer,
                    message = message,
                    onPrepareVideo = { onPlayVideo(it) }
                )
            }
        }

        // vCard
        val contacts = message.parts.filter { it.isVCard() }


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
                .padding(end = 20.dp, start = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalArrangement = Arrangement.Bottom
        ) {

            val uriHandler = LocalUriHandler.current

            val messageBody = when (message.isSms()) {
                true -> message.body
                false -> {
                    message.parts
                        .filter { part -> part.isText() }
                        .mapNotNull { part -> part.text }
                        .filter { text -> text.isNotBlank() }
                        .joinToString("\n")
                }
            }

            if (messageBody.isNotBlank()) {
                val styledMessage = messageFormatter(
                    text = messageBody,
                    primary = isUserMe
                )

                FocusClickableMessage(
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
                    onDoubleClick = onDoubleClick
                )
            }


            Spacer(modifier = Modifier.width(16.dp))

            AuthorNameTimestamp(message)



        }
    }
}
@Composable
fun FocusClickableMessage(
    modifier: Modifier = Modifier,
    styledMessage: AnnotatedString,
    style: TextStyle,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onClick: (Int) -> Unit = {},
    onLongClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},

) {


    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    var columnHeightDp: Dp = 0.dp
    val localDensity = LocalDensity.current

    var tmp = when(columnHeightDp > 300.dp){
        true -> {
            Modifier.animateContentSize()
        }
        false -> {
            Modifier
                .heightIn(max = 300.dp)
        }
    }
    var expanded by remember { mutableStateOf(false) }
    var textHeight by remember { mutableStateOf(0.dp) }

    val animatedHeight by animateDpAsState(targetValue = if (expanded) textHeight else 300.dp)

    Column {
    BasicText(
        overflow = TextOverflow.Ellipsis,
        text = styledMessage,
        style = style,
        modifier = Modifier
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
        ,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        }
    )
        // Wenn der Text höher als 300.dp ist, zeige den Button "Read more" oder "Show less" an
        if (textHeight > 300.dp) {
            TextButton(onClick = {
                expanded = !expanded
            }) {
                Text(if (expanded) "Show less" else "Read more")
            }
        }
        }
}

