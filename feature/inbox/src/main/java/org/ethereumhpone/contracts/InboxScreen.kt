package org.ethereumhpone.contracts

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.example.dgenlibrary.ui.theme.DgenTheme
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGreen
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Date
import java.time.Instant as JavaInstant
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.datetime.Instant
import org.ethereumhpone.contracts.ui.ChatListInfo
import org.ethereumhpone.contracts.ui.ConversationActionButton
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.contacts.NewConversationSheet
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.components.SwipeableListItem
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient


@Composable
fun ContactRoute(
    onConversationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val conversationState by viewModel.conversationState.collectAsStateWithLifecycle()

    InboxScreen(
        modifier = modifier,
        conversationState = conversationState,
        markAccepted = { id, acceptedState -> viewModel.updateConsentState(id, acceptedState) },
        deleteConversation = { id -> viewModel.deleteConversation(id) },
        markArchived = { id, archivedState -> viewModel.setConversationArchived(id, archivedState) },
        resolveENS = viewModel::resolveENS,
        conversationClicked = { id ->
            viewModel.setConversationAsRead(id, true)
            onConversationClick(id)
        },
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
    markAccepted: (String, Boolean) -> Unit,
    markArchived: (String, Boolean) -> Unit,
    resolveENS: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val coroutineScope = rememberCoroutineScope()


    val contactsPermissionsToRequest = listOf(
        Manifest.permission.READ_CONTACTS,
    )

    val contactsPermissionState = rememberMultiplePermissionsState(permissions = contactsPermissionsToRequest)

    var showNewConversationSheet by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        Column() {
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
                        }
                ){
                    Icon(
                        painter = painterResource(R.drawable.searchicon),
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
                        showNewConversationSheet = true
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


                    Box(modifier = Modifier.fillMaxSize()) {
                        //TODO: Improve Inbox and Requests
                        HorizontalPager(state = pagerState) { page ->
                            when (page) {
                                0 -> {
                                    val conversations = remember { mutableStateListOf<Conversation>().apply { addAll(conversationState.conversations) } }

                                    if (conversationState.conversations.isNotEmpty()){
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                        ) {
                                            item{
                                                Spacer(modifier = modifier.height(64.dp))
                                            }
                                            itemsIndexed(
                                                items = conversations,
                                            ) { index, conversation ->
                                                SwipeableListItem(
                                                    isRevealed = conversation.isOptionsRevealed,
                                                    onExpanded = {
                                                        conversations[index] = conversation.copy(isOptionsRevealed = true)
                                                    },
                                                    onCollapsed = {
                                                        conversations[index] = conversation.copy(isOptionsRevealed = false)
                                                    },
                                                    actions = {
                                                        ConversationActionButton(
                                                            onClick = {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Contact ${conversation.id} was deleted.",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                conversations.remove(conversation)
                                                            },
                                                            icon = Icons.Outlined.Delete,
                                                            iconColor = dgenRed,
                                                            iconSize = 48.dp,
                                                            modifier = Modifier.fillMaxHeight()
                                                        )

                                                    },
                                                ) {
                                                    ChatListInfo(
                                                        //TODO: Improve group identification
                                                        isGroup = conversation.recipients.size > 1,
                                                        lastPerson = conversation.lastMessage?.recipient?.contact?.name.toString(),
                                                        header = conversation.getHeader(),
                                                        subheader = conversation.lastMessage?.body.toString(),
                                                        time = conversation.lastMessage?.date,
                                                        readConversation = conversation.lastMessage?.seen == true,
                                                        onClick = { conversationClicked(conversation.id) },
                                                    )

                                                }
                                            }
                                            item{
                                                Spacer(modifier = modifier.height(24.dp))
                                            }
                                        }
                                    }
                                    else{
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Image(
                                                    modifier = Modifier.size(82.dp),
                                                    contentScale = ContentScale.Crop,
                                                    painter = painterResource(id = org.ethereumhpone.contracts.R.drawable.outline_message_24),
                                                    contentDescription = null,
                                                    colorFilter = ColorFilter.tint(dgenTurqoise)
                                                )
                                                Text(text = "NO CONVERSATIONS",
                                                    style = TextStyle(
                                                        fontFamily = SpaceMono,
                                                        color = dgenTurqoise,
                                                        fontWeight = FontWeight.Normal,
                                                        fontSize = 24.sp,
                                                        letterSpacing = 0.sp,
                                                        textDecoration = TextDecoration.None
                                                    )
                                                )

                                            }
                                        }
                                    }

                                }
                                1 -> {
                                    val conversations = remember { mutableStateListOf<Conversation>().apply { addAll(conversationState.conversations) } }

                                    if (conversationState.conversations.isEmpty()){
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                        ) {
                                            item{
                                                Spacer(modifier = modifier.height(64.dp))
                                            }
                                            itemsIndexed(
                                                items = conversations,
                                            ) { index, conversation ->
                                                SwipeableListItem(
                                                    isRevealed = conversation.isOptionsRevealed,
                                                    onExpanded = {
                                                        conversations[index] = conversation.copy(isOptionsRevealed = true)
                                                    },
                                                    onCollapsed = {
                                                        conversations[index] = conversation.copy(isOptionsRevealed = false)
                                                    },
                                                    actions = {
                                                        ConversationActionButton(
                                                            onClick = {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Contact ${conversation.id} was deleted.",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                conversations.remove(conversation)
                                                            },
                                                            icon = Icons.Outlined.Delete,
                                                            iconColor = dgenRed,
                                                            iconSize = 48.dp,
                                                            modifier = Modifier.fillMaxHeight()
                                                        )

                                                    },
                                                ) {
                                                    val now = Instant.parse("2025-04-10T10:00:00Z")
                                                    ChatListInfo(
                                                        //TODO: Improve group identification
                                                        isGroup = conversation.recipients.size > 1,
                                                        lastPerson = conversation.lastMessage?.recipient?.contact?.name.toString(),
                                                        header = conversation.getHeader(),
                                                        subheader = conversation.lastMessage?.body.toString(),
                                                        time = conversation.lastMessage?.date,
                                                        readConversation = conversation.lastMessage?.seen == true,
                                                        onClick = { conversationClicked(conversation.id) },
                                                    )

                                                }
                                            }
                                            item{
                                                Spacer(modifier = modifier.height(24.dp))
                                            }
                                        }
                                    }
                                    else{
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Image(
                                                    modifier = Modifier.size(82.dp),
                                                    contentScale = ContentScale.Crop,
                                                    painter = painterResource(id = org.ethereumhpone.contracts.R.drawable.outline_message_24),
                                                    contentDescription = null,
                                                    colorFilter = ColorFilter.tint(dgenTurqoise)
                                                )
                                                Text(text = "NO REQUESTS",
                                                    style = TextStyle(
                                                        fontFamily = SpaceMono,
                                                        color = dgenTurqoise,
                                                        fontWeight = FontWeight.Normal,
                                                        fontSize = 24.sp,
                                                        letterSpacing = 0.sp,
                                                        textDecoration = TextDecoration.None
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            //TODO: Add AnimatedVisibilty with enums and make it a composable
                            TabRow(
                                containerColor = dgenBlack,
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
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            dgenBlack,
                                            Color.Transparent
                                        )
                                    )
                                ))
                        }

                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, dgenBlack))))

                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showNewConversationSheet,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            NewConversationSheet(
                onDismiss = { showNewConversationSheet = false },
                onConversationCreated = {
                    showNewConversationSheet = false
                    //TODO: CHANGE TO NOT ONLY LOOK FOR PHONE NUMBER !!!URGENT!!!
                    conversationClicked(it)
                }
            )
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
                deliveryStatus = DeliveryStatus.PUBLISHED,
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

    /*
    InboxScreen(
        conversationState = convo,
        conversationClicked = { it -> }
    )
     */

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

