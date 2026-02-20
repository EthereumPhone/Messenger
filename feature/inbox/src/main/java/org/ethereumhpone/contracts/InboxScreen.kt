package org.ethereumhpone.contracts

import android.Manifest
import android.graphics.Paint.Align
import android.os.Build.VERSION.SDK_INT
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.messenger.terminalsdk.TerminalLEDController
import org.ethosmobile.components.library.theme.Colors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.datetime.Instant
import org.ethereumhpone.contracts.ui.ConversationActionButton
import org.ethereumphone.contacts.NewConversationSheet
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.SearchHeader
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import androidx.compose.material.icons.filled.Check
import com.example.dgenlibrary.ConfirmationOverlay
import com.example.dgenlibrary.InfoScreen
import com.example.dgenlibrary.components.ChatListInfo
import com.example.dgenlibrary.components.SwipeableListItem
import org.ethereumhpone.contracts.utils.printFormattedDateInfo

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactRoute(
    onConversationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val conversationState by viewModel.conversationState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    // Request contacts permission and sync contacts when granted
    val contactsPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.READ_CONTACTS)
    )
    LaunchedEffect(Unit) {
        if (!contactsPermissionState.allPermissionsGranted) {
            contactsPermissionState.launchMultiplePermissionRequest()
        }
    }
    LaunchedEffect(contactsPermissionState.allPermissionsGranted) {
        if (contactsPermissionState.allPermissionsGranted) {
            viewModel.syncContacts()
        }
    }

    InboxScreen(
        modifier = modifier,
        conversationState = conversationState,
        isOnline = isOnline,
        onNewConversationCreated = { id ->
            viewModel.unhideConversation(id)
            onConversationClick(id)
        },
        markAccepted = { id, acceptedState -> viewModel.updateConsentState(id, acceptedState) },
        deleteConversation = { id ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    viewModel.deleteConversation(id)
                    
                    // Success feedback: haptic + LED
                    withContext(Dispatchers.Main) {
                        // Positive haptic feedback for successful deletion
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    // Flash success pattern when conversation deleted
                    TerminalLEDController.flashSuccess()
                } catch (e: Exception) {
                    // Error feedback: haptic + LED
                    withContext(Dispatchers.Main) {
                        // Error haptic feedback
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    // Flash error pattern if deletion fails
                    TerminalLEDController.flashError()
                }
            }
        },
        markArchived = { id, archivedState -> viewModel.setConversationArchived(id, archivedState) },
        resolveENS = viewModel::resolveENS,
        conversationClicked = { id ->
            onConversationClick(id)
        },
        primaryColor = primaryColor,
        secondaryColor  = secondaryColor
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun InboxScreen(
    conversationState: ConversationUIState,
    isOnline: Boolean,
    conversationClicked: (String) -> Unit,
    onNewConversationCreated: (String) -> Unit,
    deleteConversation: (String) -> Unit,
    markAccepted: (String, Boolean) -> Unit,
    markArchived: (String, Boolean) -> Unit,
    resolveENS: (String) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val coroutineScope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    var searchValue by remember { mutableStateOf(TextFieldValue("")) }
    var focusedSearch = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current


    var showNewConversationSheet by remember { mutableStateOf(false) }

    val lazylist = rememberLazyListState()

    // State for delete confirmation overlay
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<String?>(null) }
    var deleteMessage by remember { mutableStateOf("") }

    // State for accept request confirmation overlay
    var showAcceptConfirmation by remember { mutableStateOf(false) }
    var conversationToAccept by remember { mutableStateOf<String?>(null) }
    var acceptMessage by remember { mutableStateOf("") }

    // Keep track of which conversation (if any) currently shows actions
    var expandedConversationId by remember { mutableStateOf<String?>(null) }

    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if ( SDK_INT >= 28 ) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()
    }


    Box(
        Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        Column {
            SearchHeader(
                searchValue,
                { new -> searchValue = new },
                onClearValue = { searchValue = TextFieldValue("") },
                focusManager = focusManager,
                backgroundColor = dgenBlack,
                onAddContact = {
                    showNewConversationSheet = true
                },
                isSearchFocused = focusedSearch,
                focusRequester= focusRequester,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                keyboardController = keyboardController
            )

            // Show no internet screen when offline
            if (!isOnline) {
                InfoScreen(
                    primaryColor = primaryColor,
                    description = "Connect your device to the internet."
                )
            } else {
                when(conversationState) {
                    is ConversationUIState.Loading ->{
                        // Temporarily comment out to prevent double loading matrix
                        // Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        //     DgenLoadingMatrix(activeLEDColor = primaryColor, unactiveLEDColor = secondaryColor)
                        // }
                    }
                    is ConversationUIState.Empty ->{
                        InfoScreen(
                            primaryColor = primaryColor,
                            description = "Start a conversation."
                        )
                    }
                    is ConversationUIState.Success -> {
                        val tabs = listOf("INBOX","REQUESTS")
                        // Display 10 items
                        val pagerState = rememberPagerState(pageCount = {
                            tabs.size
                        })

                        // --- Simple search filter implementation ---
                        val query = searchValue.text.trim()
                        val filteredConversations = if (query.isBlank()) {
                            conversationState.conversations
                        } else {
                            conversationState.conversations.filter { conversation ->
                                // Search in header, summary, last message body, recipient names/addresses/ENS
                                conversation.getHeader().contains(query, ignoreCase = true) ||
                                        conversation.getSummary().contains(query, ignoreCase = true) ||
                                        (conversation.lastMessage?.body?.contains(query, ignoreCase = true) == true) ||
                                        conversation.recipients.any { recipient ->
                                            (recipient.contact?.name?.contains(query, ignoreCase = true) == true) ||
                                                    (recipient.ens?.contains(query, ignoreCase = true) == true) ||
                                                    recipient.address.contains(query, ignoreCase = true)
                                        }
                            }
                        }

                        // Separate inbox and request conversations based on `unknown` flag
                        val inboxConversations = filteredConversations.filter { !it.unknown }
                        val requestConversations = filteredConversations.filter { it.unknown }

                        // Total requests (unfiltered) to display dot indicator
                        val requestCountAll = conversationState.conversations.count { it.unknown }
                        // -------------------------------------------------


                        Column(modifier = Modifier.fillMaxSize()) {
                            //TODO: Improve Inbox and Requests
                            TabRow(
                                containerColor = dgenBlack,
                                contentColor = primaryColor,
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
                                        if(pagerState.currentPage == index) primaryColor else primaryColor.copy(0.5f),
                                        tween(300)
                                    )
                                    //TODO: Make a custom Tab
                                    Tab(
                                        modifier = Modifier,
                                        selectedContentColor = primaryColor,
                                        unselectedContentColor = primaryColor.copy(0.5f),
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
                                                    textDecoration = if(pagerState.currentPage == index) TextDecoration.Underline else TextDecoration.None
                                                )
                                            )
                                        },
                                    )
                                }
                            }
                            HorizontalPager(modifier = Modifier.fillMaxSize(),state = pagerState) { page ->
                                when (page) {
                                    0 -> {

                                        if (inboxConversations.isNotEmpty()){
                                            val conversations = inboxConversations

                                            Box(
                                                Modifier.fillMaxSize()
                                            ) {
                                                LazyColumn(
                                                    state = lazylist,
                                                    modifier = Modifier
                                                        .verticalLazyListScrollbar(
                                                            lazylist,
                                                            scrollBarTrackColor = secondaryColor,
                                                            scrollBarColor = primaryColor,
                                                            autoHide = true,
                                                            trackTopInset = 32.dp,
                                                            trackBottomInset = 32.dp,
                                                        )
                                                        .fillMaxSize()
                                                ) {
                                                    item{
                                                        Spacer(modifier = modifier.fillMaxWidth().height(24.dp))
                                                    }
                                                    itemsIndexed(
                                                        items = conversations,
                                                    ) { index, conversation ->
                                                        SwipeableListItem(
                                                            isRevealed = expandedConversationId == conversation.id && !showDeleteConfirmation && !showAcceptConfirmation,
                                                            onExpanded = {
                                                                expandedConversationId = conversation.id
                                                            },
                                                            onCollapsed = {
                                                                if (expandedConversationId == conversation.id) {
                                                                    expandedConversationId = null
                                                                }
                                                            },
                                                            actions = {
                                                                ConversationActionButton(
                                                                    onClick = {
                                                                        conversationToDelete = conversation.id
                                                                        deleteMessage = "Do you want to delete the conversation with ${conversation.getHeader()}?"
                                                                        showDeleteConfirmation = true
                                                                        expandedConversationId = null // close any revealed rows
                                                                    },
                                                                    icon = Icons.Outlined.Delete,
                                                                    iconColor = primaryColor,
                                                                    iconSize = 100.dp,
                                                                    modifier = Modifier.fillMaxHeight()
                                                                )

                                                            },
                                                        ) {
                                                            ChatListInfo(
                                                                //TODO: Improve group identification
                                                                primaryColor = primaryColor,
                                                                isGroup = conversation.recipients.size > 1,
                                                                header = conversation.getHeader(),
                                                                subheader = conversation.getSummary(),
                                                                time = printFormattedDateInfo(conversation.lastMessage?.date),
                                                                readConversation = conversation.lastMessage?.seen == true,
                                                                onClick = { conversationClicked(conversation.id) },
                                                            )

                                                        }
                                                    }
                                                    item{
                                                        Spacer(modifier = modifier.fillMaxWidth().height(24.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .fillMaxWidth()
                                                    .height(24.dp)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                dgenBlack,
                                                                Color.Transparent
                                                            )
                                                        )
                                                    )
                                                )
                                                Spacer(modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(24.dp)
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color.Transparent,
                                                                dgenBlack
                                                            )
                                                        )
                                                    )
                                                )
                                            }



                                        }
                                        else{
                                            InfoScreen(
                                                primaryColor = primaryColor,
                                                description = "Start a conversation with somebody"
                                            )
                                        }

                                    }
                                    1 -> {

                                        if (requestConversations.isNotEmpty()){
                                            val conversations = requestConversations
                                            Box(
                                                Modifier.fillMaxSize()
                                            ) {
                                                LazyColumn(
                                                    state = lazylist,
                                                    modifier = Modifier
                                                        .verticalLazyListScrollbar(
                                                            lazylist,
                                                            scrollBarTrackColor = secondaryColor,
                                                            scrollBarColor = primaryColor,
                                                            trackTopInset = 48.dp,
                                                            trackBottomInset = 48.dp
                                                        )
                                                        .fillMaxSize()
                                                ) {
                                                    item{
                                                        Spacer(modifier = modifier.fillMaxWidth().height(24.dp))
                                                    }
                                                    itemsIndexed(
                                                        items = conversations,
                                                    ) { index, conversation ->
                                                        SwipeableListItem(
                                                            isRevealed = expandedConversationId == conversation.id && !showDeleteConfirmation && !showAcceptConfirmation,
                                                            onExpanded = {
                                                                expandedConversationId = conversation.id
                                                            },
                                                            onCollapsed = {
                                                                if (expandedConversationId == conversation.id) {
                                                                    expandedConversationId = null
                                                                }
                                                            },
                                                            actions = {
                                                                ConversationActionButton(
                                                                    onClick = {
                                                                        conversationToAccept = conversation.id
                                                                        acceptMessage = "Do you want to accept the conversation with ${conversation.getHeader()}?"
                                                                        showAcceptConfirmation = true
                                                                        expandedConversationId = null
                                                                    },
                                                                    icon = Icons.Filled.Check,
                                                                    iconColor = secondaryColor,
                                                                    iconSize = 48.dp,
                                                                    modifier = Modifier.fillMaxHeight()
                                                                )

                                                            },
                                                        ) {
                                                            ChatListInfo(
                                                                isGroup = conversation.recipients.size > 1,
                                                                header = conversation.getHeader(),
                                                                subheader = conversation.getSummary(),
                                                                time = printFormattedDateInfo(conversation.lastMessage?.date),
                                                                readConversation = conversation.lastMessage?.seen == true,
                                                                onClick = { conversationClicked(conversation.id) },
                                                                primaryColor = primaryColor
                                                            )

                                                        }
                                                    }
                                                    item{
                                                        Spacer(modifier = modifier.fillMaxWidth().height(24.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .fillMaxWidth()
                                                    .height(24.dp)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                dgenBlack,
                                                                Color.Transparent
                                                            )
                                                        )
                                                    )
                                                )
                                                Spacer(modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(24.dp)
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color.Transparent,
                                                                dgenBlack
                                                            )
                                                        )
                                                    )
                                                )
                                            }

                                        }
                                        else{
                                            InfoScreen(
                                                modifier = Modifier.offset(y=16.dp),
                                                imageSize = 200.dp,
                                                primaryColor = primaryColor,
                                                description = "No requests"
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
        AnimatedVisibility(
            visible = showNewConversationSheet,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            NewConversationSheet(
                onDismiss = { showNewConversationSheet = false },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                onConversationCreated = {
                    showNewConversationSheet = false
                    //TODO: CHANGE TO NOT ONLY LOOK FOR PHONE NUMBER !!!URGENT!!!
                    onNewConversationCreated(it)
                }
            )
        }

        // Delete conversation confirmation overlay
        AnimatedVisibility(
            visible = showDeleteConfirmation,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            ConfirmationOverlay(
                description = deleteMessage,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                onCancel = {
                    showDeleteConfirmation = false
                    conversationToDelete?.let { deleteConversation(it) }
                },
                onConfirm = { showDeleteConfirmation = false }
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

