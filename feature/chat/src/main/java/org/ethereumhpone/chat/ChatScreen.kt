package org.ethereumhpone.chat

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import org.ethereumhpone.chat.components.Message
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import org.ethereumhpone.chat.components.ContactSheet
import org.ethereumhpone.chat.components.FunctionalityNotAvailablePanel
import org.ethereumhpone.chat.components.ModalSelector
import org.ethereumhpone.chat.components.WalletSelector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.components.ComposablePosition
import org.ethereumhpone.chat.components.ContactItem
import org.ethereumhpone.chat.components.GallerySheet
import org.ethereumhpone.chat.components.TxMessage
import org.ethereumhpone.chat.components.attachments.AttachmentRow
import org.ethereumhpone.chat.components.customBlur
import org.ethereumhpone.chat.components.makePhoneCall
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.Attachment
import org.ethosmobile.components.library.core.ethOSIconButton


@Composable
fun ChatRoute(
    navigateBackToConversations: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
){
    val messagesUiState by viewModel.messagesState.collectAsStateWithLifecycle()
    val recipient by viewModel.recipientState.collectAsStateWithLifecycle()
    val tokenBalance by viewModel.ethBalance.collectAsStateWithLifecycle()
    val chainName by viewModel.chainName.collectAsStateWithLifecycle()
    val attachments by viewModel.attachments.collectAsStateWithLifecycle()
    val selectedAttachments by viewModel.selectedAttachments.collectAsStateWithLifecycle()

    val focusedMessage by viewModel.focusedMessage.collectAsStateWithLifecycle()

    ChatScreen(
        messagesUiState = messagesUiState,
        recipient = recipient,
        attachments = attachments,
        selectedAttachments = selectedAttachments,
        navigateBackToConversations = navigateBackToConversations,
        tokenBalance = tokenBalance,
        chainName = chainName,
        onSendEthClicked = viewModel::sendEth,
        onAttachmentClicked = viewModel::toggleSelection,
        onSendMessageClicked = viewModel::sendMessage,
        onDeleteMessage = viewModel::deleteMessage,
        focusedMessage = focusedMessage,
        onFocusedMessageUpdate = viewModel::updatefocusedMessage,
        onPhoneClicked = viewModel::callPhone
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    messagesUiState: MessagesUiState,
    recipient: Recipient?,
    attachments: List<Attachment> = emptyList(),
    selectedAttachments: Set<Attachment> = emptySet(),
    navigateBackToConversations: () -> Unit,
    onSendEthClicked: (amount: Double) -> Unit,
    tokenBalance: Double = 0.0,
    chainName: String = "?",
    onAttachmentClicked: (Attachment) -> Unit,
    onSendMessageClicked: (String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    focusedMessage: Message?,
    onFocusedMessageUpdate: (Message) -> Unit,
    onPhoneClicked: () -> Unit
){
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()


    //ModalSheets
    var showAssetSheet by remember { mutableStateOf(false) }
    val modalAssetSheetState = rememberModalBottomSheetState(true)


    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    var currentModalSelector by rememberSaveable { mutableStateOf(ModalSelector.CONTACT) }


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

    val controller = LocalSoftwareKeyboardController.current


    val context = LocalContext.current
    val motionScene = remember {
        context.resources
            .openRawResource(R.raw.motion_scene)
            .readBytes()
            .decodeToString()
    }
    val profileAnimationProgress by animateFloatAsState(

        // specifying target value on below line.
        targetValue = if (profileview.value) 1f else 0f,

        // on below line we are specifying
        // animation specific duration's 1 sec
        animationSpec = tween(1000,)
    )

    val alpha: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(500,500))



    Scaffold (
        containerColor = Color.Black,
        topBar = {
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ){ paddingValues ->
            Box(modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)) {
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
                    recipient?.let {
                        MotionLayout(
                            motionScene = MotionScene(content = motionScene),
                            progress = profileAnimationProgress,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            //------------HEADER START------------
                            Box (
                                modifier = Modifier

                                    .layoutId("box")
                            ){

                            }
                            IconButton(
                                onClick = {
                                    if (profileview.value) {
                                        profileview.value = !profileview.value
                                    } else {
                                        navigateBackToConversations()
                                    }

                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .layoutId("back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBackIosNew,
                                    contentDescription = "Go back",
                                    tint = Colors.WHITE,
                                    modifier = modifier.size(24.dp)
                                )
                            }


                            if (it.contact?.photoUri != "" && !(it.contact?.photoUri.isNullOrEmpty())){
                                Image(
                                    painter = rememberAsyncImagePainter(it.contact?.photoUri),
                                    contentDescription = "profile_image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFF262626))
                                        .clickable {
                                            currentModalSelector = ModalSelector.CONTACT
                                            profileview.value = true //!profileview.value
                                        }
                                        .layoutId("profile_pic")
                                )
                            }
                            else{
                                Image(
                                    painter = painterResource(id = R.drawable.nouns_placeholder),
                                    contentDescription = "placeholder_profile_image",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF262626))
                                        .clickable {
                                            currentModalSelector = ModalSelector.CONTACT
                                            profileview.value = true //!profileview.value
                                        }
                                        .layoutId("profile_pic")
                                )
                            }

                            val name_properties = motionProperties(id = "name")

                            val name_modifier = when(profileview.value){
                                true -> {
                                    Modifier
                                        .animateContentSize()
                                        .fillMaxWidth()
                                        .layoutId("name")
                                }
                                false -> {
                                    Modifier
                                        .animateContentSize()

                                        .clickable {
                                            currentModalSelector = ModalSelector.CONTACT
                                            profileview.value = true //!profileview.value
                                        }
                                        .layoutId("name")
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (it.contact?.numbers?.get(0)  != null) {
                                        makePhoneCall(context, it.contact?.numbers?.get(0)!!.address)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .layoutId("call_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Call,
                                    contentDescription = "Call contact",
                                    tint = Colors.WHITE,
                                    modifier = modifier.size(24.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(if(profileview.value) 10.dp else 2.dp),
                                horizontalAlignment = if(profileview.value) Alignment.CenterHorizontally else Alignment.CenterHorizontally,
                                modifier = name_modifier

                            ) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = it.getDisplayName(),
                                    fontSize = name_properties.value.fontSize("name_fontSize"),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = Fonts.INTER,
                                    modifier = if(profileview.value) Modifier.clickable {
                                        //TODO: implement funcitonality
                                    } else {
                                        Modifier
                                    }

                                )

                                //TODO: enable ens
                                if (false){//profileview.value){
                                    Text(
                                        textAlign = TextAlign.Center,
                                        text = it.getDisplayName(),
                                        fontSize = name_properties.value.fontSize("ens_fontSize"),
                                        color = Colors.GRAY,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = Fonts.INTER,
                                        modifier = if(profileview.value) Modifier
                                            .graphicsLayer(alpha = alpha)
                                            .clickable {
                                                //TODO: implement funcitonality
                                            } else {
                                            Modifier.graphicsLayer(alpha = alpha)
                                        }

                                    )
                                }

                            }


                            //------------HEADER END------------

                            //------------CHAT BEGIN------------


                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .layoutId("conversations")
                            ){
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
                                        LazyColumn(
                                            reverseLayout = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp)
                                        ) {
                                            Log.d("DEBUG", messagesUiState.messages.sortedBy { it.date }.reversed().toString())
                                            items(items = messagesUiState.messages.sortedBy { it.date }.reversed(), key = { it.id }) { message ->

                                                val prevAuthor = messagesUiState.messages.getOrNull(messagesUiState.messages.indexOf(message) - 1)?.address
                                                val nextAuthor = messagesUiState.messages.getOrNull(messagesUiState.messages.indexOf(message) + 1)?.address
                                                val isFirstMessageByAuthor = prevAuthor != message.address
                                                val isLastMessageByAuthor = nextAuthor != message.address

                                                if (isValidTransactionMessage(message.body)) {
                                                    val transactionDetails = extractTransactionDetails(message.body)
                                                    transactionDetails?.let {
                                                        TxMessage(
                                                            amount = it.amount.toDouble(),
                                                            txUrl = it.url,
                                                            isUserMe = message.isMe(),
                                                            isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                            isLastMessageByAuthor = isLastMessageByAuthor,
                                                            networkName = chainIdToReadableName(it.chainId),
                                                        )
                                                    } ?: Message(
                                                        onAuthorClick = { },
                                                        msg = message,
                                                        isUserMe = message.isMe(),
                                                        isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                        isLastMessageByAuthor = isLastMessageByAuthor,
                                                        composablePositionState = composablePositionState,
                                                        onLongClick = {
                                                            onFocusedMessageUpdate(message)
                                                            focusMode.value = true
                                                        },
                                                    )
                                                } else {
                                                    Message(
                                                        onAuthorClick = { },
                                                        msg = message,
                                                        isUserMe = message.isMe(),
                                                        isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                        isLastMessageByAuthor = isLastMessageByAuthor,
                                                        composablePositionState = composablePositionState,
                                                        onLongClick = {
                                                            Log.d("DEBUG Before", "${message.id} - ${message.body} - ${message}")
                                                            onFocusedMessageUpdate(message)
                                                            focusMode.value = true
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Column(
                                    modifier = modifier.padding(top = 8.dp, bottom = 24.dp, end = 12.dp, start = 12.dp)
                                ) {

                                    AttachmentRow(
                                        selectedAttachments = selectedAttachments.toList(),
                                        attachmentRemoved = { onAttachmentClicked(it) }
                                    )

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
                                        SelectionContainer {
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
                                        }

                                        AnimatedVisibility(
                                            textState.text.isNotBlank() || selectedAttachments.isNotEmpty(),
                                            enter = expandHorizontally(),
                                            exit = shrinkHorizontally(),
                                        ) {
                                            IconButton(
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF8C7DF7))
                                                    .size(42.dp),
                                                enabled = true,
                                                onClick = {
                                                    // Move scroll to bottom
                                                    //resetScroll()
                                                    dismissKeyboard()

                                                    if(showSelectionbar){
                                                        showSelectionbar = false
                                                    }

                                                    if(showActionbar){
                                                        showActionbar = false
                                                        startAnimation = !startAnimation
                                                    }



                                                    controller?.hide() // Keyboard

                                                    lastFocusState = false
                                                    textFieldFocusState = false
                                                    onSendMessageClicked(textState.text)
                                                    textState = TextFieldValue()
                                                },
                                            ) {
                                                Icon(imageVector = Icons.Rounded.ArrowUpward,modifier= Modifier
                                                    .size(32.dp), contentDescription = "Send",tint = Color.White)
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
                                                    InputSelector.EMOJI -> FunctionalityNotAvailablePanel("Emoji")
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
                                                            attachments = attachments,
                                                            selectedAttachments = selectedAttachments,
                                                            onItemClicked = { onAttachmentClicked(it) },
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

                            //-----------------------
                            //------------PROFILE VIEW START------------

                            val alpha1: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(1000,500))
                            val alpha2: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(1000,1000))
                            val alpha3: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(1000,1250))
                            val alpha4: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(1000,1750))
                            val alpha5: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(1000,1750))


                            Column (
                                modifier = modifier
                                    .layoutId("profileactions")
                                    .fillMaxHeight()
                                    .padding(start = 24.dp, end = 24.dp, top = 48.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ){
                                Row(
                                    modifier = Modifier.graphicsLayer(alpha = alpha1),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {

                                    Box(modifier = Modifier.graphicsLayer(alpha = alpha1)) {
                                        ethOSIconButton(
                                            onClick = {
                                                if (it.contact?.numbers?.get(0)  != null) {
                                                    makePhoneCall(context, it.contact?.numbers?.get(0)!!.address)
                                                }
                                            },
                                            icon = Icons.Outlined.Call,
                                            contentDescription="Call"
                                        )
                                    }
                                    Box(modifier = Modifier.graphicsLayer(alpha = alpha2)) {
                                        ethOSIconButton(
                                            onClick = { /*TODO*/ },
                                            icon = Icons.Outlined.Contacts,
                                            contentDescription="Contact"
                                        )
                                    }



                                }
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer(alpha = alpha3),
                                    color = Colors.DARK_GRAY
                                )

                                Spacer(modifier = Modifier.height(24.dp))
                                Column(
                                ) {

                                    it.contact?.numbers?.get(0).let {
                                        if (it != null) {
                                            ContactItem(
                                                modifier = Modifier.graphicsLayer(alpha = alpha4),
                                                title= "Phone Number",
                                                detail= it.address
                                            )
                                        }
                                    }

                                    it.contact?.ethAddress.let {
                                        if (it != null && it.isNotBlank()) {
                                            ContactItem(
                                                modifier = Modifier.graphicsLayer(alpha = alpha5),
                                                title= "Ethereum Address",
                                                detail= it
                                            )
                                        }
                                    }

                                    //TODO: ENS
//                                        ContactItem(
//                                            title= "ENS",
//                                            detail= getEnsAddresses(ens)
//                                        )

                                }
                            }
                            //------------PROFILE VIEW END------------
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
                            Modifier.layoutId("messageoptions"),focusedMessage,composablePositionState, focusMode,onDeleteMessage
                        ) {
                            detailview = true
                        }
                    }

                }

//                TODO: Implement Infooscreen
//                AnimatedVisibility(
//                    detailview ,
//                    enter = fadeIn(
//                        animationSpec = tween(300),
//                    ),
//                    exit = fadeOut(
//                        animationSpec = tween(300,),
//                    ),
//                ){
//                    if (focusedMessage != null) {
//                        MessageDetailsScreen(
//                            Modifier,focusedMessage,composablePositionState, focusMode, onDeleteMessage
//                        )
//                    }
//
//                }
            }

            //Asset ModalSheet

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
                    AssetPickerSheet()
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

fun extractTransactionDetails(message: String): TransactionDetails? {
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
                onSelectorChange(InputSelector.EMOJI)
                onHideKeyboard()
                onShowSelectionbar()
            },
        ) {
            Box(
                contentAlignment = Alignment.Center
            ){
                Icon(imageVector = Icons.Outlined.Mood, modifier= Modifier
                    .size(32.dp)
                    ,contentDescription = "Send",tint = Color.White)
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