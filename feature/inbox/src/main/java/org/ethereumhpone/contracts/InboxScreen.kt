package org.ethereumhpone.contracts

import android.Manifest
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.theme.Colors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.database.model.ConversationEntity
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Date
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.ethereumhpone.contracts.ui.ChatListItem
import kotlin.reflect.KSuspendFunction1


@Composable
fun ContactRoute(
    onConversationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val conversationState by viewModel.conversationState.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList())

    InboxScreen(
        modifier = modifier,
        conversationState = conversationState,
        markAccepted = { id, address -> viewModel.setConversationAsAccepted(id, address) },
        deleteConversation = { id -> viewModel.deleteConversation(id) },
        markArchived = { id -> viewModel.setConversationArchived(id) },
        resolveENS = viewModel::resolveENS,
        conversationClicked = { id ->
            viewModel.setConversationAsRead(id, true)
            onConversationClick(id)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun InboxScreen(
    conversationState: ConversationUIState,
    conversationClicked: (String) -> Unit,
    deleteConversation: (String) -> Unit,
    markAccepted: (Long, String) -> Unit,
    markArchived: (Long) -> Unit,
    resolveENS: KSuspendFunction1<String, String>,
    modifier: Modifier = Modifier
){

    val context = LocalContext.current

    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val coroutineScope = rememberCoroutineScope()


    val contactsPermissionsToRequest = listOf(
        Manifest.permission.READ_CONTACTS,
    )

    val contactsPermissionState = rememberMultiplePermissionsState(permissions = contactsPermissionsToRequest)

    var showHiddenConversations by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        when(conversationState) {
            is ConversationUIState.Loading ->{
                Box(contentAlignment = Alignment.Center,modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Loading",
                        fontSize = 14.sp,
                        fontFamily = Fonts.INTER,
                        fontWeight = FontWeight.SemiBold,
                        color = Colors.WHITE,
                    )
                }
            }
            is ConversationUIState.Empty ->{
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No conversations",
                        fontSize = 14.sp,
                        fontFamily = Fonts.INTER,
                        fontWeight = FontWeight.SemiBold,
                        color = Colors.WHITE,
                    )
                }
            }

            is ConversationUIState.Success -> {
                val tabs = listOf("Inbox","Requests")
                // Display 10 items
                val pagerState = rememberPagerState(pageCount = {
                    tabs.size
                })

                TabRow(
                    containerColor = Colors.TRANSPARENT,
                    contentColor = Colors.WHITE,
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp,end = 12.dp),
                    selectedTabIndex = pagerState.currentPage,
                    divider = { Divider(color = Colors.TRANSPARENT) },
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.Indicator(
                                color = Colors.WHITE,
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, s ->
                        Tab(
                            selectedContentColor = Colors.WHITE,
                            unselectedContentColor = Colors.GRAY,
                            selected = pagerState.currentPage == index,
                            onClick = {
                                //tabIndex = index
                                coroutineScope.launch {
                                    // Call scroll to on pagerState
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = s,
                                    color = if(pagerState.currentPage == index) Colors.WHITE else Colors.GRAY,
                                    fontSize = 14.sp,
                                    fontFamily = Fonts.INTER,
                                    fontWeight = FontWeight.SemiBold,

                                    )
                            },

                            )
                    }
                }
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> {
                            Box(modifier = Modifier.weight(1f)) {
                                LazyColumn(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    conversationState.conversations
                                        .forEach { conversation ->
                                            item {
                                                ChatListItem(
                                                    image = {
                                                        if (conversation.recipients[0].contact?.photoUri != null) {
                                                            Image(
                                                                painter = rememberAsyncImagePainter(model = conversation.recipients.first().contact?.photoUri), // Replace 'contact.image' with the correct URI variable from your 'Contact' object
                                                                contentDescription = "Contact Image",
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier
                                                                    .size(62.dp) // Set the size of the image
                                                                    .clip(CircleShape) // Apply a circular shape
                                                            )
                                                        } else {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.nouns),
                                                                contentDescription = "Contact Image",
                                                                modifier = Modifier
                                                                    .size(62.dp) // Set the size of the image
                                                                    .clip(CircleShape) // Apply a circular shape
                                                            )
                                                        }
                                                    },
                                                    header = conversation.getTitle(),
                                                    subheader = "", // conversation.lastMessage?.getSummary() ?: "",
                                                    time = Date(), //conversation.lastMessage?.date, // convertLongToTime(conversation.lastMessage?.date ?: 0L),
                                                    unreadConversation = true ,//conversation.unread,
                                                    onClick = {
                                                        conversationClicked(conversation.id.toString())
                                                    },
                                                    onClickLeft = {
                                                        /*
                                                        markArchived(conversation.id)
                                                        if(isEthereumAddress(conversation.getConversationTitle())) {
                                                            deleteXMTPConversation(conversation.getConversationTitle())
                                                        }
                                                         */

                                                    },
                                                    onClickRight = {
                                                        /*
                                                        markArchived(conversation.id)
                                                        if(isEthereumAddress(conversation.getConversationTitle())) {
                                                            deleteXMTPConversation(conversation.getConversationTitle())
                                                        }
                                                         */

                                                    }
                                                )
                                        }
                                    }
                                }
                            }

                        }
                        1 -> {
                            /*
                            if(conversationState.conversations.isNotEmpty()){
                                Box(modifier = Modifier.weight(1f)) {
                                    LazyColumn(
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ){
                                        conversationState.conversations.filter { it.isUnknown }.filter { it.date > 0 }.sortedBy { it.date }.reversed().forEach { conversation ->
                                            item {

                                                val dates = conversation.lastMessage?.date?.let { Date(it) }
                                                ChatListItem(
                                                    image = {
                                                        if (conversation.recipients.get(0).contact?.photoUri != null) {
                                                            Image(
                                                                painter = rememberAsyncImagePainter(model = conversation.recipients.get(0).contact?.photoUri), // Replace 'contact.image' with the correct URI variable from your 'Contact' object
                                                                contentDescription = "Contact Image",
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier
                                                                    .size(62.dp) // Set the size of the image
                                                                    .clip(CircleShape) // Apply a circular shape
                                                            )
                                                        } else {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.nouns),
                                                                contentDescription = "Contact Image",
                                                                modifier = Modifier
                                                                    .size(62.dp) // Set the size of the image
                                                                    .clip(CircleShape) // Apply a circular shape
                                                            )
                                                        }
                                                    },
                                                    header = conversation.recipients.get(0).getDisplayName(),
                                                    subheader = conversation.lastMessage?.getSummary() ?: "",
                                                    time = dates, //conversation.lastMessage?.date, // convertLongToTime(conversation.lastMessage?.date ?: 0L),
                                                    unreadConversation = conversation.unread,
                                                    onClick = {
                                                        conversationClicked(conversation.id.toString())
                                                        markAccepted(conversation.id, conversation.getConversationTitle())
                                                    },
                                                    onClickLeft = {
                                                        markArchived(conversation.id)
                                                    },
                                                    onClickRight = {
                                                        markArchived(conversation.id)
                                                        deleteXMTPConversation(conversation.getConversationTitle())
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            else{
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No conversations",
                                        fontSize = 20.sp,
                                        fontFamily = Fonts.INTER,
                                        fontWeight = FontWeight.Medium,
                                        color = Colors.GRAY,
                                    )
                                }
                            }
                             */
                            //TODO: Add logic (unaccepted messages)


                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No conversations",
                                    fontSize = 20.sp,
                                    fontFamily = Fonts.INTER,
                                    fontWeight = FontWeight.Medium,
                                    color = Colors.GRAY,
                                )
                            }

                        }
                    }
                }
            }
        }
    }

    if(showHiddenConversations){
        // Popup that lists conversations names that have unknown set to true
        /*
        if (conversationState is ConversationUIState.Success) {
            val allConvos = conversationState.conversations
            ShowHiddenConversationsPopup(
                hiddenConversations = allConvos.filter { it.isUnknown },
                onApprove = { id, address ->
                    showHiddenConversations = false
                    markAccepted(id, address)
                },
                onDismiss = { showHiddenConversations = false }
            )
        }
         */

    }
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return format.format(date)
}

@Composable
fun ShowHiddenConversationsPopup(
    hiddenConversationEntities: List<ConversationEntity>,
    onApprove: (Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    /*
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Colors.BLACK
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hidden Conversations",
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontFamily = Fonts.INTER,
                    color = Colors.WHITE,
                )

                LazyColumn {
                    items(hiddenConversations) { conversation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = trimEthereumAddress(conversation.getConversationTitle()) + ": " + conversation.lastMessage?.body, // Assuming Conversation has a 'name' property
                                modifier = Modifier.weight(1f),
                                fontFamily = Fonts.INTER,
                                color = Colors.WHITE,
                            )
                            // Icons.Default.Check
                            androidx.compose.material.IconButton(
                                onClick = {
                                    onApprove(conversation.id, conversation.getConversationTitle())
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Approve hidden conversation",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
     */

}



@Composable
@Preview
fun PreviewShowHiddenConversationsPopup(){

    /*
    ShowHiddenConversationsPopup(
        listOf(
            Conversation(
                id= 0,
                recipients = emptyList(),
                lastMessage =  Message(
                    body = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor"
                ),
                title = "Mark Katakowski"
            )
        ),
        {_,_ ->},
        {}
    )
     */

}

/*
@Composable
@Preview
fun PreviewContactScreen(){
    ContactScreen(
        emptyList(),
        ConversationUIState.Success(
            conversations = listOf(
                Conversation(
                    id= 0,
                    recipients = emptyList(),
                    lastMessage =  Message(
                        body = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor"
                    ),
                    title = "Mark Katakowski"
                )
            )
        ),
        {},
        {},
        {},
        {},
        {_,_ ->},
        {},
        {}
    )
}

 */