package org.ethereumhpone.contracts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.ethereumhpone.contracts.ui.ChatListItem
import org.ethereumhpone.contracts.ui.ContactSheet
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.theme.Colors

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ethereumhpone.database.model.Conversation
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun ContactRoute(
    modifier: Modifier = Modifier,
    navigateToChat: (String) -> Unit,
    viewModel: ContactViewModel = hiltViewModel()
){
    val conversations by viewModel.conversations.collectAsStateWithLifecycle(emptyList())
    ContactScreen(
        conversations = conversations,
        navigateToChat = navigateToChat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    conversations: List<Conversation>,
    navigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier
){

    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()


    //ModalSheets
    var showAssetSheet by remember { mutableStateOf(false) }
    val modalAssetSheetState = rememberModalBottomSheetState(true)

    var showCameraWithPerm by remember {
        mutableStateOf(false)
    }



    Scaffold (
        containerColor = Color.Black,
        topBar = {
            ethOSHeader(
                title=  "Messaging",
                titleSize =  24.sp,
                isTrailContent = true,
                trailContent = {

                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(42.dp)
                        ,
                        enabled = true,
                        onClick = {
                            showAssetSheet = true
                        },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ){
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add contact", tint = Colors.WHITE, modifier = Modifier.size(32.dp))
                        }

                    }
                },
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
                    modifier = Modifier.padding(horizontal = 12.dp)
                ){
//                    item {
//                        ChatListItem(
//                            header = "Mark Katakowskihashvili",
//                            subheader = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd ",
//                            ens = "emunsi.eth",
//                            onClick = navigateToChat
//                        )
//                    }
                    conversations.forEach { conversation ->
                        item {
                            ChatListItem(
                                header = conversation.recipients.get(0).contact?.name!!,
                                subheader = conversation.lastMessage?.getText()!!,
                                ens = "",
                                time = convertLongToTime(conversation.lastMessage?.date!!),
                                unreadConversation = conversation.unread,
                                onClick = { navigateToChat(conversation.id.toString()) }
                            )
                        }

                    }

                }
            }
        }

    }

    if(showAssetSheet){
        ModalBottomSheet(
            containerColor= Colors.BLACK,
            contentColor= Colors.WHITE,

            onDismissRequest = {
                scope.launch {
                    modalAssetSheetState.hide()
                }.invokeOnCompletion {
                    if(!modalAssetSheetState.isVisible) showAssetSheet = false
                }
            },
            sheetState = modalAssetSheetState
        ) {

            ContactSheet()

        }
    }
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return format.format(date)
}

fun currentTimeToLong(): Long {
    return System.currentTimeMillis()
}

fun convertDateToLong(date: String): Long {
    val df = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return df.parse(date).time
}




    @Composable
@Preview
fun PreviewChatScreen(){
    ContactScreen(emptyList(),{})
}