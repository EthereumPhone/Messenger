package org.ethereumhpone.chat.components

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumhpone.chat.components.message.ChatBubbleShape
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.components.message.LastChatBubbleShape
import org.ethereumhpone.chat.components.message.LastUserChatBubbleShape
import org.ethereumhpone.chat.components.message.UserChatBubbleShape
import org.ethereumhpone.chat.components.message.parts.MediaBinder
import org.ethereumhpone.chat.components.message.parts.VCardBinder
import org.ethereumhpone.chat.model.SymbolAnnotationType
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumhpone.chat.util.colorFor
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.theme.dgenGray
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.Message
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    onDetailMessage: () -> Unit = {},
    isGroup: Boolean = false

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
            onPlayVideo = {},
            primaryColor = SystemColorManager.openGLColor,
            secondaryColor = SystemColorManager.secondaryColor,
            isGroup = isGroup
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
    primaryColor: Color,
    secondaryColor: Color,
    isGroup: Boolean = false,
) {

    //TODO: Add replies

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
            .background(
                color = messageBrush
            ),

        ){
        // Show time and sender name in group chats on the first message of the block
        if (isGroup && isFirstMessageByAuthor) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = sdf.format(Date(message.date.toEpochMilliseconds()))
            val displayName = if (isUserMe) "ME" else message.recipient.contact?.name.toString()
            val nameColor = if (isUserMe) primaryColor else colorFor(message.recipient)
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    text = time,
                    style = TextStyle(
                        textAlign = if (isUserMe) TextAlign.End else TextAlign.Start,
                        fontFamily = PitagonsSans,
                        color = dgenGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        textDecoration = TextDecoration.None
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    displayName,
                    style = TextStyle(
                        textAlign = if (isUserMe) TextAlign.End else TextAlign.Start,
                        fontFamily = SpaceMono,
                        color = nameColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            }
        }

        val media = emptyList<MessageEntity>()//message.parts.filter { it.isImage() || it.isVideo() }

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
        val contacts = emptyList<MessageEntity>()// message.parts.filter { it.isVCard() }


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

            val messageBody = message.body

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

            AuthorNameTimestamp(
                message,
                isUserMe = isUserMe,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                showTime = !isGroup
            )



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

    //SelectionContainer {
    BasicText(
        overflow = TextOverflow.Ellipsis,
        text = styledMessage,
        style = style,
        modifier = Modifier
            .heightIn(max = 300.dp)
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
}

