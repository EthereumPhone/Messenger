package org.ethereumhpone.chat

import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Message
import androidx.compose.animation.AnimatedVisibility
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.AssetPickerSheet
import org.ethereumhpone.chat.components.Header
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.chat.components.Message
import org.ethereumhpone.chat.components.UserInput
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.core.ethOSTextField
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import org.ethereumhpone.chat.components.ChatHeader
import org.ethereumhpone.chat.components.ContactSheet
import org.ethereumhpone.chat.components.EmojiSelector
import org.ethereumhpone.chat.components.FunctionalityNotAvailablePanel
import org.ethereumhpone.chat.components.InputSelectorButton
import org.ethereumhpone.chat.components.ModalSelector
import org.ethereumhpone.chat.components.SelectorExpanded
import org.ethereumhpone.chat.components.UserInputSelector
import org.ethereumhpone.chat.components.WalletSelector
import org.ethereumhpone.chat.components.addText
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.Lazy
import org.ethereumhpone.chat.components.BlurContainer
import org.ethereumhpone.chat.components.TxMessage
import org.ethereumhpone.chat.components.customBlur
import org.ethereumhpone.chat.model.MockMessage
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