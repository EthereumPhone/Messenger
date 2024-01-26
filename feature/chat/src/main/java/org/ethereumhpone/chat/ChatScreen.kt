package org.ethereumhpone.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.Header
import org.ethereumhpone.chat.components.Message
import org.ethereumhpone.chat.components.UserInput
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.core.ethOSTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier
){

//    Column(
//        verticalArrangement = Arrangement.SpaceBetween,
//        modifier = modifier
//            .background(Color.Black)
//            .fillMaxSize()
//    ) {
//
//        Header(
//            name = "Mark Katakowski",
//            image = "",
//            onBackClick = {},
//            isTrailContent = false,
//            trailContent= {},
//        )
//
//
//
        val initialMessages = listOf(
            org.ethereumhpone.chat.model.Message(
                "me",
                "Check it out!",
                "8:07 PM"
            ),
            org.ethereumhpone.chat.model.Message(
                "me",
                "Thank you!",
                "8:06 PM",
                R.drawable.ethos
            ),
            org.ethereumhpone.chat.model.Message(
                "Taylor Brooks",
                "You can use all the same stuff",
                "8:05 PM"
            ),
            org.ethereumhpone.chat.model.Message(
                "Taylor Brooks",
                "@aliconors Take a look at the `Flow.collectAsStateWithLifecycle()` APIs",
                "8:05 PM"
            ),
            org.ethereumhpone.chat.model.Message(
                "John Glenn",
                "Compose newbie as well, have you looked at the JetNews sample? " +
                        "Most blog posts end up out of date pretty fast but this sample is always up to " +
                        "date and deals with async data loading (it's faked but the same idea " +
                        "applies)  https://goo.gle/jetnews",
                "8:04 PM"
            ),
            org.ethereumhpone.chat.model.Message(
                "me",
                "Compose newbie: I’ve scourged the internet for tutorials about async data " +
                        "loading but haven’t found any good ones " +
                        "What’s the recommended way to load async data and emit composable widgets?",
                "8:03 PM"
            )

        )
//
//        val authorMe = "me"
//
//        LazyColumn(
//            reverseLayout = true,
//            modifier = Modifier
//
//                .padding(start = 24.dp, end = 24.dp,)
//        ){
//            initialMessages.forEachIndexed {  index, message ->
//
//                val prevAuthor = initialMessages.getOrNull(index - 1)?.author
//                val nextAuthor = initialMessages.getOrNull(index + 1)?.author
//                val content = initialMessages[index]
//                val isFirstMessageByAuthor = prevAuthor != content.author
//                val isLastMessageByAuthor = nextAuthor != content.author
//
//                item {
//                    Message(
//                        onAuthorClick = {  },
//                        msg = message,
//                        isUserMe = message.author == authorMe,
//                        isFirstMessageByAuthor = isFirstMessageByAuthor,
//                        isLastMessageByAuthor = isLastMessageByAuthor
//                    )
//                }
//
//            }
//        }
//
//
//
//
//        Column {
//            UserInput(onMessageSent = {})
//        }
//
//
//
//
//
//
//    }



    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()
    Scaffold (
        containerColor = Color.Black,
        topBar = {
            Header(
                name = "Mark Katakowski",
                image = "",
                onBackClick = {},
                isTrailContent = false,
                trailContent= {},
            )
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ){ paddingValues ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)) {

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    reverseLayout = true,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp,)
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
                                isUserMe = message.author == "me",
                                isFirstMessageByAuthor = isFirstMessageByAuthor,
                                isLastMessageByAuthor = isLastMessageByAuthor
                            )
                        }

                    }
                }
            }


            UserInput(
                onMessageSent = { content ->
//                    uiState.addMessage(
//                        Message(authorMe, content, timeNow)
//                    )
                },
                resetScroll = {
                    scope.launch {
                        scrollState.scrollToItem(0)
                    }
                },
                // let this element handle the padding so that the elevation is shown behind the
                // navigation bar
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )

        }

    }
}

@Composable
@Preview
fun PreviewChatScreen(){
    ChatScreen()
}