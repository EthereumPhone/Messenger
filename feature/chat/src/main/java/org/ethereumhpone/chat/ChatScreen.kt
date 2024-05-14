package org.ethereumhpone.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.MutableState
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
import org.ethereumhpone.chat.components.ComposablePosition
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.Attachment


@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    navigateBackToConversations: () -> Unit,
    threadId: String?,
    viewModel: ChatViewModel = hiltViewModel()
){
    val chatUIState by viewModel.chatState.collectAsStateWithLifecycle()
    val recipient by viewModel.recipient.collectAsStateWithLifecycle()

    ChatScreen(
        chatUIState = chatUIState,
        recipient = recipient,
        navigateBackToConversations = navigateBackToConversations,
        onSendMessageClicked = viewModel::sendMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    chatUIState: ChatUIState,
    recipient: Recipient?,
    navigateBackToConversations: () -> Unit,
    onSendMessageClicked: (String, List<Attachment>) -> Unit
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




    //Message
    val focusMode = remember {
        mutableStateOf(false)
    }

    val composablePositionState = remember { mutableStateOf(ComposablePosition()) }//gets offset of message composable

    var focusedmessage by remember { mutableStateOf(
        org.ethereumhpone.database.model.Message(
            address = "me",
            body = "",
            subject = ""
        )
    ) }




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
                                when(chatUIState) {

                                    is ChatUIState.Loading -> {

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

                                    is ChatUIState.Success -> {


                                        LazyColumn(

                                            reverseLayout = true,

                                            modifier = Modifier

                                                .weight(1f)

                                                .fillMaxWidth()

                                                .padding(horizontal = 24.dp)

                                        ) {

                                            chatUIState.messages.sortedBy { it.date }.reversed().forEachIndexed { index, message ->

                                                val prevAuthor = chatUIState.messages.getOrNull(index - 1)?.address

                                                val nextAuthor = chatUIState.messages.getOrNull(index + 1)?.address

                                                val content = chatUIState.messages[index]

                                                val isFirstMessageByAuthor = prevAuthor != content.address

                                                val isLastMessageByAuthor = nextAuthor != content.address



                                                item {

                                                    Message(

                                                        //onAuthorClick = { },

                                                        msg = message,

                                                        isUserMe = message.isMe(),

                                                        isFirstMessageByAuthor = isFirstMessageByAuthor,

                                                        isLastMessageByAuthor = isLastMessageByAuthor,

                                                        composablePositionState = composablePositionState

                                                    )

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
                                                    InputSelector.WALLET -> WalletSelector(focusRequester = FocusRequester(), onOpenAssetPicker = { })//FunctionalityNotAvailablePanel("Wallet")
                                                    InputSelector.PICTURE -> FunctionalityNotAvailablePanel("Picture") // TODO: link to Camera
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

                AnimatedVisibility(
                    focusMode.value,
                    enter = fadeIn(
                        animationSpec = tween(300),
                    ),
                    exit = fadeOut(
                        animationSpec = tween(300,),
                    )
                ){
                    MessageOptionsScreen(
                        focusedmessage,composablePositionState, focusMode
                    )
                }
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
                        ModalSelector.CONTACT -> ContactSheet(
                            name = "Mark Katakowski",
                            image = "",
                            ens = listOf("emunsi.eth"),)
                        ModalSelector.ASSETS -> AssetPickerSheet()
                    }
                }
            }


        }

}


@Composable
fun SelectorExpanded(
    onSelectorChange: (InputSelector) -> Unit,
    onShowSelectionbar: () -> Unit,
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