package org.ethereumhpone.chat

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ethereumhpone.chat.components.ChatBottomAppBar
import org.ethereumhpone.chat.components.ChatTopAppBar
import org.ethereumhpone.chat.components.ExpandedReactionPicker
import org.ethereumhpone.chat.components.FocusTransactionReferenceBubble
import org.ethereumhpone.chat.components.FocusTransactionRequestBubble
import org.ethereumhpone.chat.components.ReactionPicker
import org.ethereumhpone.chat.components.GroupDetailsSheet
import org.ethereumhpone.chat.components.TransactionAction
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.components.message.OverlayMessageItem
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.util.generateTestGroupMessages
import org.ethereumhpone.chat.util.generateTestMessages
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.domain.model.Attachment
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import org.ethereumphone.dgenlibrary.components.GoToBottomFab
import org.ethereumphone.dgenlibrary.components.NewMessagesDivider
import androidx.compose.ui.graphics.Color
import org.ethereumphone.dgenlibrary.components.TimeHeader
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumphone.dgenlibrary.components.SelectionOverlay
import org.ethereumphone.dgenlibrary.components.SelectionBarColumn
import org.ethereumphone.dgenlibrary.showDgenToast
// removed SharedMessageItem; using measured overlay clone
// removed unused: LocalDensity/width/height
import org.ethereumhpone.common.util.TextFieldFocusManager
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import org.ethereumphone.model.TransactionRequest
import org.ethereumphone.model.TransactionRequestStatus
import java.io.ByteArrayOutputStream
import kotlin.time.Duration.Companion.seconds
import com.messenger.terminalsdk.TerminalLEDController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import org.ethereumphone.dgenlibrary.components.SelectionBarColumn
import org.ethereumphone.dgenlibrary.theme.dgenRed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import android.util.Log
import android.os.Build
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.dgenlibrary.ConfirmationOverlay
import org.ethereumphone.dgenlibrary.components.TransactionStatus
import org.ethereumphone.dgenlibrary.components.TransactionStatusOverlay
import org.ethereumhpone.chat.components.AddMembersSheet


@Composable
fun ChatRoute(
    onBackClick: () -> Unit,
    onNavigateToSend: (String) -> Unit,
    onNavigateToRequest: (String) -> Unit,
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

    val selectedMessages by chatViewModel.selectedMessages.collectAsStateWithLifecycle()
    val selectMode by chatViewModel.selectMode.collectAsStateWithLifecycle()
    val myInboxId by chatViewModel.myInboxId.collectAsStateWithLifecycle()
    val canManageMembers by chatViewModel.canManageMembers.collectAsStateWithLifecycle()
    val isSuperAdmin by chatViewModel.isSuperAdmin.collectAsStateWithLifecycle()
    val transactionStatus by chatViewModel.transactionStatus.collectAsStateWithLifecycle()

    val converstation by chatViewModel.conversation.collectAsStateWithLifecycle()

    //TODO: Add Media Selection
    /*val mediaItems by mediaViewModel.mediaItems.collectAsState()
    val selectedIndex by mediaViewModel.selectedIndex.collectAsState()
    //val selectedAttachment by viewModel.selectedAttachment.collectAsState()
    val isInSelectionMode by mediaViewModel.isInSelectionMode.collectAsState()
    val selectedSet by mediaViewModel.selectedSet.collectAsState()*/

    val selectedIndex by mediaViewModel.selectedIndex.collectAsState()

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor
    val openGLColor = SystemColorManager.openGLColor



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
        selectedMessages = selectedMessages,
        selectMode = selectMode,
        onSendEthClicked = { },
        onSendMessageClicked = chatViewModel::sendMessage,
        onDeleteMessage = chatViewModel::deleteMessage,
        onFocusedMessageUpdate = {},
        onPrepareVideo = mediaViewModel::addVideoUri,
        onContactSelected = chatViewModel::parseContact,
        onToggleAttachment = chatViewModel::toggleAttachment,
        onToggleSelection = chatViewModel::toggleSelection,
        onOpenContact = chatViewModel::onOpenContact,
        selectedIndex = selectedIndex,
        nextMedia = mediaViewModel::next,
        prevMedia = mediaViewModel::prev,
        selectMedia = mediaViewModel::select,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        openGLColor = openGLColor,
        clearSelection = chatViewModel::clearSelection,
        onUpdateGroupName = chatViewModel::updateGroupName,
        onUpdateGroupDescription = chatViewModel::updateGroupDescription,
        onAddGroupMembers = chatViewModel::addGroupMembers,
        onRemoveGroupMember = chatViewModel::removeGroupMember,
        onLeaveGroup = { chatViewModel.leaveGroup(onBackClick) },
        onRemoveGroup = { chatViewModel.removeGroup(onBackClick) },
        isSuperAdmin = isSuperAdmin,
        onExecuteTransaction = { request, messageId ->
            // Route to the appropriate method based on whether this is paying a request
            if (messageId != null) {
                // Paying a transaction request - sends TransactionReference as proof
                chatViewModel.payTransactionRequest(request, messageId)
            } else {
                // Regular transaction execution
                chatViewModel.executeTransaction(request)
            }
        },
        onRejectTransaction = chatViewModel::rejectTransaction,
        onSendReaction = chatViewModel::sendReaction,
        myInboxId = myInboxId,
        canManageMembers = canManageMembers,
        onSendTransactionRequest = chatViewModel::sendTransactionRequest,
        onNavigateToSend = onNavigateToSend,
        onNavigateToRequest = onNavigateToRequest,
        transactionStatus = transactionStatus,
        onClearTransactionStatus = chatViewModel::clearTransactionStatus
    )

    // Mark messages as seen when leaving the chat screen
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.markSeenOnExit()
        }
    }
}

@OptIn(
    ExperimentalLayoutApi::class
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
    selectedMessages: List<Message> = emptyList(),
    selectMode: Boolean = false,
    onContactSelected: (ContactEntity) -> Unit,
    onToggleAttachment: (Attachment) -> Unit,
    onSendMessageClicked: (String) -> Unit = {},
    onDeleteMessage: (String) -> Unit,
    onFocusedMessageUpdate: (Message) -> Unit,
    onPrepareVideo: (Uri) -> Unit,
    onToggleSelection: (Message) -> Unit,
    selectedIndex: Int = -1,
    nextMedia: () -> Unit = {},
    prevMedia: () -> Unit = {},
    selectMedia: (Int) -> Unit = {},
    primaryColor: Color,
    openGLColor: Color,
    secondaryColor: Color,
    clearSelection: () -> Unit,
    onUpdateGroupName: (String) -> Unit = {},
    onUpdateGroupDescription: (String) -> Unit = {},
    onAddGroupMembers: (List<String>) -> Unit = {},
    onRemoveGroupMember: (String) -> Unit = {},
    onLeaveGroup: () -> Unit = {},
    onRemoveGroup: () -> Unit = {},
    isSuperAdmin: Boolean = false,
    onExecuteTransaction: (TransactionRequest, String?) -> Unit = { _, _ -> },
    onRejectTransaction: (Message) -> Unit = {},
    onSendReaction: (messageId: String, emoji: String) -> Unit = { _, _ -> },
    myInboxId: String = "",
    canManageMembers: Boolean = false,
    onSendTransactionRequest: (TransactionRequest) -> Unit = {},
    onNavigateToSend: (String) -> Unit = {},
    onNavigateToRequest: (String) -> Unit = {},
    transactionStatus: TransactionStatus? = null,
    onClearTransactionStatus: () -> Unit = {},
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    //handle focus
    val showBottomSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // FocusRequester for the message input text field (used for F2 voice input focus)
    val messageTextFieldFocusRequester = remember { FocusRequester() }
    
    // Register/unregister focus callback for OS text field focus requests (F2 long-press)
    DisposableEffect(Unit) {
        TextFieldFocusManager.registerFocusCallback {
            try {
                messageTextFieldFocusRequester.requestFocus()
                true
            } catch (e: Exception) {
                false
            }
        }
        onDispose {
            TextFieldFocusManager.unregisterFocusCallback()
        }
    }

    //gets offset of message composable
    val composablePositionState = remember { mutableStateOf(ComposablePosition()) }


    var showOverlay = remember { mutableStateOf(false) }
    val longPressedMessage = remember { mutableStateOf<Message?>(null) }
    var showExpandedReactionPicker = remember { mutableStateOf(false) }

    val scrollState = rememberLazyListState()

    //for selecting images from gallery
    val showPicker = remember { mutableStateOf(false) }
    
    // Action overlay state
    var showActionOverlay by remember { mutableStateOf(false) }
    var currentAction by remember { mutableStateOf(Actions.IDLE) }
    val shouldRotateAction = remember { mutableStateOf(false) }
    
    // Transaction send/request flows are handled via dedicated routes.

    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Group details sheet state
    var showGroupDetails by remember { mutableStateOf(false) }
    var showAddMembersSheet by remember { mutableStateOf(false) }

    BackHandler(showOverlay.value || showPicker.value || showActionOverlay || (WindowInsets.isImeVisible && !showBottomSheet) || selectMode || showGroupDetails || showAddMembersSheet) {
        when {
            showAddMembersSheet -> {
                showAddMembersSheet = false
            }
            showGroupDetails -> {
                showGroupDetails = false
            }
            showActionOverlay -> {
                showActionOverlay = false
                currentAction = Actions.IDLE
                shouldRotateAction.value = false
            }
            showOverlay.value -> {
                showOverlay.value = false
                longPressedMessage.value = null
            }
            showPicker.value -> {
                showPicker.value = false
            }
            selectMode -> {
                clearSelection()
            }
            else -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }
    }


    val chatConversion = when (converstation) {
        ConversationUiState.Loading -> null
        is ConversationUiState.Error -> null
        is ConversationUiState.Success -> converstation.conversation
    }
    val existingRecipientAddresses = remember(chatConversion?.recipients) {
        chatConversion?.recipients
            ?.map { it.address.trim().lowercase() }
            ?.toSet()
            ?: emptySet()
    }
    val eligibleContacts = remember(contactEntities, existingRecipientAddresses) {
        contactEntities.filter { contact ->
            val address = contact.ethAddress?.trim()
            !address.isNullOrBlank() && address.lowercase() !in existingRecipientAddresses
        }
    }

    // variables for ui
    var currentActions by remember { mutableStateOf(Actions.IDLE) }
    val visibleMap = remember { mutableStateMapOf<String, MutableState<Boolean>>() }
    val isFirstLoad = remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // variables for Chatbottombar
    var hasMultipleLines = remember { mutableStateOf(false) }
    val expand = remember { mutableStateOf(false) }

    // Soft-delete state for messages (UI-only)
    val softDeletedMessageIds = remember { mutableStateMapOf<String, Boolean>() }
    val showDeleteConfirmation = remember { mutableStateOf(false) }


    //for selecting images from gallery
    val selectedAttachments = remember { mutableStateListOf<Attachment>() }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            selectedAttachments.clear()

            data?.clipData?.let { clip ->
                // multiple images
                for (i in 0 until clip.itemCount) {
                    clip.getItemAt(i).uri?.let { uri ->
                        selectedAttachments.add(Attachment.Image(uri = uri))
                    }
                }
            }
            // single image
            data?.data?.let { uri ->
                selectedAttachments.add(Attachment.Image(uri = uri))
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
                selectedAttachments += Attachment.Image(uri = uri, date = System.currentTimeMillis())
            }
        }
        showPicker.value = true
    }


    // boolean for GoToBottomFab
    val showFab by remember {
        derivedStateOf {
            val info = scrollState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            // totalItemsCount includes headers/footers too; -1 to get max index
            lastVisible < (info.totalItemsCount - 1)
        }
    }




    // Removed CRTBackground overlay. Using plain dgenBlack background instead.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(dgenBlack)
    ) {
        Scaffold(
            topBar = {
                ChatTopAppBar(
                    chatConversion?.getHeader() ?: "",
                    recipientUiState = recipientUiState,
                    isGroup = chatConversion?.isGroup ?: false,
                    onTitleClicked = {
                        // Show group details if this is a group conversation
                        if (chatConversion?.isGroup == true) {
                            showGroupDetails = true
                        } else {
                            // Copy recipient's address to clipboard when title is clicked
                            // Use getOtherRecipientAddress() to get the contact's address, not the user's own
                            val address = chatConversion?.getOtherRecipientAddress()
                            if (!address.isNullOrBlank()) {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Recipient Address", address)
                                clipboardManager.setPrimaryClip(clip)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDgenToast(context, "Address copied")
                            } else {
                                // Fallback to previous behavior if no address available
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }
                    },
                    onBackClicked = navigateBackToConversations,
                    primaryColor = primaryColor
                )
            },
            bottomBar = {
                ChatBottomAppBar(
                    attachments = selectedAttachments,
                    onToggleAttachment = { att ->
                        if (att in selectedAttachments) selectedAttachments.remove(att)
                        else selectedAttachments.add(att)
                    },
                    onSendClick = { text ->
                        if (text.isNotBlank()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    println("Before send")
                                    onSendMessageClicked(text)
                                    println("After send")
                                    
                                    // Success feedback: haptic + LED
                                    withContext(Dispatchers.Main) {
                                        // Positive haptic feedback for successful send
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    // Flash success pattern when message sent
                                    TerminalLEDController.flashSuccess()
                                } catch (e: Exception) {
                                    // Error feedback: haptic + LED
                                    withContext(Dispatchers.Main) {
                                        // Error haptic feedback
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    // Flash error pattern if send fails
                                    TerminalLEDController.flashError()
                                }
                            }
                        }
                        selectedAttachments.clear()
                    },
                    hasMultipleLines = hasMultipleLines,
                    expand = expand,
                    openAction = {
                        expand.value = false
                        // Show action overlay with Transfer and Request options
                        showActionOverlay = true
                        currentAction = Actions.ACTION_MENU
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    primaryColor = primaryColor,
                    textFieldFocusRequester = messageTextFieldFocusRequester
                )
            },
            containerColor = Color.Transparent,
        )
        { paddingValues ->


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            // dismiss keyboard when tapping outside text field
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
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

                                // Variables for the new messages UI
                                var seenCount by remember { mutableIntStateOf(messages.size) }

                                MessageList(
                                    modifier = Modifier.fillMaxSize(),
                                    messages = messages,
                                    scrollState = scrollState,
                                    seenCount = seenCount,
                                    chatConversion = chatConversion,
                                    selectedMessages = selectedMessages,
                                    selectMode = remember { mutableStateOf(selectMode) },
                                    onToggleSelection = onToggleSelection,
                                    onMessageLongPress = { msg ->
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        longPressedMessage.value = msg
                                        showOverlay.value = true
                                    },
                                    composablePositionState = composablePositionState,
                                    player = videoPlayer,
                                    onPrepareVideo = onPrepareVideo,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    openGLColor = openGLColor,
                                    deletedMessageIds = softDeletedMessageIds,
                                    onUpdateSeenCount = { seenCount = it },
                                    onExecuteTransaction = onExecuteTransaction,
                                    onRejectTransaction = onRejectTransaction,
                                    myInboxId = myInboxId,
                                    onReactionClick = onSendReaction
                                )
                            }

                            MessageUiState.Loading -> {
                                Box(
                                    Modifier
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DgenLoadingMatrix(
                                        unactiveLEDColor = secondaryColor,
                                        activeLEDColor = primaryColor
                                    )
                                }
                            }
                }

                

            }


        }

        SelectionOverlay(
            visible = showOverlay.value,
            primaryColor = primaryColor,
            onCancelClick = {
                showOverlay.value = false
                longPressedMessage.value = null
                showExpandedReactionPicker.value = false
            },
            dismissOnBackgroundClick = true,
            content = {
                longPressedMessage.value?.let { selected ->
                    val density = LocalDensity.current
                    val msgPos = composablePositionState.value.offset
                    val msgWidth = composablePositionState.value.width
                    val msgHeight = composablePositionState.value.height
                    
                    // Determine message type for different positioning logic
                    val isTransactionMessage = selected.isTransactionReference() || selected.isTransactionRequest()
                    
                    // Screen dimensions
                    val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
                    
                    // For normal messages, we need both start and target X positions
                    val xAnim = remember { Animatable(0f) }
                    val yAnim = remember { Animatable(0f) }
                    
                    // Calculate positions based on message type
                    val (initialX, targetY) = with(density) {
                        when {
                            selected.isTransactionRequest() -> {
                                // Transaction request: position higher up (18% from top)
                                val txInitialX = msgPos.x.coerceAtLeast(0f)
                                Pair(txInitialX, screenHeight * 0.18f)
                            }
                            selected.isTransactionReference() -> {
                                // Transaction reference: position at 25% from top
                                val txInitialX = msgPos.x.coerceAtLeast(0f)
                                Pair(txInitialX, screenHeight * 0.25f)
                            }
                            else -> {
                                // Normal messages: X stays fixed at original position, only Y animates
                                val normalInitialX = msgPos.x.coerceAtLeast(0f)
                                // Target Y: ~35% from top
                                Pair(normalInitialX, screenHeight * 0.35f)
                            }
                        }
                    }
                    
                    LaunchedEffect(selected.id, showOverlay.value) {
                        if (showOverlay.value) {
                            val yCorrectionPx = with(density) { 8.dp.toPx() }
                            val startY = (msgPos.y - yCorrectionPx).coerceAtLeast(0f)
                            
                            // Snap to initial positions - X stays fixed, only Y animates
                            xAnim.snapTo(initialX)
                            yAnim.snapTo(startY)
                            
                            Log.d("ChatOverlay", "MessageType: ${if (isTransactionMessage) "Transaction" else "Normal"}")
                            Log.d("ChatOverlay", "Fixed X: $initialX, Start Y: $startY")
                            Log.d("ChatOverlay", "Target Y: $targetY")
                            Log.d("ChatOverlay", "Message width: $msgWidth")
                            
                            delay(500)
                            
                            // Only animate Y (vertical movement)
                            yAnim.animateTo(targetY, animationSpec = tween(durationMillis = 500))
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Transaction messages use a different layout structure
                        if (isTransactionMessage) {
                            // Transaction message overlay layout
                            // Width modifier: use original message width (same as chat bubbles)
                            // For user messages: fixed width; for incoming: flexible to fit content
                            val txWidthModifier = if (selected.isMe) {
                                Modifier.width(with(density) { msgWidth.toDp() })
                            } else {
                                Modifier.widthIn(
                                    min = with(density) { msgWidth.toDp() },
                                    max = 320.dp
                                )
                            }
                            
                            Column(
                                modifier = Modifier
                                    .offset { IntOffset(xAnim.value.roundToInt(), yAnim.value.roundToInt()) }
                                    .then(txWidthModifier),
                                // Same alignment logic as normal messages
                                horizontalAlignment = if (selected.isMe) Alignment.End else Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Expanded reaction picker - same logic as normal messages, with animation
                                AnimatedVisibility(
                                    visible = showExpandedReactionPicker.value,
                                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                                        animationSpec = tween(200),
                                        initialOffsetY = { it / 2 }
                                    ),
                                    exit = fadeOut(animationSpec = tween(150))
                                ) {
                                    ExpandedReactionPicker(
                                        isVisible = true,
                                        onReactionSelected = { emoji ->
                                            Log.d("REACTION_DEBUG", "ChatScreen: ExpandedReactionPicker (TX) selected emoji=$emoji")
                                            onSendReaction(selected.id, emoji)
                                            showExpandedReactionPicker.value = false
                                            showOverlay.value = false
                                            longPressedMessage.value = null
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDismiss = {
                                            showExpandedReactionPicker.value = false
                                        },
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor,
                                        modifier = Modifier
                                            .wrapContentWidth(
                                                unbounded = true,
                                                align = if (selected.isMe) Alignment.End else Alignment.Start
                                            )
                                            .offset(y = (-16).dp)
                                    )
                                }

                                // Quick reaction picker - same logic as normal messages
                                ReactionPicker(
                                    isVisible = !showExpandedReactionPicker.value,
                                    isUserMe = selected.isMe,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    onReactionSelected = { emoji ->
                                        Log.d("REACTION_DEBUG", "ChatScreen: ReactionPicker (TX) selected emoji=$emoji")
                                        onSendReaction(selected.id, emoji)
                                        showOverlay.value = false
                                        longPressedMessage.value = null
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onExpandPicker = {
                                        showExpandedReactionPicker.value = true
                                    },
                                    modifier = Modifier
                                        .wrapContentWidth(
                                            unbounded = true,
                                            align = if (selected.isMe) Alignment.End else Alignment.Start
                                        )
                                )

                                // Display transaction bubble based on type
                                when {
                                    selected.isTransactionReference() && selected.transactionReference != null -> {
                                        FocusTransactionReferenceBubble(
                                            message = selected,
                                            transactionReference = selected.transactionReference!!,
                                            isUserMe = selected.isMe,
                                            primaryColor = primaryColor,
                                            secondaryColor = secondaryColor,
                                            modifier = Modifier
                                                .onGloballyPositioned { coordinates ->
                                                    val pos = coordinates.positionInRoot()
                                                    Log.d("ChatOverlay", "FocusTransactionReferenceBubble XY: ${pos.x}, ${pos.y}")
                                                }
                                        )
                                    }
                                    selected.isTransactionRequest() && selected.transactionRequest != null -> {
                                        FocusTransactionRequestBubble(
                                            message = selected,
                                            transactionRequest = selected.transactionRequest!!,
                                            isUserMe = selected.isMe,
                                            primaryColor = primaryColor,
                                            secondaryColor = secondaryColor,
                                            onExecuteTransaction = { txRequest, messageId ->
                                                onExecuteTransaction(txRequest, messageId)
                                                showOverlay.value = false
                                                longPressedMessage.value = null
                                            },
                                            modifier = Modifier
                                                .onGloballyPositioned { coordinates ->
                                                    val pos = coordinates.positionInRoot()
                                                    Log.d("ChatOverlay", "FocusTransactionRequestBubble XY: ${pos.x}, ${pos.y}")
                                                }
                                        )
                                    }
                                }
                            }
                        }
                        else {
                            // Normal message overlay layout
                            // For user messages (isMe): align to end, picker extends LEFT
                            // For incoming messages (!isMe): align to start, picker extends RIGHT
                            
                            // Width modifier differs based on message sender:
                            // - User messages: fixed width (original behavior, positions correctly)
                            // - Incoming messages: flexible widthIn to prevent AuthorNameTimestamp overflow
                            val widthModifier = if (selected.isMe) {
                                Modifier.width(with(density) { msgWidth.toDp() })
                            } else {
                                Modifier.widthIn(
                                    min = with(density) { msgWidth.toDp() },
                                    max = 300.dp
                                )
                            }
                            
                            Column(
                                modifier = Modifier
                                    .offset { IntOffset(xAnim.value.roundToInt(), yAnim.value.roundToInt()) }
                                    .then(widthModifier),
                                horizontalAlignment = if (selected.isMe) Alignment.End else Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Expanded reaction picker - full width, unbounded from parent, with animation
                                // Position differs based on message sender
                                AnimatedVisibility(
                                    visible = showExpandedReactionPicker.value,
                                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                                        animationSpec = tween(200),
                                        initialOffsetY = { it / 2 }
                                    ),
                                    exit = fadeOut(animationSpec = tween(150))
                                ) {
                                    ExpandedReactionPicker(
                                        isVisible = true,
                                        onReactionSelected = { emoji ->
                                            Log.d("REACTION_DEBUG", "ChatScreen: ExpandedReactionPicker selected emoji=$emoji")
                                            onSendReaction(selected.id, emoji)
                                            showExpandedReactionPicker.value = false
                                            showOverlay.value = false
                                            longPressedMessage.value = null
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDismiss = {
                                            Log.d("REACTION_DEBUG", "ChatScreen: ExpandedReactionPicker dismissed")
                                            showExpandedReactionPicker.value = false
                                        },
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor,
                                        modifier = Modifier
                                            .wrapContentWidth(
                                                unbounded = true,
                                                align = if (selected.isMe) Alignment.End else Alignment.Start
                                            )
                                            .offset(y = (-16).dp)
                                    )
                                }
                                
                                // Quick reaction picker - full width, unbounded from parent
                                // For user messages: anchored to end, extends LEFT
                                // For incoming messages: anchored to start, extends RIGHT
                                ReactionPicker(
                                    isVisible = !showExpandedReactionPicker.value,
                                    isUserMe = selected.isMe,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    onReactionSelected = { emoji ->
                                        Log.d("REACTION_DEBUG", "ChatScreen: ReactionPicker selected emoji=$emoji")
                                        onSendReaction(selected.id, emoji)
                                        showOverlay.value = false
                                        longPressedMessage.value = null
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onExpandPicker = {
                                        Log.d("REACTION_DEBUG", "ChatScreen: onExpandPicker - setting showExpandedReactionPicker=true")
                                        showExpandedReactionPicker.value = true
                                    },
                                    modifier = Modifier
                                        .wrapContentWidth(
                                            unbounded = true,
                                            align = if (selected.isMe) Alignment.End else Alignment.Start
                                        )
                                )
                                
                                // Normal message bubble - constrained to max 300.dp
                                OverlayMessageItem(
                                    msg = selected,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    modifier = Modifier
                                        .widthIn(max = 300.dp)
                                        .onGloballyPositioned { coordinates ->
                                            val pos = coordinates.positionInRoot()
                                            Log.d("ChatOverlay", "OverlayMessageItem XY: ${pos.x}, ${pos.y}")
                                        }
                                )
                            }
                        }
                    }
                }
            },
            actions = {
//                if (longPressedMessage.value?.isMe == true) {
//                    SelectionBarColumn(
//                        imageVector = Icons.Outlined.Delete,
//                        title = "Delete",
//                        primaryColor = dgenRed,
//                        onClick = {
//                            // Hide selection actions and ask for confirmation
//                            showOverlay.value = false
//                            showDeleteConfirmation.value = true
//                        }
//                    )
//                }
                
                // Show SEND button for transaction request messages (not transaction reference)
                // Only show if not already paid
                longPressedMessage.value?.let { msg ->
                    if (msg.isTransactionRequest() && msg.transactionRequest != null && 
                        msg.transactionStatus != TransactionRequestStatus.SUCCESS) {
                        SelectionBarColumn(
                            icon = R.drawable.send_arrow,
                            title = "Send",
                            primaryColor = primaryColor,
                            onClick = {
                                msg.transactionRequest?.let { txRequest ->
                                    onExecuteTransaction(txRequest, msg.id)
                                }
                                showOverlay.value = false
                                longPressedMessage.value = null
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }
                }



                SelectionBarColumn(
                    imageVector = Icons.Outlined.ContentCopy,
                    title = "Copy",
                    primaryColor = primaryColor,
                    onClick = {
                        longPressedMessage.value?.let { msg ->
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Message", msg.body)
                            clipboardManager.setPrimaryClip(clip)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDgenToast(context, "Copied")
                        }
                        showOverlay.value = false
                        longPressedMessage.value = null
                    }
                )
            }
        )

        // Confirmation overlay for deleting a message (soft-delete)
        ConfirmationOverlay(
            visible = showDeleteConfirmation.value,
            description = "Delete this message?",
            extraDescription = "This will hide the message and show 'Deleted Message'.",
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            onCancel = {
                longPressedMessage.value?.id?.let { msgId ->
                    softDeletedMessageIds[msgId] = true
                }
                showDgenToast(context, "Message deleted")
                showDeleteConfirmation.value = false
                longPressedMessage.value = null
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onConfirm = {
                showDeleteConfirmation.value = false
            }
        )

        // Group Details Sheet
        if (showGroupDetails && chatConversion != null) {
            GroupDetailsSheet(
                conversation = chatConversion,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                canManageMembers = canManageMembers,
                isSuperAdmin = isSuperAdmin,
                onBackClick = { showGroupDetails = false },
                onUpdateGroupName = onUpdateGroupName,
                onUpdateGroupDescription = onUpdateGroupDescription,
                onAddMembers = {
                    showGroupDetails = false
                    showAddMembersSheet = true
                },
                onRemoveMember = onRemoveGroupMember,
                onLeaveGroup = {
                    onLeaveGroup()
                    showGroupDetails = false
                },
                onRemoveGroup = {
                    onRemoveGroup()
                    showGroupDetails = false
                }
            )
        }

        // Add Members Sheet (opened from Group Details)
        if (showAddMembersSheet) {
            AddMembersSheet(
                eligibleContacts = eligibleContacts,
                onAddMembers = { selectedContacts ->
                    val addressesToAdd = selectedContacts
                        .mapNotNull { it.ethAddress?.trim() }
                        .filter { it.isNotBlank() }
                        .filter { it.lowercase() !in existingRecipientAddresses }
                        .distinctBy { it.lowercase() }

                    if (addressesToAdd.isNotEmpty()) {
                        onAddGroupMembers(addressesToAdd)
                    }

                    showAddMembersSheet = false
                },
                onBackClick = {
                    showAddMembersSheet = false
                },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
            )
        }
    }
    
    // Action Overlay (Transfer / Request menu)
    ChatOverlays(
        showPicker = showActionOverlay,
        currentActions = currentAction,
        onActionSelected = { action, showOverlayNext ->
            when (action) {
                Actions.IDLE -> {
                    showActionOverlay = false
                    currentAction = Actions.IDLE
                    shouldRotateAction.value = false
                }
                Actions.SEND -> {
                    val conversationId = chatConversion?.id
                    showActionOverlay = false
                    currentAction = Actions.IDLE
                    shouldRotateAction.value = false
                    if (!conversationId.isNullOrBlank()) {
                        onNavigateToSend(conversationId)
                    }
                }
                Actions.TRANSFER_REQUEST -> {
                    val conversationId = chatConversion?.id
                    showActionOverlay = false
                    currentAction = Actions.IDLE
                    shouldRotateAction.value = false
                    if (!conversationId.isNullOrBlank()) {
                        onNavigateToRequest(conversationId)
                    }
                }
                else -> {
                    currentAction = action
                }
            }
        },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
    )
    
    // Transaction Status Overlay - shows pending/success/failure state for transactions executed from chat
    // GIF-enabled image loader for the overlay animation
    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()
    }
    
    TransactionStatusOverlay(
        status = transactionStatus,
        gifLoader = gifEnabledLoader,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        onDismiss = {
            onClearTransactionStatus()
        }
    )

}




private fun getImageUri(context: Context, bitmap: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
    return Uri.parse(path)
}

enum class Actions {
    IDLE, SEND, TRANSFER_REQUEST, ACTION_MENU
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
}



