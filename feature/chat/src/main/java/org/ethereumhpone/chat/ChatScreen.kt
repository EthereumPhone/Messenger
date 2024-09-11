package org.ethereumhpone.chat

import android.net.Uri
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.zIndex
import org.ethereumhpone.chat.components.WalletSelector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import org.ethereumhpone.chat.components.ChatHeader
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
    navigateBackToConversations: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    mediaViewModel: MediaViewModel = hiltViewModel()
){
    val videoPlayer = mediaViewModel.exoPlayer
    val messagesUiState by chatViewModel.messagesState.collectAsStateWithLifecycle()
    val contacts by chatViewModel.contacts.collectAsStateWithLifecycle()
    val media by chatViewModel.media.collectAsStateWithLifecycle()
    val recipient by chatViewModel.recipientState.collectAsStateWithLifecycle()
    val tokenBalance by chatViewModel.ethBalance.collectAsStateWithLifecycle()
    val chainName by chatViewModel.chainName.collectAsStateWithLifecycle()
    val attachments by chatViewModel.attachments.collectAsStateWithLifecycle()
    val focusedMessage by chatViewModel.focusedMessage.collectAsStateWithLifecycle()
   // val ensAddress by chatViewModel.ensAddress.collectAsStateWithLifecycle()

    val selectedMessaged by chatViewModel.selectedMessages.collectAsStateWithLifecycle()


    ChatScreen(
        messagesUiState = messagesUiState,
        recipient = recipient,
        contacts = contacts,
        media = media,
        attachments = attachments,
        navigateBackToConversations = navigateBackToConversations,
        tokenBalance = tokenBalance,
        chainName = chainName,
        videoPlayer = videoPlayer,
        focusedMessage = focusedMessage,
        selectedMessaged = selectedMessaged,
        onSendEthClicked = chatViewModel::sendEth,
        onSendMessageClicked = chatViewModel::sendMessage,
        onDeleteMessage = chatViewModel::deleteMessage,
        onFocusedMessageUpdate = chatViewModel::updatefocusedMessage,
        onPhoneClicked = chatViewModel::callPhone,
        onPrepareVideo = mediaViewModel::addVideoUri,
        onContactSelected = chatViewModel::parseContact,
        onToggleAttachment = chatViewModel::toggleAttachment,
        onRemoveSelectedMessage = chatViewModel::removeSelectedMessage,
        onAddSelectedMessage = chatViewModel::addSelectedMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    messagesUiState: MessagesUiState,
    recipient: Recipient?,
    contacts: List<Contact> = emptyList(),
    media: List<Uri> = emptyList(),
    attachments: Set<Attachment> = emptySet(),
    navigateBackToConversations: () -> Unit,
    onSendEthClicked: (amount: Double) -> Unit,
    tokenBalance: Double = 0.0,
    chainName: String = "?",
    videoPlayer: Player,
    selectedMessaged: List<Message?>,
    onContactSelected: (Contact) -> Unit,
    onToggleAttachment: (Attachment) -> Unit,
    onSendMessageClicked: (String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    focusedMessage: Message?,
    onFocusedMessageUpdate: (Message) -> Unit,
    onPhoneClicked: () -> Unit,
    onPrepareVideo: (Uri) -> Unit,
    onRemoveSelectedMessage: (Message) -> Unit,
    onAddSelectedMessage: (Message) -> Unit,
) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()


    //ModalSheets
    var showModalSheet by remember { mutableStateOf(false) }
    val modalAssetSheetState = rememberModalBottomSheetState(true)


    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    var currentModalSelector by rememberSaveable { mutableStateOf(DetailSelector.CONTACT) }


    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var showActionbar by remember { mutableStateOf(false) }
    var showSelectionbar by remember { mutableStateOf(false) }



    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }


    //Focused Message
    val focusMode = remember {
        mutableStateOf(false)
    }
    val composablePositionState = remember { mutableStateOf(ComposablePosition()) }//gets offset of message composable


    val profileview = remember {
        mutableStateOf(false)
    }

    var detailview by remember {
        mutableStateOf(false)
    }

    val selectMode = remember {
        mutableStateOf(false)
    }

    var selectAll = remember { mutableStateOf(false) }

    val selectedMessagesMap = remember { mutableMapOf<Message, Boolean>() }

    val controller = LocalSoftwareKeyboardController.current

    //TODO: differentiate between xmtp and sms
    var list = listOf("SMS","XMTP")
    var index = remember {
        mutableIntStateOf(0)
    }
    var sendbuttonbg = when(index.intValue){
        0 -> Color(0xFF8C7DF7)
        1 -> Color(0xFFF83C40)
        else -> {Color(0xFF8C7DF7)}
    }


    Scaffold (
        containerColor = Color.Black,
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ){ paddingValues ->

        Box(modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            AnimatedVisibility(
                modifier = modifier.zIndex(10f),
                visible = profileview.value,
                enter = slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth }, // Start from right
                        animationSpec = tween(300)
                    )
                ,
                exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth }, // Exit to left
                        animationSpec = tween(300)
                    )
            ) {
                ContactDetailView(
                    messagesUiState = messagesUiState,
                    name = recipient?.getDisplayName() ?: "",
                    image = recipient?.contact?.photoUri ?: "",
                    ens = listOf(""),
                    recipient = recipient,
                    profileview = profileview,
                    onMembersClick = {
                        currentModalSelector = DetailSelector.MEMBERS
                        showModalSheet = true
                    },onMediaClick = {
                        currentModalSelector = DetailSelector.MEDIA
                        showModalSheet = true
                    },
                    onTxClick = {
                        currentModalSelector = DetailSelector.TXS
                        showModalSheet = true
                    },
                    onContactClick = {
                        currentModalSelector = DetailSelector.CONTACT
                        showModalSheet = true
                    }
                )
            }

                Box(modifier = modifier
                    .fillMaxSize()
                    .customBlur(if (focusMode.value) 100f else 0f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (focusMode.value) {
                                    //Toast.makeText(context,"focus tap press -kkkk",Toast.LENGTH_SHORT).show()
                                    focusMode.value = false
                                }
                            },
                            onLongPress = {
                                if (focusMode.value) {
                                    //Toast.makeText(context,"focus tap press -kkkk",Toast.LENGTH_SHORT).show()
                                    focusMode.value = false
                                }
                            }
                        )
                    },
                ){

                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .layoutId("conversations")
                    ) {

                        ChatHeader(
                            name = recipient?.getDisplayName() ?: "",
                            image = recipient?.contact?.photoUri ?: "",
                            ens = listOf(""),
                            onBackClick = navigateBackToConversations,
                            onPhoneClick = onPhoneClicked,
                            onContactClick = {
                                //currentModalSelector = ModalSelector.CONTACT
                                profileview.value = true
                                //showAssetSheet = true
                            },
                            selectMode = selectMode,
                            selectAll = selectAll,
                            onSelectAll = {
                                selectAll.value = !selectAll.value
                            }
                        )


                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            color = Colors.DARK_GRAY
                        )

                        when(messagesUiState) {
                            is MessagesUiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {

                                    Text(
                                        text = "Loading...",
                                        fontSize = 12.sp,
                                        fontFamily = Fonts.INTER,
                                        color = Colors.WHITE,
                                    )
                                }
                            }

                            is MessagesUiState.Success -> {

                                val listState = rememberLazyListState()

                                LazyColumn(
                                    state = listState,
                                    reverseLayout = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                ) {

                                    items(
                                        items = messagesUiState.messages,
                                        key = {message -> message.id} // because message can be deleted
                                    ) { message ->

                                        val prevAuthor = messagesUiState.messages.getOrNull(messagesUiState.messages.indexOf(message) - 1)?.address
                                        val nextAuthor = messagesUiState.messages.getOrNull(messagesUiState.messages.indexOf(message) + 1)?.address
                                        val isFirstMessageByAuthor = prevAuthor != message.address
                                        val isLastMessageByAuthor = nextAuthor != message.address


                                        if (isValidTransactionMessage(message.body)) {
                                            extractTransactionDetails(message.body)?.let {
                                                TxMessage(
                                                    amount = it.amount.toDouble(),
                                                    txUrl = it.url,
                                                    isUserMe = message.isMe(),
                                                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                    isLastMessageByAuthor = isLastMessageByAuthor,
                                                    networkName = chainIdToReadableName(it.chainId),
                                                )
                                            }
                                        } else {
                                            MessageItem(
                                                onAuthorClick = { },
                                                msg = message,
                                                isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                isLastMessageByAuthor = isLastMessageByAuthor,
                                                composablePositionState = composablePositionState,
                                                player = videoPlayer,
                                                onPrepareVideo = { onPrepareVideo(it) },
                                                onLongClick = {
                                                    onFocusedMessageUpdate(message)
                                                    focusMode.value = true
                                                },
                                                name = recipient?.getDisplayName() ?: "",
                                                isSelected = selectedMessagesMap.contains(message),
                                                selectMode = selectMode,
                                                onSelect = { selectedMessage ->
                                                    // invert boolean or add
                                                    selectedMessagesMap.compute(selectedMessage) { _, isChecked ->
                                                        isChecked?.let { !it } ?: true
                                                    }
                                                }
                                            ) { selectMode.value = !selectMode.value }
                                        }
                                    }
                                }

                                // jump to last Message
                                LaunchedEffect(key1 = messagesUiState) {
                                    listState.animateScrollToItem(0)
                                }

                                // Handle select all
                                if(selectMode.value) {
                                    if (selectAll.value) {
                                        messagesUiState.messages.forEach { msg ->
                                            selectedMessagesMap[msg] = true
                                        }
                                    } else {
                                        selectedMessagesMap.replaceAll { _, _ -> false}
                                    }
                                }
                            }
                        }


                        Column(
                            modifier = modifier
                                .padding(top = 8.dp, bottom = 24.dp, end = 12.dp, start = 12.dp)
                        ) {
                            AnimatedContent(
                                modifier = Modifier.fillMaxWidth(),
                                targetState = selectMode.value, label = "",
                            ) { targetMode ->
                                if(targetMode) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                            .height(56.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ){
                                        IconButton(
                                            onClick = {  },
                                            modifier = modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Go back",
                                                tint = Colors.WHITE,
                                                modifier = modifier.size(28.dp)
                                            )
                                        }
                                        Text(
                                            text = "${selectedMessagesMap.size} Selected Messages",//"${selectedMessaged.size} Selected Messages",
                                            fontSize = 16.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = Fonts.INTER,
                                        )

                                        IconButton(
                                            onClick = {  },
                                            modifier = modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Shortcut,
                                                contentDescription = "Go back",
                                                tint = Colors.WHITE,
                                                modifier = modifier.size(28.dp)
                                            )
                                        }
                                    }
                                } else {

                                    AttachmentRow(
                                        selectedAttachments = attachments.toList(),
                                        onToggleAttachment = { onToggleAttachment(it) }
                                    )

                                    SelectionContainer {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                        ) {
                                            var startAnimation by remember { mutableStateOf(false) }
                                            val animationSpec = tween<Float>(
                                                durationMillis = 400,
                                                easing = LinearOutSlowInEasing
                                            )
                                            val rotationAngle by animateFloatAsState(
                                                targetValue = if (startAnimation) 45f else 0f,
                                                animationSpec = animationSpec,
                                                label = ""
                                            )
                                            IconButton(
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .clip(CircleShape)
                                                    .size(42.dp),
                                                enabled = true,
                                                onClick = {
                                                    //onChangeShowActionBar()
                                                    dismissKeyboard()
                                                    controller?.hide() // Keyboard

                                                    showActionbar = !showActionbar

                                                    if (showSelectionbar) {
                                                        showSelectionbar = false
                                                    }
                                                    startAnimation = !startAnimation
                                                },
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Add,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .graphicsLayer(rotationZ = rotationAngle),
                                                        contentDescription = "Send",
                                                        tint = Color.White
                                                    )
                                                }

                                            }

                                            var lastFocusState by remember { mutableStateOf(false) }
                                            TextField(
                                                shape = RoundedCornerShape(35.dp),
                                                value = textState,
                                                onValueChange = { textState = it },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(35.dp))
                                                    .border(
                                                        2.dp,
                                                        Colors.DARK_GRAY,
                                                        RoundedCornerShape(35.dp)
                                                    )
                                                    .heightIn(min = 56.dp, max = 100.dp)
                                                    .onFocusChanged { state ->
                                                        if (lastFocusState != state.isFocused) {

                                                            if (state.isFocused) {
                                                                currentInputSelector =
                                                                    InputSelector.NONE
                                                                //resetScroll()
                                                            }
                                                            textFieldFocusState = state.isFocused

                                                        }
                                                        lastFocusState = state.isFocused
                                                    },

                                                placeholder = {
                                                    Text("Type a message")
                                                },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Colors.WHITE,
                                                    unfocusedTextColor = Colors.WHITE,
                                                    focusedContainerColor = Colors.TRANSPARENT,
                                                    unfocusedContainerColor = Colors.TRANSPARENT,
                                                    disabledContainerColor = Colors.TRANSPARENT,
                                                    cursorColor = Colors.WHITE,
                                                    errorCursorColor = Colors.WHITE,
                                                    focusedBorderColor = Colors.TRANSPARENT,
                                                    unfocusedBorderColor = Colors.TRANSPARENT,
                                                    focusedPlaceholderColor = Colors.GRAY,
                                                    unfocusedPlaceholderColor = Colors.GRAY,
                                                ),
                                                textStyle = TextStyle(
                                                    fontWeight = FontWeight.Medium,
                                                    fontFamily = Fonts.INTER,
                                                    fontSize = 18.sp,
                                                    color = Colors.WHITE,
                                                )

                                            )



                                            AnimatedVisibility(
                                                textState.text.isNotBlank() || attachments.isNotEmpty(),
                                                enter = expandHorizontally(),
                                                exit = shrinkHorizontally(),
                                            ) {
                                                SendButton(
                                                    background = sendbuttonbg,
                                                    list = list,
                                                    selectedTab = index,
                                                    onClick = {
                                                        // Move scroll to bottom
                                                        //resetScroll()
                                                        dismissKeyboard()

                                                        if (showSelectionbar) {
                                                            showSelectionbar = false
                                                        }

                                                        if (showActionbar) {
                                                            showActionbar = false
                                                            startAnimation = !startAnimation
                                                        }



                                                        controller?.hide() // Keyboard

                                                        lastFocusState = false
                                                        textFieldFocusState = false
                                                        onSendMessageClicked(textState.text)
                                                        textState = TextFieldValue()
                                                    })
                                            }

                                        }
                                    }

                                    // Animated visibility will eventually remove the item from the composition once the animation has finished.
                                    AnimatedVisibility(showActionbar) {

                                        SelectorExpanded(
                                            onSelectorChange = {
                                                currentInputSelector = it
                                            },
                                            onShowSelectionbar = {
                                                if (!showSelectionbar) {
                                                    showSelectionbar = true
                                                }
                                            },
                                            onHideKeyboard = { controller?.hide() },
                                            recipient = recipient
                                        )
                                    }


                                    AnimatedVisibility(showSelectionbar) {
                                        if(showActionbar){
                                            Surface(
                                                color = Colors.TRANSPARENT,
                                                tonalElevation = 8.dp
                                            ) {
                                                when (currentInputSelector) {

                                                    InputSelector.CONTACT -> ContactSheet(
                                                        contacts = contacts,
                                                        attachments = attachments,
                                                        onContactClicked = { onContactSelected(it) }
                                                    )
                                                    //InputSelector.EMOJI -> FunctionalityNotAvailablePanel("Emoji")
                                                    InputSelector.WALLET -> WalletSelector(
                                                        focusRequester = FocusRequester(),
                                                        onSendEth = {
                                                            onSendEthClicked(it)
                                                            dismissKeyboard()
                                                            controller?.hide() // Keyboard

                                                            showActionbar = !showActionbar
                                                            if(showSelectionbar){
                                                                showSelectionbar = false
                                                            }
                                                        },
                                                        tokenBalance = tokenBalance,
                                                        chainName = chainName
                                                    )
                                                    InputSelector.PICTURE -> {
                                                        GallerySheet(
                                                            media = media,
                                                            attachments = attachments,
                                                            onMediaClicked = { onToggleAttachment(it) },
                                                        )
                                                    }

                                                    else -> {
                                                        //TODO: commented the code because sending message defaults to this route and crashes the app
                                                        //throw NotImplementedError()
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

                AnimatedVisibility(
                    focusMode.value && focusedMessage != null ,
                    enter = fadeIn(
                        animationSpec = tween(300),
                    ),
                    exit = fadeOut(
                        animationSpec = tween(300,),
                    ),
                ){
                    if (focusedMessage != null) {
                        MessageOptionsScreen(
                            modifier = Modifier.layoutId("messageoptions"),
                            message = focusedMessage,
                            composablePositionState = composablePositionState,
                            focusMode = focusMode,
                            onDeleteMessage = onDeleteMessage
                        ) {
                            detailview = true
                        }
                    }

                }
            }

            //Asset ModalSheet

            if(showModalSheet){
                ModalBottomSheet(
                    containerColor= Colors.BLACK,
                    contentColor= Colors.WHITE,

                    modifier = Modifier.fillMaxHeight(0.95f),
                    onDismissRequest = {
                        scope.launch {
                            modalAssetSheetState.hide()
                        }.invokeOnCompletion {
                            if(!modalAssetSheetState.isVisible) showModalSheet = false
                        }
                    },
                    sheetState = modalAssetSheetState
                ) {
                    when(currentModalSelector){
                        DetailSelector.CONTACT -> {

                            ContactSheet(
                                name = recipient?.getDisplayName() ?: "",
                                image = recipient?.contact?.photoUri ?: "",
                                ens = listOf(""),
                                recipient = recipient,
                            )
                        }

                        DetailSelector.MEDIA -> MediaSheet(messagesUiState)
                        DetailSelector.MEMBERS -> MembersSheet()
                        DetailSelector.TXS -> TXSheet(messagesUiState)
                        DetailSelector.ASSET -> AssetPickerSheet()
                    }

                }
            }


        //                TODO: Implement Infooscreen
                AnimatedVisibility(
                    detailview ,
                    enter = slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth }, // Start from right
                        animationSpec = tween(300)
                    )
                    ,
                    exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth }, // Exit to left
                        animationSpec = tween(300)
                    ),
                ){
                    if (focusedMessage != null) {
                        MessageDetailView(
                            message = focusedMessage,
                            isUserMe = focusedMessage.isMe(),
                            player = videoPlayer,
                            onDismissRequest = { detailview = false },
                            name = recipient?.getDisplayName() ?: "",
                            onPrepareVideo = { onPrepareVideo(it) },
                        )
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
    recipient: Recipient?,
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
        recipient?.contact?.ethAddress?.let {
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
fun PreviewChatScreen(){
    //ChatScreen(navigateBackToConversations={},chatUIState=ChatUIState.Success(listOf()))
}