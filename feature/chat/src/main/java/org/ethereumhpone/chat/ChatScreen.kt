package org.ethereumhpone.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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