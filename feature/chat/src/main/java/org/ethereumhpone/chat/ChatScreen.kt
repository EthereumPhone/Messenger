package org.ethereumhpone.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.example.dgenlibrary.ui.theme.dgenBlack
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ethereumhpone.chat.components.ActionOverlayScreen
import org.ethereumhpone.chat.components.ChatBottomAppBar
import org.ethereumhpone.chat.components.ChatTopAppBar
import org.ethereumhpone.chat.components.OverlaySendScreen
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.components.message.MessageItem
import org.ethereumhpone.chat.screen.ImageSelectionScreen
import org.ethereumhpone.chat.util.generateTestGroupMessages
import org.ethereumhpone.chat.util.generateTestMessages
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.domain.model.Attachment
import org.ethereumphone.dgenlibrary.components.TimeHeader
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import java.io.ByteArrayOutputStream
import kotlin.time.Duration.Companion.seconds


@Composable
fun ChatRoute(
    onBackClick: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val videoPlayer = mediaViewModel.exoPlayer
    val messagesUiState by chatViewModel.messagesState.collectAsStateWithLifecycle()
    val contacts by chatViewModel.contacts.collectAsStateWithLifecycle()
    val media by chatViewModel.media.collectAsStateWithLifecycle()
    val recipients by chatViewModel.recipients.collectAsStateWithLifecycle()
    //val tokenBalance by chatViewModel.ethBalance.collectAsStateWithLifecycle()
    val chainName by chatViewModel.chainName.collectAsStateWithLifecycle()
    val attachments by chatViewModel.attachments.collectAsStateWithLifecycle()
    // val ensAddress by chatViewModel.ensAddress.collectAsStateWithLifecycle()

    val selectedMessaged by chatViewModel.selectedMessages.collectAsStateWithLifecycle()

    val converstation by chatViewModel.conversation.collectAsStateWithLifecycle()

    //TODO: Add Media Selection
    /*val mediaItems by mediaViewModel.mediaItems.collectAsState()
    val selectedIndex by mediaViewModel.selectedIndex.collectAsState()
    //val selectedAttachment by viewModel.selectedAttachment.collectAsState()
    val isInSelectionMode by mediaViewModel.isInSelectionMode.collectAsState()
    val selectedSet by mediaViewModel.selectedSet.collectAsState()*/

    val selectedIndex by mediaViewModel.selectedIndex.collectAsState()


    ChatScreen(
        messageUiState = messagesUiState,
        recipientUiState = recipients,
        converstation = converstation,
        contactEntities = contacts,
        media = media,
        attachments = attachments,
        navigateBackToConversations = onBackClick,
        tokenBalance = 2.456, //tokenBalance,
        chainName = chainName,
        videoPlayer = videoPlayer,
        //selectedMessaged = selectedMessaged,
        onSendEthClicked = { },
        onSendMessageClicked = chatViewModel::sendMessage,
        onDeleteMessage = chatViewModel::deleteMessage,
        onFocusedMessageUpdate = {},
        onPrepareVideo = mediaViewModel::addVideoUri,
        onContactSelected = chatViewModel::parseContact,
        onToggleAttachment = chatViewModel::toggleAttachment,
        onRemoveSelectedMessage = chatViewModel::removeSelectedMessage,
        onOpenContact = chatViewModel::onOpenContact,
        onAddSelectedMessage = chatViewModel::addSelectedMessage,
        selectedIndex = selectedIndex,
        nextMedia = mediaViewModel::next,
        prevMedia = mediaViewModel::prev,
        selectMedia = mediaViewModel::select ,

    )
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    messageUiState: MessageUiState,
    recipientUiState: RecipientUiState,
    converstation: ConversationUiState,
    contactEntities: List<ContactEntity> = emptyList(),
    media: List<Uri> = emptyList(),
    attachments: Set<Attachment> = emptySet(),
    navigateBackToConversations: () -> Unit,
    onSendEthClicked: (amount: Double) -> Unit,
    tokenBalance: Double = 0.0,
    chainName: String = "?",
    videoPlayer: Player? = null,
    onOpenContact: () -> Unit,
    selectedMessaged: List<Message?> = emptyList(),
    onContactSelected: (ContactEntity) -> Unit,
    onToggleAttachment: (Attachment) -> Unit,
    onSendMessageClicked: (String) -> Unit = {},
    onDeleteMessage: (String) -> Unit,
    onFocusedMessageUpdate: (Message) -> Unit,
    onPrepareVideo: (Uri) -> Unit,
    onRemoveSelectedMessage: (Message) -> Unit,
    onAddSelectedMessage: (Message) -> Unit,
    selectedIndex: Int = -1,
    nextMedia: () -> Unit = {},
    prevMedia: () -> Unit = {},
    selectMedia: (Int) -> Unit = {},
) {
    val context = LocalContext.current

    //handle focus
    val showBottomSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    //gets offset of message composable
    val composablePositionState = remember { mutableStateOf(ComposablePosition()) }

    val selectMode = remember { mutableStateOf(false) }
    val selectedMessagesMap = remember { mutableMapOf<Message, Boolean>() }

    var showOverlay = remember { mutableStateOf(false) }

    var shouldRotate = remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler(showOverlay.value || (WindowInsets.isImeVisible && !showBottomSheet)) {
        if (showOverlay.value) {
            showOverlay.value = false
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    val chatConversion = when (converstation) {
        ConversationUiState.Loading -> null
        is ConversationUiState.Success -> converstation.conversation
    }

    // variables for ui
    var currentActions by remember { mutableStateOf(Actions.IDLE) }
    val visibleMap = remember { mutableStateMapOf<String, MutableState<Boolean>>() }
    val isFirstLoad = remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // variables for Chatbottombar
    var hasMultipleLines = remember { mutableStateOf(false) }
    val expand = remember { mutableStateOf(false) }



    //for selecting images from gallery
    var showPicker by remember { mutableStateOf(false) }
    val attachments = remember { mutableStateListOf<Attachment>() }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            attachments.clear()

            data?.clipData?.let { clip ->
                // multiple images
                for (i in 0 until clip.itemCount) {
                    clip.getItemAt(i).uri?.let { uri ->
                        attachments.add(Attachment.Image(uri = uri))
                    }
                }
            }
            // single image
            data?.data?.let { uri ->
                attachments.add(Attachment.Image(uri = uri))
            }
        }
    }

    // for camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            // convert to Uri and wrap
            getImageUri(context, it)?.let { uri ->
                attachments += Attachment.Image(uri = uri, date = System.currentTimeMillis())
            }
        }
        showPicker = true
    }


    var showContact by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Scaffold(
            topBar = {
                ChatTopAppBar(
                    chatConversion?.getHeader() ?: "",
                    recipientUiState = recipientUiState,
                    onTitleClicked = {
                        currentActions = Actions.CONTACT
                        showPicker = true
                    },
                    onBackClicked = navigateBackToConversations
                )
            },
            bottomBar = {
                ChatBottomAppBar(
                    attachments = attachments,
                    onToggleAttachment = { att ->
                        if (att in attachments) attachments.remove(att)
                        else attachments.add(att)
                    },
                    onSendClick = { text ->
                        if (text.isNotBlank()) {
                            onSendMessageClicked(text)
                        }
                        attachments.clear()
                    },
                    hasMultipleLines = hasMultipleLines,
                    expand = expand,
                    openAction = {
                        expand.value = false
                        // Show overlay when this action is triggered
                        showOverlay.value = true
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            },
            containerColor = dgenBlack,
        ) { paddingValues ->


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                //TODO: refactor ALL of this

                when (messageUiState) {
                    is MessageUiState.Success -> {
                        val messages = messageUiState.messageEntities

                        var initialScrollDone by remember { mutableStateOf(false) }

                        // handles scroll logic.
                        LaunchedEffect(messages) {
                            if (messages.isNotEmpty()) {
                                val lastVisibleItemIndex = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                val totalItemsCount = scrollState.layoutInfo.totalItemsCount

                                if (!initialScrollDone) {
                                    scrollState.animateScrollToItem(totalItemsCount)
                                    initialScrollDone = true
                                }

                                if (lastVisibleItemIndex != null && lastVisibleItemIndex >= totalItemsCount - 2) {
                                    scrollState.animateScrollToItem(totalItemsCount)
                                }
                            }
                        }

                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(
                                items = messages,
                                key = { _, message -> message.id }
                            ) { index, message ->

                                val prevMessage = messages.getOrNull(index - 1)
                                val prevAuthor = prevMessage?.recipient?.id
                                val isFirstMessageByAuthor = prevAuthor != message.recipient.id

                                val prevDate = prevMessage?.date?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                                val currentDate = message.date.toLocalDateTime(TimeZone.currentSystemDefault()).date

                                //Log.d("List index", index.toString())
                                Column {
                                    if (prevDate != currentDate) {
                                        TimeHeader(message.date)
                                    }

                                    MessageItem(
                                        onAuthorClick = { },
                                        msg = message,
                                        composablePositionState = composablePositionState,
                                        player = videoPlayer,
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
                                        isGroup = chatConversion?.isGroup == true,
                                        isVisible = true
                                    )
                                }
                            }
                        }
                    }

                    MessageUiState.Loading -> {
                        Box(
                            Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

            }


        }

        // Overlay with AnimatedVisibility for fade effect
        ActionOverlayScreen(
            showOverlay = showOverlay,
            shouldRotate = shouldRotate,
            openImage = {
                //opens gallery for selecting images
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                launcher.launch(intent)
                //showPicker = true
                showOverlay.value = false
            },
            openVideo = {
                //opens gallery for selecting images
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                launcher.launch(intent)

                //showPicker = true
                showOverlay.value = false
            },
            openCamera = {
                //open camera
                //val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(null)
                showOverlay.value = false
            },
            openSend = {
                //open camera
                //val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                //cameraLauncher.launch(null)
                //showOverlay.value = false
                currentActions = Actions.SEND //set sending screen
                showPicker = true
            }
        )

        //TODO: Fix Action Picker
        AnimatedVisibility(
            visible = showPicker,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(dgenBlack)
            ) {
                when (currentActions) {
                    Actions.IDLE -> {}
                    Actions.SEND -> {
                        //if (chatConversion != null) {

                        OverlaySendScreen(
                            onBackClick = {
                                currentActions = Actions.IDLE
                                showPicker = false
                                showOverlay.value = false
                            },
                            onDone = {
                                //TODO: Implement Sending
                            },
                        )

                        //}
                    }
                    Actions.PHOTO -> {
                        //TODO: Add ImageSelection
                        /*ImageSelectionScreen(
                            attachments = attachments,
                            onToggleAttachment = { attachment ->
                                if (attachment in attachments) attachments.remove(attachment)
                                else attachments.add(attachment)
                            },
                            onBack = { showPicker = false },
                            onSelectedItems = {  },
                        )*/
                    }
                    Actions.VIDEO -> {
                        //TODO: add Videopicker
                    }
                    Actions.CONTACT -> {
                        chatConversion?.isGroup?.let {
                            OverlayContactScreen(
                                onBackClick = {
                                    currentActions = Actions.IDLE
                                    showPicker = false
                                    showOverlay.value = false
                                },
                                title = chatConversion.title.toString(),
                                isGroup = it,
                                media = media,
                                transactions = emptyList(), //TODO: Add Transaction
                                recipientUiState = recipientUiState,
                                deleteGroup= {},
                                leaveGroup = {},
                                deleteContact= {},
                                deleteMember= {},
                                selectedIndex = selectedIndex,
                                prev = prevMedia,
                                next = nextMedia,
                                select = selectMedia
                            )
                        }
                    }
                }
            }
        }


    }

}

private fun getImageUri(context: Context, bitmap: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
    return Uri.parse(path)
}

enum class Actions {
    IDLE, SEND, PHOTO, VIDEO, CONTACT
}


fun chainIdToReadableName(chainId: Int): String = when (chainId) {
    1 -> "Ethereum Mainnet"
    11155111 -> "Ethereum Sepolia"
    10 -> "Optimism Mainnet"
    42161 -> "Arbitrum Mainnet"
    137 -> "Polygon Mainnet"
    8453 -> "Base Mainnet"
    5 -> "Ethereum Goerli"
    else -> ""
}

fun isValidTransactionMessage(message: String): Boolean {
    val regex = """^Sent \d+(\.\d+)? ETH: https://[a-zA-Z0-9.-]+/tx/0x[a-fA-F0-9]{64}$""".toRegex()
    return regex.matches(message)
}

data class TransactionDetails(val amount: String, val url: String, val chainId: Int)

fun extractTransactionDetails(message: String): TransactionDetails? {
    val regex =
        """^Sent (\d+(\.\d+)?) ETH: (https://[a-zA-Z0-9.-]+/tx/0x[a-fA-F0-9]{64})$""".toRegex()
    val matchResult = regex.find(message) ?: return null

    val (amount, _, url) = matchResult.destructured

    val chainId = when (url.split("/")[2]) {
        "etherscan.io" -> 1
        "sepolia.etherscan.io" -> 11155111
        "optimistic.etherscan.io" -> 10
        "arbiscan.io" -> 42161
        "polygonscan.com" -> 137
        "basescan.org" -> 8453
        "goerli.etherscan.io" -> 5
        else -> -1 // Or any other appropriate value for unknown chain IDs
    }

    return if (chainId != -1) TransactionDetails(amount, url, chainId) else null
}


@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
private fun PreviewChatScreen() {


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
    ChatScreen(
        messageUiState = messageUiState,
        recipientUiState = recipientUiState,
        navigateBackToConversations = { },
        tokenBalance = 2.456,
        onSendEthClicked = { it -> },
        converstation = ConversationUiState.Success(
            Conversation(
                id = "2",
                title = null,
                recipients = listOf(
                    Recipient("r2", "0x456", null, Contact("lk2", "Bob", null, "0x456")),
                ),
                draft = null,
                lastMessage = Message(
                    id = "m2",
                    threadId = "2",
                    recipient = Recipient(
                        "r2",
                        "0x456",
                        null,
                        Contact("lk2", "Bob", null, "0x456")
                    ),
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
            )
        ),
        modifier = TODO(),
        contactEntities = TODO(),
        media = TODO(),
        attachments = TODO(),
        chainName = TODO(),
        videoPlayer = TODO(),
        onOpenContact = TODO(),
        selectedMessaged = TODO(),
        onContactSelected = TODO(),
        onToggleAttachment = TODO(),
        onSendMessageClicked = TODO(),
        onDeleteMessage = TODO(),
        onFocusedMessageUpdate = TODO(),
        onPrepareVideo = TODO(),
        onRemoveSelectedMessage = TODO(),
        onAddSelectedMessage = TODO()
    )
    /*
    ChatScreen(
        messagesUiState = messageUiState,
        recipients = listOf(
            Recipient(
                id = "userB",
                address = "0xDeF456HodlGuyWallet",
                ens = "hodl.eth",
                contact = Contact("lk2", "Bob", null, "0x456")
            )
        ),
        navigateBackToConversations={},
        onPhoneClicked = {},
        onSendEthClicked = {},
        onOpenContact = {},
        onContactSelected = {},
        onToggleAttachment = {},
        onSendMessageClicked = {},
        onDeleteMessage = {},
        onFocusedMessageUpdate = {},
        onPrepareVideo = {},
        onRemoveSelectedMessage = {},
        onAddSelectedMessage = {},
        videoPlayer = null
    )
     */


}


@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
private fun PreviewGroupChatScreen() {


    val now = Instant.parse("2025-04-17T12:00:00Z")
    val messageUiState = MessageUiState.Success(generateTestGroupMessages())

    val recipientMe = Recipient(
        id = "userA",
        address = "0xAbC123CryptoBroWallet",
        ens = "bro.eth",
        contact = Contact("lk1", "Timothy", null, "0x423")
    )

    val recipientUiState = RecipientUiState.Success(
        listOf(
            Recipient(
                id = "userB",
                address = "0xDeF456HodlGuyWallet",
                ens = "hodl.eth",
                contact = Contact("lk2", "Bob", null, "0x456")
            ),
            Recipient(
                id = "userC",
                address = "0xDeF456JoeGuy",
                ens = "Joe.eth",
                contact = Contact("lk2", "Joe", null, "0x474")
            )
        )
    )
    val convo = ConversationUiState.Success(
        Conversation(
            id = "3",
            title = "🏀 Game Plan",
            recipients = listOf(
                recipientMe,
                Recipient(
                    id = "userB",
                    address = "0xDeF456HodlGuyWallet",
                    ens = "hodl.eth",
                    contact = Contact("lk2", "Bob", null, "0x456")
                ),
                Recipient(
                    id = "userC",
                    address = "0xDeF456JoeGuy",
                    ens = "Joe.eth",
                    contact = Contact("lk2", "Joe", null, "0x474")
                )
            ),
            draft = "Need to reply...",
            lastMessage = Message(
                "17",
                "thread3x3",
                recipientMe,
                now - (51 * 60).seconds,
                now - (51 * 60).seconds,
                true,
                DeliveryStatus.PUBLISHED,
                null,
                false,
                emptyList(),
                emptyList(),
                "Got it! And I’ll post some stories tagging Freedom Factory later."
            ),
            pinned = true,
            clientInbox = "inbox3",
            isGroup = true
        )
    )



    ChatScreen(
        messageUiState = messageUiState,
        recipientUiState = recipientUiState,
        navigateBackToConversations = { },
        tokenBalance = 2.456,
        onSendEthClicked = { it -> },
        converstation = convo,
        modifier = TODO(),
        contactEntities = TODO(),
        media = TODO(),
        attachments = TODO(),
        chainName = TODO(),
        videoPlayer = TODO(),
        onOpenContact = TODO(),
        selectedMessaged = TODO(),
        onContactSelected = TODO(),
        onToggleAttachment = TODO(),
        onSendMessageClicked = TODO(),
        onDeleteMessage = TODO(),
        onFocusedMessageUpdate = TODO(),
        onPrepareVideo = TODO(),
        onRemoveSelectedMessage = TODO(),
        onAddSelectedMessage = TODO()
    )
    /*
    ChatScreen(
        messagesUiState = messageUiState,
        recipients = listOf(
            Recipient(
                id = "userB",
                address = "0xDeF456HodlGuyWallet",
                ens = "hodl.eth",
                contact = Contact("lk2", "Bob", null, "0x456")
            )
        ),
        navigateBackToConversations={},
        onPhoneClicked = {},
        onSendEthClicked = {},
        onOpenContact = {},
        onContactSelected = {},
        onToggleAttachment = {},
        onSendMessageClicked = {},
        onDeleteMessage = {},
        onFocusedMessageUpdate = {},
        onPrepareVideo = {},
        onRemoveSelectedMessage = {},
        onAddSelectedMessage = {},
        videoPlayer = null
    )
     */


}