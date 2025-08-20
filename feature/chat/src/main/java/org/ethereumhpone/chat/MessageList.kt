package org.ethereumhpone.chat

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.dgenlibrary.ui.theme.SpaceMono
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.components.message.MessageItem
import org.ethereumphone.dgenlibrary.components.GoToBottomFab
import org.ethereumphone.dgenlibrary.components.NewMessagesDivider
import org.ethereumphone.dgenlibrary.components.TimeHeader
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.Message


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    scrollState: LazyListState,
    seenCount: Int,
    chatConversion: Conversation?,
    selectedMessages: List<Message>,
    selectMode: MutableState<Boolean>,
    onToggleSelection: (Message) -> Unit,
    composablePositionState: MutableState<ComposablePosition>,
    player: Player?,
    onPrepareVideo: (Uri) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    openGLColor: Color,
    onUpdateSeenCount: (Int) -> Unit
) {
    val newCount = (messages.size - seenCount).coerceAtLeast(0)
    val coroutineScope = rememberCoroutineScope()

    val showFab by remember {
        derivedStateOf {
            val info = scrollState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            // totalItemsCount includes headers/footers too; -1 to get max index
            lastVisible < (info.totalItemsCount - 1)
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .verticalLazyListScrollbar(
                    autoHide = true,
                    lazyListState = scrollState,
                    scrollBarColor = primaryColor,
                    scrollBarTrackColor = secondaryColor
                )
        ) {
            itemsIndexed(
                items = messages,
                key = { _, message -> message.id }
            ) { index, message ->
                Column(
                    Modifier.animateItemPlacement(
                        animationSpec = tween(durationMillis = 300)
                    )
                ) {
                    val prevMessage = messages.getOrNull(index - 1)
                    val prevAuthor = prevMessage?.recipient?.id
                    val isFirstMessageByAuthor = prevAuthor != message.recipient.id

                    val prevDate =
                        prevMessage?.date?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                    val currentDate =
                        message.date.toLocalDateTime(TimeZone.currentSystemDefault()).date

                    if (index == seenCount && newCount > 0) {
                        NewMessagesDivider(
                            count = newCount,
                            primaryColor = primaryColor
                        )
                    }

                    if (prevDate != currentDate) {
                        TimeHeader(
                            timestamp = message.date,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    }

                    MessageItem(
                        onAuthorClick = { },
                        msg = message,
                        composablePositionState = composablePositionState,
                        player = player,
                        onPrepareVideo = { /* your logic */ },
                        onLongClick = { onToggleSelection(message) },
                        name = "${message.recipient.contact?.name}",
                        isSelected = selectedMessages.contains(message),
                        selectMode = selectMode,
                        isXMTP = true,
                        onSelect = { onToggleSelection(message) },
                        onDoubleClick = { onToggleSelection(message) },
                        isFirstMessageByAuthor = isFirstMessageByAuthor,
                        isGroup = chatConversion?.isGroup == true,
                        isVisible = true,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        openGLColor = openGLColor,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showFab,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((-32).dp, (-64).dp)
        ) {
            GoToBottomFab(
                onClick = {
                    coroutineScope.launch {
                        val lastIndex = messages.lastIndex
                        if(newCount > 0) onUpdateSeenCount(messages.size)
                        scrollState.animateScrollToItem(lastIndex)
                    }
                },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }

        AnimatedVisibility(
            visible = showFab && (newCount > 0),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.TopCenter)

        ) {
            Surface(
                color = primaryColor,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clickable {
                        // jump to the divider
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(seenCount)
                        }
                    }
            ) {
                Text(
                    text = "$newCount new message${if (newCount > 1) "s" else ""}".uppercase(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 1.sp,
                        textDecoration = TextDecoration.None,
                        textAlign = TextAlign.Center
                    ),
                )
            }
        }
    }
}