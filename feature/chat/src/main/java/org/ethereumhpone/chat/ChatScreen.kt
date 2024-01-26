package org.ethereumhpone.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.ethereumhpone.chat.components.Header
import org.ethereumhpone.chat.components.Message
import org.ethosmobile.components.library.core.ethOSHeader

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier
){

    Column(
        modifier = modifier
            .background(Color.Black)
            .fillMaxSize()
    ) {

        Header(
            name = "Mark Katakowski",
            image = "",
            onBackClick = {},
            isTrailContent = false,
            trailContent= {},
        )



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

        Column {
            //Message()
        }

        Column {
            Row {

            }
        }

    }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
//    ){
//        //Header˙
//
//        //Chat
//
//        //Textfield w/ ctionbar
//    }
}

@Composable
@Preview
fun PreviewChatScreen(){
    ChatScreen()
}