package org.ethereumhpone.chat

import android.net.Uri
import android.provider.Telephony
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.TextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.AssetPickerSheet
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.chat.components.message.MessageItem
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shortcut
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.zIndex
import org.ethereumhpone.chat.components.WalletSelector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import org.ethereumhpone.chat.components.ChatBottomAppBar
import org.ethereumhpone.chat.components.ChatHeader
import org.ethereumhpone.chat.components.ChatTopAppBar
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.components.ContactSheet
import org.ethereumhpone.chat.components.DetailSelector
import org.ethereumhpone.chat.components.GallerySheet
import org.ethereumhpone.chat.components.MediaSheet
import org.ethereumhpone.chat.components.MembersSheet
import org.ethereumhpone.chat.components.SendButton
import org.ethereumhpone.chat.components.message.TxMessage
import org.ethereumhpone.chat.components.TXSheet
import org.ethereumhpone.chat.components.attachments.AttachmentRow
import org.ethereumhpone.chat.components.customBlur
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.Attachment


@Composable
fun ChatRoute(
    onBackClick: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    mediaViewModel: MediaViewModel = hiltViewModel()
){
    val videoPlayer = mediaViewModel.exoPlayer
    val messagesUiState by chatViewModel.messagesState.collectAsStateWithLifecycle()
    val contacts by chatViewModel.contacts.collectAsStateWithLifecycle()
    val media by chatViewModel.media.collectAsStateWithLifecycle()
    val recipients by chatViewModel.recipientState.collectAsStateWithLifecycle()
    val tokenBalance by chatViewModel.ethBalance.collectAsStateWithLifecycle()
    val chainName by chatViewModel.chainName.collectAsStateWithLifecycle()
    val attachments by chatViewModel.attachments.collectAsStateWithLifecycle()
   // val ensAddress by chatViewModel.ensAddress.collectAsStateWithLifecycle()

    val selectedMessaged by chatViewModel.selectedMessages.collectAsStateWithLifecycle()


    ChatScreen(
        messagesUiState = messagesUiState,
        recipients = recipients,
        contacts = contacts,
        media = media,
        attachments = attachments,
        navigateBackToConversations = onBackClick,
        tokenBalance = tokenBalance,
        chainName = chainName,
        videoPlayer = videoPlayer,
        selectedMessaged = selectedMessaged,
        onSendEthClicked = { },
        onSendMessageClicked = chatViewModel::sendMessage,
        onDeleteMessage = chatViewModel::deleteMessage,
        onFocusedMessageUpdate = {},
        onPhoneClicked = { },
        onPrepareVideo = mediaViewModel::addVideoUri,
        onContactSelected = chatViewModel::parseContact,
        onToggleAttachment = chatViewModel::toggleAttachment,
        onRemoveSelectedMessage = chatViewModel::removeSelectedMessage,
        onOpenContact = chatViewModel::onOpenContact,
        onAddSelectedMessage = chatViewModel::addSelectedMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    messagesUiState: MessagesUiState,
    recipients: List<Recipient>,
    contacts: List<Contact> = emptyList(),
    media: List<Uri> = emptyList(),
    attachments: Set<Attachment> = emptySet(),
    navigateBackToConversations: () -> Unit,
    onSendEthClicked: (amount: Double) -> Unit,
    tokenBalance: Double = 0.0,
    chainName: String = "?",
    videoPlayer: Player? = null,
    onOpenContact: () -> Unit,
    selectedMessaged: List<Message?> = emptyList(),
    onContactSelected: (Contact) -> Unit,
    onToggleAttachment: (Attachment) -> Unit,
    onSendMessageClicked: (String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onFocusedMessageUpdate: (Message) -> Unit,
    onPhoneClicked: () -> Unit,
    onPrepareVideo: (Uri) -> Unit,
    onRemoveSelectedMessage: (Message) -> Unit,
    onAddSelectedMessage: (Message) -> Unit,
) {


    //handle focus
    val showBottomSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // check if either keyboard or bottomSheet are open
    BackHandler(WindowInsets.isImeVisible && !showBottomSheet) {
        focusManager.clearFocus()

    }

    //gets offset of message composable
    val composablePositionState = remember { mutableStateOf(ComposablePosition()) }


    val selectMode = remember { mutableStateOf(false) }
    val selectedMessagesMap = remember { mutableMapOf<Message, Boolean>() }




    Scaffold (
        topBar = {
            ChatTopAppBar(
                "",
                recipients = recipients,
                onTitleClicked = {},
                onBackClicked = navigateBackToConversations
            )
        },
        bottomBar = {
                ChatBottomAppBar(
                    attachments,
                    onToggleAttachment = onToggleAttachment,
                    onSendClick = onSendMessageClicked
                )
        },
        containerColor = Color.Black,
        modifier = Modifier.imePadding()

    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
            ,
            reverseLayout = true,


        ) {
            when(messagesUiState) {
                is MessagesUiState.Success -> {
                    items(
                        items = messagesUiState.messages,
                        key = {message -> message.id}
                    ) { message ->

                        val listState = rememberLazyListState()


                        val prevAuthor = messagesUiState.messages.getOrNull(messagesUiState.messages.indexOf(message) - 1)?.address
                        val nextAuthor = messagesUiState.messages.getOrNull(messagesUiState.messages.indexOf(message) + 1)?.address
                        val isFirstMessageByAuthor = prevAuthor != message.address
                        val isLastMessageByAuthor = nextAuthor != message.address

                        MessageItem(
                            onAuthorClick = { },
                            msg = message,
                            isFirstMessageByAuthor = isFirstMessageByAuthor,
                            isLastMessageByAuthor = isLastMessageByAuthor,
                            composablePositionState = composablePositionState,
                            player = videoPlayer,
                            onPrepareVideo = { onPrepareVideo(it) },
                            onLongClick = { onFocusedMessageUpdate(message) },
                            name = recipients.first().getDisplayName(),
                            isSelected = selectedMessagesMap.contains(message),
                            selectMode = selectMode,
                            isXMTP = true,
                            onSelect = { selectedMessage ->
                                // invert boolean or add
                                selectedMessagesMap.compute(selectedMessage) { _, isChecked ->
                                    isChecked?.let { !it } ?: true
                                }
                            },
                            onDoubleClick = {selectMode.value = !selectMode.value}
                        )

                        LaunchedEffect(key1 = messagesUiState) { listState.animateScrollToItem(0) }


                    }

                }


                is MessagesUiState.Loading -> {

                }
            }
        }
    }
}

fun chainIdToReadableName(chainId: Int): String = when(chainId) {
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

fun  extractTransactionDetails(message: String): TransactionDetails? {
    val regex = """^Sent (\d+(\.\d+)?) ETH: (https://[a-zA-Z0-9.-]+/tx/0x[a-fA-F0-9]{64})$""".toRegex()
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
fun SelectorExpanded(
    onSelectorChange: (InputSelector) -> Unit,
    onShowSelectionbar: () -> Unit,
    recipients: List<Recipient>,
    onHideKeyboard: () -> Unit,
){
    Row (
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)

    ){
        IconButton(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(CircleShape)
                .size(42.dp)
            ,
            enabled = true,
            onClick = {
                onSelectorChange(InputSelector.CONTACT)
                onHideKeyboard()
                onShowSelectionbar()
            },
        ) {
            Box(
                contentAlignment = Alignment.Center
            ){
                Icon(imageVector = Icons.Outlined.Person, modifier= Modifier
                    .size(32.dp)
                    ,contentDescription = "Contact",tint = Color.White)
            }

        }
        recipients[0].contact?.ethAddress?.let {
            if (it == "") return@let
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(CircleShape)
                    .size(42.dp)
                ,
                enabled = true,
                onClick = {
                    onSelectorChange(InputSelector.WALLET)
                    onHideKeyboard()
                    onShowSelectionbar()
                },
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ){
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.wallet),
                        modifier= Modifier.size(32.dp),
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }

            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(CircleShape)
                .size(42.dp)
            ,
            enabled = true,
            onClick = {
                onSelectorChange(InputSelector.PICTURE)
                onHideKeyboard()
                onShowSelectionbar()
            },
        ) {
            Box(
                contentAlignment = Alignment.Center
            ){
                Icon(imageVector = Icons.Outlined.InsertPhoto, modifier= Modifier
                    .size(32.dp)
                    ,contentDescription = "Send",tint = Color.White)
            }

        }
    }
}

@Composable
@Preview
private fun PreviewChatScreen() {
    val messages = listOf(
        Message(
            boxId = Telephony.Mms.MESSAGE_BOX_INBOX,
            type = "sms",
            body = "Thanks, just got them :)",
            dateSent = 1729852610,
            date = 1732552610
        ),
        Message(
            boxId = 2,
            type = "sms",
            body = "Sent 0.01 ETH: https://etherscan.io/tx/0x4e3b4ef5e7bcce14cf2f8fa65d2d2ec4483aef7fa3e47324f3bc76d1e7d0f8cd",

        ),

        Message(
            type = "sms",
            body = " A wait, could you borrow me some eth before I go?",
            dateSent = 1729852610,
            date = 1729852610
        ),
        Message(
            type = "sms",
            body = "Will do, see you soon :)",
            dateSent = 1729459260,
            date = 1729459260
        ),
        Message(
            boxId = 2,
            type = "sms",
            body = "Ok, have a great flight :) Tell me when you land, so I can pick you up",
            dateSent = 1729241255,
            date = 1729248355

        ),
        Message(
            type = "sms",
            body = "Hey, my flight will at 2pm. Just wanted to let you know",
            dateSent = 1729247355,
            date = 1729248355
        )


    )

    val messageUiState = MessagesUiState.Success(messages)
    ChatScreen(
        messagesUiState = messageUiState,
        recipients = listOf(Recipient(address = "nceornea.eth")),
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
}