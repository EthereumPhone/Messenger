package org.ethereumhpone.chat

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import org.ethereumhpone.chat.components.ChatHeader
import org.ethereumhpone.chat.components.ContactSheet
import org.ethereumhpone.chat.components.FunctionalityNotAvailablePanel
import org.ethereumhpone.chat.components.ModalSelector
import org.ethereumhpone.chat.components.WalletSelector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ethereumhpone.chat.components.GallerySheet
import org.ethereumhpone.chat.components.TxMessage
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.Attachment



@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    navigateBackToConversations: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
){
    val messagesUiState by viewModel.messagesState.collectAsStateWithLifecycle()
    val recipient by viewModel.recipientState.collectAsStateWithLifecycle()
    val tokenBalance by viewModel.ethBalance.collectAsStateWithLifecycle()
    val chainName by viewModel.chainName.collectAsStateWithLifecycle()
    val currentChainId by viewModel.currentChainId.collectAsStateWithLifecycle()
    val attachments by viewModel.media.collectAsStateWithLifecycle()

    ChatScreen(
        messagesUiState = messagesUiState,
        recipient = recipient,
        attachments = attachments,
        navigateBackToConversations = navigateBackToConversations,
        tokenBalance = tokenBalance,
        chainName = chainName,
        onSendEthClicked = viewModel::sendEth,
        onSendMessageClicked = viewModel::sendMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    messagesUiState: MessagesUiState,
    recipient: Recipient?,
    attachments: List<Attachment> = emptyList(),
    navigateBackToConversations: () -> Unit,
    onSendEthClicked: (amount: Double) -> Unit,
    tokenBalance: Double,
    chainName: String,
    onSendMessageClicked: (String, List<Attachment>) -> Unit,

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

    val controller = LocalSoftwareKeyboardController.current




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
            Box(modifier = modifier.fillMaxSize()) {
                Box(modifier = modifier.fillMaxSize()){
                    Column(modifier = Modifier.fillMaxSize()) {
                        recipient?.let {
                            ChatHeader(
                                name = it.getDisplayName(),
                                image = "",
                                ens = listOf(""),
                                onBackClick = navigateBackToConversations,
                                isTrailContent = false,
                                trailContent= {},
                                onContactClick = {

                                    currentModalSelector = ModalSelector.CONTACT

                                    showAssetSheet = true

                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ){
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
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

                                            messagesUiState.messages.sortedBy { it.date }.reversed().forEachIndexed { index, message ->

                                                val prevAuthor = messagesUiState.messages.getOrNull(index - 1)?.address

                                                val nextAuthor = messagesUiState.messages.getOrNull(index + 1)?.address

                                                val content = messagesUiState.messages[index]

                                                val isFirstMessageByAuthor = prevAuthor != content.address

                                                val isLastMessageByAuthor = nextAuthor != content.address



                                                item {
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
                                                                onAuthorClick = {}
                                                            )
                                                        } ?:
                                                        Message(
                                                            onAuthorClick = { },
                                                            msg = message,
                                                            isUserMe = message.isMe(),
                                                            isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                            isLastMessageByAuthor = isLastMessageByAuthor
                                                        )
                                                    } else {
                                                        Message(
                                                            onAuthorClick = { },
                                                            msg = message,
                                                            isUserMe = message.isMe(),
                                                            isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                            isLastMessageByAuthor = isLastMessageByAuthor
                                                        )
                                                    }
                                                }

                                            }
                                        }
                                    }
                               }
                                Column(
                                    modifier = modifier.padding(top = 8.dp, bottom = 24.dp, end = 12.dp, start = 12.dp)
                                ) {

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    ) {
                                        var startAnimation by remember { mutableStateOf(false) }
                                        val animationSpec = tween<Float>(durationMillis = 400, easing = LinearOutSlowInEasing)
                                        val rotationAngle by animateFloatAsState(
                                            targetValue = if (startAnimation) 45f else 0f,
                                            animationSpec = animationSpec,

                                            )
                                        IconButton(
                                            modifier = Modifier
                                                .padding(top = 8.dp)
                                                .clip(CircleShape)
                                                .size(42.dp)
                                            ,
                                            enabled = true,
                                            onClick = {
                                                //onChangeShowActionBar()
                                                dismissKeyboard()
                                                controller?.hide() // Keyboard

                                                showActionbar = !showActionbar

                                                if(showSelectionbar){
                                                    showSelectionbar = false
                                                }
                                                startAnimation = !startAnimation
                                            },
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center
                                            ){
                                                Icon(imageVector = Icons.Filled.Add, modifier= Modifier
                                                    .size(32.dp)
                                                    .graphicsLayer(rotationZ = rotationAngle),contentDescription = "Send",tint = Color.White)
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
                                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                                focusedTextColor = Colors.WHITE,
                                                unfocusedTextColor = Colors.WHITE,
                                                containerColor= Colors.TRANSPARENT,
                                                focusedBorderColor =  Colors.TRANSPARENT,
                                                unfocusedBorderColor = Colors.TRANSPARENT,
                                                cursorColor = Colors.WHITE,
                                                errorCursorColor = Colors.WHITE,
                                                focusedPlaceholderColor = Colors.GRAY,
                                                unfocusedPlaceholderColor = Colors.GRAY,
                                            ),
                                            textStyle =  TextStyle(
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = Fonts.INTER,
                                                fontSize = 18.sp,
                                                color = Colors.WHITE,
                                            )

                                        )

                                        AnimatedVisibility(
                                            textState.text.isNotBlank(),
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
                                                    onSendMessageClicked(textState.text, listOf())
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
                                                        GallerySheet(media = attachments) {

                                                        }
                                                    }

                                                    else -> {
                                                        throw NotImplementedError()
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
                //MessageOptionsScreen()
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
                    when(currentModalSelector){
                        ModalSelector.CONTACT -> {
                            recipient?.contact?.let {
                                ContactSheet(
                                    contact = it,
                                    ens = listOf("")
                                )
                            }
                        }
                        ModalSelector.ASSETS -> AssetPickerSheet()
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
@Preview
fun MessageOptionsScreen(){
    Box(modifier = Modifier
        .fillMaxSize()
        //.background(Brush.horizontalGradient(colorStops = colorStops), alpha = 0.5f)
        .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MessageReactions()
            Message(
                onAuthorClick = {  },
                msg = org.ethereumhpone.database.model.Message(
                    address = "me",
                    body = "Check it out!",
                    subject = "8:07 PM"
                ),
                isUserMe = true,
                isFirstMessageByAuthor = true,
                isLastMessageByAuthor = true
            )
            MessageActionList()
        }

    }
}


@Composable
@Preview
fun MessageReactions() {
    Box(modifier = Modifier


    ) {
        Row {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier

                    .clip(CircleShape)
                    .background(Colors.DARK_GRAY)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier.size(28.dp),tint=Colors.GRAY, imageVector = Icons.Filled.Favorite, contentDescription = "")
                }
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier.size(28.dp),tint=Colors.GRAY, imageVector = Icons.Filled.ThumbUp, contentDescription = "")
                }
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier.size(28.dp),tint=Colors.GRAY, imageVector = Icons.Filled.ThumbDown, contentDescription = "")
                }
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier.size(28.dp),tint=Colors.GRAY, imageVector = Icons.Filled.AddComment, contentDescription = "")
                }
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Colors.GRAY),tint=Colors.WHITE, imageVector = Icons.Rounded.Add, contentDescription = "")
                }

            }

        }
    }
}


@Composable
@Preview
fun MessageActionList() {
    Box(modifier = Modifier
        .graphicsLayer {
            shape = RoundedCornerShape(12.dp)
            clip = true
        }
        .background(Colors.DARK_GRAY)
        .width(200.dp)

    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(text = "Copy", fontFamily = Fonts.INTER, fontWeight = FontWeight.Medium, color=Colors.WHITE)
                Icon(tint= Colors.WHITE, modifier = Modifier.size(20.dp), imageVector = Icons.Outlined.ContentCopy, contentDescription = "")
            }
            Divider(color=Colors.GRAY)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(text = "Info", fontFamily = Fonts.INTER,fontWeight = FontWeight.Medium, color=Colors.WHITE)
                Icon(tint= Colors.WHITE, modifier = Modifier.size(20.dp), imageVector = Icons.Outlined.Info, contentDescription = "")
            }
            Divider(color=Colors.GRAY)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(text = "Delete", fontFamily = Fonts.INTER,
                    fontWeight = FontWeight.Medium, color=Colors.ERROR)
                Icon(tint= Colors.ERROR, modifier = Modifier.size(20.dp), imageVector = Icons.Outlined.Delete, contentDescription = "")
            }

        }
    }
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