package org.ethereumhpone.contracts

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.theme.Colors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.dgenlibrary.ui.theme.DgenTheme
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Date
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.datetime.Instant
import org.ethereumhpone.contracts.ui.ChatListItem
import org.ethereumhpone.contracts.ui.ConversationActionButton
import org.ethereumphone.dgenlibrary.components.SwipeableListItem
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import kotlin.reflect.KSuspendFunction1


@Composable
fun ContactRoute(
    onConversationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val conversationState by viewModel.conversationState.collectAsStateWithLifecycle()
    val now = Instant.parse("2025-04-14T10:00:00Z")

    val testConversations = listOf(
        Conversation(
            id = "1",
            title = "Design Chat",
            recipients = listOf(
                Recipient("r1", "0x123", "alice.eth", Contact("lk1", "Alice", null, "0x123"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m1",
                threadId = "1",
                recipient = Recipient("r1", "0x123", "alice.eth", Contact("lk1", "Alice", null, "0x123")),
                date = now,
                dateSent = now,
                seen = true,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = true,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "Let's review the UI"
            ),
            clientInbox = "inbox1"
        ),
        Conversation(
            id = "2",
            title = null,
            recipients = listOf(
                Recipient("r2", "0x456", null, Contact("lk2", "Bob", null, "0x456")),
                Recipient("r3", "0x789", null, Contact("lk3", "Carol", null, "0x789"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m2",
                threadId = "2",
                recipient = Recipient("r2", "0x456", null, Contact("lk2", "Bob", null, "0x456")),
                date = now,
                dateSent = now,
                seen = false,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = false,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "See you tomorrow!"
            ),
            clientInbox = "inbox2"
        ),
        Conversation(
            id = "3",
            title = "🏀 Game Plan",
            recipients = listOf(
                Recipient("r4", "0xabc", null, Contact("lk4", "Coach", null, "0xabc"))
            ),
            draft = "Need to reply...",
            lastMessage = null,
            pinned = true,
            clientInbox = "inbox3"
        ),
        Conversation(
            id = "4",
            title = null,
            recipients = listOf(
                Recipient("r5", "0xdef", null, Contact("lk5", null, null, "0xdef"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m4",
                threadId = "4",
                recipient = Recipient("r5", "0xdef", null, Contact("lk5", null, null, "0xdef")),
                date = now,
                dateSent = now,
                seen = true,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = true,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "Thanks!"
            ),
            clientInbox = "inbox4"
        ),
        Conversation(
            id = "5",
            title = "Dev Team",
            recipients = listOf(
                Recipient("r6", "0xaaa", null, Contact("lk6", "Eve", null, "0xaaa")),
                Recipient("r7", "0xbbb", null, Contact("lk7", "Frank", null, "0xbbb"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m5",
                threadId = "5",
                recipient = Recipient("r6", "0xaaa", null, Contact("lk6", "Eve", null, "0xaaa")),
                date = now,
                dateSent = now,
                seen = true,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = true,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "Pushed the update"
            ),
            archived = true,
            clientInbox = "inbox5"
        ),
        Conversation(
            id = "6",
            title = null,
            recipients = listOf(
                Recipient("r8", "0xccc", null, Contact("lk8", "Grace", null, "0xccc"))
            ),
            draft = "Don't forget the deadline",
            lastMessage = null,
            blocked = true,
            clientInbox = "inbox6"
        ),
        Conversation(
            id = "7",
            title = "Meeting Notes",
            recipients = listOf(
                Recipient("r9", "0xddd", null, Contact("lk9", "Hank", null, "0xddd"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m7",
                threadId = "7",
                recipient = Recipient("r9", "0xddd", null, Contact("lk9", "Hank", null, "0xddd")),
                date = now,
                dateSent = now,
                seen = false,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = false,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "Uploaded the doc"
            ),
            clientInbox = "inbox7"
        ),
        Conversation(
            id = "8",
            title = null,
            recipients = listOf(
                Recipient("r10", "0xeee", null, null)
            ),
            draft = null,
            lastMessage = Message(
                id = "m8",
                threadId = "8",
                recipient = Recipient("r10", "0xeee", null, null),
                date = now,
                dateSent = now,
                seen = false,
                deliveryStatus = DeliveryStatus.FAILED,
                replyReference = null,
                isMe = false,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "What's your ENS?"
            ),
            unknown = true,
            clientInbox = "inbox8"
        )
    )

    val convo = ConversationUIState.Success(
        conversations = testConversations
    )

    InboxScreen(
        modifier = modifier,
        conversationState = convo, //conversationState,
        /*markAccepted = { id, acceptedState -> viewModel.updateConsentState(id, acceptedState) },
        deleteConversation = { id -> viewModel.deleteConversation(id) },
        markArchived = { id, archivedState -> viewModel.setConversationArchived(id, archivedState) },
        resolveENS = viewModel::resolveENS,*/
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
    /*deleteConversation: (String) -> Unit,
    markAccepted: (String, Boolean) -> Unit,
    markArchived: (String, Boolean) -> Unit,
    resolveENS: KSuspendFunction1<String, String>,*/
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

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures {
                            //TODO: implement search
                        }
                        // Never reached
                        //detectDragGestures { _, _ -> log = "Dragging" }
                    }
            ){
                Icon(
                    painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.searchicon),
                    contentDescription = "Search",
                    tint = DgenTheme.colors.dgenTurqoise,
                    modifier = Modifier.size(24.dp)

                )
                Text(
                    text = "SEARCH",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = DgenTheme.colors.dgenTurqoise,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            }

            IconButton(
                onClick = {
                    //TODO: Add Convo
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = dgenTurqoise
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Search",
                    tint = DgenTheme.colors.dgenTurqoise,
                    modifier = Modifier
                        .size(32.dp)
                )
            }


        }


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
                val tabs = listOf("INBOX","REQUESTS")
                // Display 10 items
                val pagerState = rememberPagerState(pageCount = {
                    tabs.size
                })

                //TODO: Add AnimatedVisibilty with enums and make it a composable

                TabRow(
                    containerColor = Colors.TRANSPARENT,
                    contentColor = dgenTurqoise,
                    modifier = Modifier
                        .fillMaxWidth(),
                    selectedTabIndex = pagerState.currentPage,
                    divider = { Divider(color = Colors.TRANSPARENT) },
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.Indicator(
                                color = Colors.TRANSPARENT,
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, s ->
                        val fontColor by animateColorAsState(
                            if(pagerState.currentPage == index) dgenTurqoise else dgenTurqoise.copy(0.5f),
                            tween(300)
                        )
                        //TODO: Make a custom Tab
                        Tab(
                            modifier = Modifier,
                            selectedContentColor = dgenTurqoise,
                            unselectedContentColor = dgenTurqoise.copy(0.5f),
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
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        color = fontColor,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    )
                                )
                            },
                            )
                    }
                }
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> {
                            Box(modifier = Modifier.weight(1f)) {
                                /*
                                LazyColumn(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    conversationState.conversations
                                        .forEach { conversation ->
                                            item {
                                                ChatListItem(
                                                    image = {
                                                        if (conversation.recipients.getOrNull(0)?.contact?.photoUri != null) {
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
                                                    header = conversation.getHeader(),
                                                    subheader = conversation.getSummary(),
                                                    time = conversation.lastMessage?.date,
                                                    unreadConversation = conversation.lastMessage?.seen ?: false, // if the convo has no messages, always display as seen
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
                                 */

                                val conversations = remember { mutableStateListOf<Conversation>().apply { addAll(conversationState.conversations) } }

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    itemsIndexed(
                                        items = conversations,
                                    ) { index, contact ->
                                        SwipeableListItem(
                                            isRevealed = contact.isOptionsRevealed,
                                            onExpanded = {
                                                conversations[index] = contact.copy(isOptionsRevealed = true)
                                            },
                                            onCollapsed = {
                                                conversations[index] = contact.copy(isOptionsRevealed = false)
                                            },
                                            actions = {
                                                ConversationActionButton(
                                                    onClick = {
                                                        Toast.makeText(
                                                            context,
                                                            "Contact ${contact.id} was deleted.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        conversations.remove(contact)
                                                    },
                                                    backgroundColor = Color.Red,
                                                    icon = Icons.Default.Delete,
                                                    modifier = Modifier.fillMaxHeight()
                                                )
                                                ConversationActionButton(
                                                    onClick = {
                                                        conversations[index] = contact.copy(isOptionsRevealed = false)
                                                        Toast.makeText(
                                                            context,
                                                            "Contact ${contact.id} was sent an email.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    backgroundColor = Color.Yellow,
                                                    icon = Icons.Default.Email,
                                                    modifier = Modifier.fillMaxHeight()
                                                )
                                                ConversationActionButton(
                                                    onClick = {
                                                        conversations[index] = contact.copy(isOptionsRevealed = false)
                                                        Toast.makeText(
                                                            context,
                                                            "Contact ${contact.id} was shared.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    backgroundColor = Color.Magenta,
                                                    icon = Icons.Default.Share,
                                                    modifier = Modifier.fillMaxHeight()
                                                )
                                            },
                                        ) {
                                            Text(
                                                text = "Contact ${contact.id}",
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                        }
                        1 -> {
                            Box(modifier = Modifier.weight(1f)) {
                                LazyColumn(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ){
                                    conversationState.conversations.forEach { conversation ->
                                        item {
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
                                                header = conversation.getHeader(),
                                                subheader = conversation.getSummary(),
                                                time = conversation.lastMessage?.date,
                                                unreadConversation = conversation.lastMessage?.seen ?: false, // if the convo has no messages, always display as seen
                                                onClick = {
                                                    conversationClicked(conversation.id)
                                                    //markAccepted(conversation.id, conversation.getConversationTitle())
                                                },
                                                onClickLeft = {
                                                    //markArchived(conversation.id)
                                                },
                                                onClickRight = {
                                                    //markArchived(conversation.id)
                                                    //deleteXMTPConversation(conversation.getConversationTitle())
                                                }
                                            )
                                        }
                                    }
                                }
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
    conversations: List<Conversation>,
    onApprove: (Long, String) -> Unit,
    onDismiss: () -> Unit
) {

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
                    items(conversations) { conversation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "" , //trimEthereumAddress(conversation.getConversationTitle()) + ": " + conversation.lastMessage?.body, // Assuming Conversation has a 'name' property
                                modifier = Modifier.weight(1f),
                                fontFamily = Fonts.INTER,
                                color = Colors.WHITE,
                            )
                            // Icons.Default.Check
                            androidx.compose.material.IconButton(
                                onClick = {
                                    //onApprove(conversation.id, conversation.getConversationTitle())
                                }
                            ) {
                                /*
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Approve hidden conversation",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                */

                            }
                        }
                    }
                }
            }
        }
    }


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


@Composable
@Preview
fun PreviewContactScreen() {
    val now = Instant.parse("2025-04-14T10:00:00Z")

    val testConversations = listOf(
        Conversation(
            id = "1",
            title = "Design Chat",
            recipients = listOf(
                Recipient("r1", "0x123", "alice.eth", Contact("lk1", "Alice", null, "0x123"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m1",
                threadId = "1",
                recipient = Recipient("r1", "0x123", "alice.eth", Contact("lk1", "Alice", null, "0x123")),
                date = now,
                dateSent = now,
                seen = true,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = true,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "Let's review the UI"
            ),
            clientInbox = "inbox1"
        ),
        Conversation(
            id = "2",
            title = null,
            recipients = listOf(
                Recipient("r2", "0x456", null, Contact("lk2", "Bob", null, "0x456")),
                Recipient("r3", "0x789", null, Contact("lk3", "Carol", null, "0x789"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m2",
                threadId = "2",
                recipient = Recipient("r2", "0x456", null, Contact("lk2", "Bob", null, "0x456")),
                date = now,
                dateSent = now,
                seen = false,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = false,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "See you tomorrow!"
            ),
            clientInbox = "inbox2"
        ),
        Conversation(
            id = "3",
            title = "🏀 Game Plan",
            recipients = listOf(
                Recipient("r4", "0xabc", null, Contact("lk4", "Coach", null, "0xabc"))
            ),
            draft = "Need to reply...",
            lastMessage = null,
            pinned = true,
            clientInbox = "inbox3"
        ),
        Conversation(
            id = "4",
            title = null,
            recipients = listOf(
                Recipient("r5", "0xdef", null, Contact("lk5", null, null, "0xdef"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m4",
                threadId = "4",
                recipient = Recipient("r5", "0xdef", null, Contact("lk5", null, null, "0xdef")),
                date = now,
                dateSent = now,
                seen = true,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = true,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "Thanks!"
            ),
            clientInbox = "inbox4"
        ),
        Conversation(
            id = "5",
            title = "Dev Team",
            recipients = listOf(
                Recipient("r6", "0xaaa", null, Contact("lk6", "Eve", null, "0xaaa")),
                Recipient("r7", "0xbbb", null, Contact("lk7", "Frank", null, "0xbbb"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m5",
                threadId = "5",
                recipient = Recipient("r6", "0xaaa", null, Contact("lk6", "Eve", null, "0xaaa")),
                date = now,
                dateSent = now,
                seen = true,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = true,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "Pushed the update"
            ),
            archived = true,
            clientInbox = "inbox5"
        ),
        Conversation(
            id = "6",
            title = null,
            recipients = listOf(
                Recipient("r8", "0xccc", null, Contact("lk8", "Grace", null, "0xccc"))
            ),
            draft = "Don't forget the deadline",
            lastMessage = null,
            blocked = true,
            clientInbox = "inbox6"
        ),
        Conversation(
            id = "7",
            title = "Meeting Notes",
            recipients = listOf(
                Recipient("r9", "0xddd", null, Contact("lk9", "Hank", null, "0xddd"))
            ),
            draft = null,
            lastMessage = Message(
                id = "m7",
                threadId = "7",
                recipient = Recipient("r9", "0xddd", null, Contact("lk9", "Hank", null, "0xddd")),
                date = now,
                dateSent = now,
                seen = false,
                deliveryStatus = DeliveryStatus.PUBLISHED,
                replyReference = null,
                isMe = false,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "Uploaded the doc"
            ),
            clientInbox = "inbox7"
        ),
        Conversation(
            id = "8",
            title = null,
            recipients = listOf(
                Recipient("r10", "0xeee", null, null)
            ),
            draft = null,
            lastMessage = Message(
                id = "m8",
                threadId = "8",
                recipient = Recipient("r10", "0xeee", null, null),
                date = now,
                dateSent = now,
                seen = false,
                deliveryStatus = DeliveryStatus.FAILED,
                replyReference = null,
                isMe = false,
                attachments = emptyList(),
                reactions = emptyList(),
                body = "What's your ENS?"
            ),
            unknown = true,
            clientInbox = "inbox8"
        )
    )

    val convo = ConversationUIState.Success(
        conversations = testConversations
    )

    InboxScreen(
        conversationState = convo,
        conversationClicked = { it -> }
    )
    /*
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
     */

}

