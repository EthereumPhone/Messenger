package org.ethereumhpone.chat


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import org.ethereumphone.model.Contact
import org.ethereumhpone.chat.components.message.MessageItem
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.util.generateTestMessages
import org.ethereumphone.dgenlibrary.components.GoToBottomFab
import org.ethereumphone.dgenlibrary.components.NewMessagesDivider
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import kotlin.time.Duration.Companion.seconds

@Composable
fun NewMessage(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

        Surface(
            modifier = modifier.pointerInput(Unit){
                detectTapGestures{
                    onClick()
                }
            },
            shape = RoundedCornerShape(12.dp),
            color = dgenTurqoise
        ) {
            Text(
                text = "New Message".uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenOcean,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
        }

}

@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun NewMessagePreview(){
    NewMessage()
}

@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
private fun PreviewTestScreen() {

    val scrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()


    val showFab by remember {
        derivedStateOf {
            val info = scrollState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            // totalItemsCount includes headers/footers too; -1 to get max index
            lastVisible < (info.totalItemsCount - 1)
        }
    }

    val now = Instant.parse("2025-04-17T12:23:05Z")

    val messageUiState = MessageUiState.Success(generateTestMessages())

    val recipientUiState = RecipientUiState.Success(
        listOf(
            Recipient(
                id = "userB",
                address = "0xDeF456HodlGuyWallet",
                ens = "hodl.eth",
                contact = Contact("lk2", "Bob", null, "0x456")
            )
        )
    )

    var testmessages by remember {
        mutableStateOf(generateTestMessages().toMutableList())
    }

    // 2) Track how many have been "seen"
    var seenCount by remember { mutableIntStateOf(testmessages.size) }
    val newCount = (testmessages.size - seenCount).coerceAtLeast(0)



    LaunchedEffect(scrollState, testmessages.size) {
        snapshotFlow {
            val lastIndex = scrollState.layoutInfo.totalItemsCount - 1
            scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == lastIndex
        }.distinctUntilChanged()
            .collect { isAtBottom ->
                if (isAtBottom) {
                    seenCount = testmessages.size
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()){

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().verticalLazyListScrollbar(scrollState)
        ) {
            itemsIndexed(
                items = testmessages.take(seenCount),
                key = { _, message -> message.id }
            ) { index, message ->

                val prevMessage = testmessages.getOrNull(index - 1)
                val prevAuthor = prevMessage?.recipient?.id
                val isFirstMessageByAuthor = prevAuthor != message.recipient.id

                val prevDate = prevMessage?.date?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                val currentDate = message.date.toLocalDateTime(TimeZone.currentSystemDefault()).date

                //Log.d("List index", index.toString())

                val composablePositionState = remember { mutableStateOf(ComposablePosition()) }
                val selectMode = remember { mutableStateOf(false) }
                val selectedMessagesMap = remember { mutableMapOf<Message, Boolean>() }
                Column {
                    if (prevDate != currentDate) {
                        //TimeHeader(message.date)
                        NewMessagesDivider(96)
                    }

                    MessageItem(
                        onAuthorClick = { },
                        msg = message,
                        composablePositionState = composablePositionState,
                        player = null,
                        onPrepareVideo = { /* your logic */ },
                        onLongClick = { /* your logic */ },
                        name = "${message.recipient.contact?.name}",
                        isSelected = selectedMessagesMap.contains(message),
                        selectMode = selectMode,
                        isXMTP = true,
                        onSelect = { selectedMessage ->
                            selectedMessagesMap.compute(selectedMessage) { _, isChecked ->
                                isChecked?.let { !it } ?: true
                            }
                        },
                        onDoubleClick = {
                            selectMode.value = !selectMode.value
                        },
                        isFirstMessageByAuthor = isFirstMessageByAuthor,
                        isGroup = false,
                        isVisible = true
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
                .offset(-32.dp,-64.dp)
        ) {
            GoToBottomFab(
                onClick = {
                    coroutineScope.launch {
                        val lastIndex = testmessages.lastIndex
                        scrollState.animateScrollToItem(lastIndex)
                    }
                }
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
                color = dgenTurqoise,
                shape = CircleShape,
                elevation = 6.dp,
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

        Button(
            onClick = {
                val nextId = (testmessages.size + 1).toLong()
                testmessages = (testmessages +
                        Message("2", "thread123",
                            Recipient(
                                id = "userB",
                                address = "0xDeF456HodlGuyWallet",
                                ens = "hodl.eth",
                                contact = Contact("lk2", "Bob", null, "0x456")
                            ),
                            now - (59 * 60).seconds,
                            now - (59 * 60).seconds,
                            true,
                            DeliveryStatus.PUBLISHED,
                            null,
                            false,
                            emptyList(),
                            emptyList(),
                            "🚀🚀 New Message $nextId"))
                    .toMutableList()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Simulate incoming")
        }
    }




}

