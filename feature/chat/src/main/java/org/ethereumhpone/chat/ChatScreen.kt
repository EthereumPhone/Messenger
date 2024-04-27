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


@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    navigateBackToConversations: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
){
    val chatUIState by viewModel.chatState.collectAsStateWithLifecycle()

    ChatScreen(
        chatUIState = chatUIState,
        navigateBackToConversations = navigateBackToConversations
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    chatUIState: ChatUIState,
    navigateBackToConversations: () -> Unit
){


    val focusManager: FocusManager = LocalFocusManager.current



//
//        val authorMe = "me"
//
//        LazyColumn(
//            reverseLayout = true,
//            modifier = Modifier
//
//                .padding(start = 24.dp, end = 24.dp,)
//        ){
//            initialMessages.forEachIndexed {  index, message ->
//
//                val prevAuthor = initialMessages.getOrNull(index - 1)?.author
//                val nextAuthor = initialMessages.getOrNull(index + 1)?.author
//                val content = initialMessages[index]
//                val isFirstMessageByAuthor = prevAuthor != content.author
//                val isLastMessageByAuthor = nextAuthor != content.author
//
//                item {
//                    Message(
//                        onAuthorClick = {  },
//                        msg = message,
//                        isUserMe = message.author == authorMe,
//                        isFirstMessageByAuthor = isFirstMessageByAuthor,
//                        isLastMessageByAuthor = isLastMessageByAuthor
//                    )
//                }
//
//            }
//        }
//
//
//
//
//        Column {
//            UserInput(onMessageSent = {})
//        }
//
//
//
//
//
//
//    }



    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()


    //ModalSheets
    var showAssetSheet by remember { mutableStateOf(false) }
    val modalAssetSheetState = rememberModalBottomSheetState(true)

    var showCameraWithPerm by remember {
        mutableStateOf(false)
    }


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

    val context =  LocalContext.current
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
                Box(modifier = modifier.fillMaxSize().customBlur(100f)){
                    Column(modifier = Modifier.fillMaxSize()) {
                        ChatHeader(
                            name = "Mark Katakowski",
                            image = "",
                            ens = listOf("mk.eth"),
                            onBackClick = navigateBackToConversations,
                            isTrailContent = false,
                            trailContent= {},
                            onContactClick = {

                                currentModalSelector = ModalSelector.CONTACT

                                showAssetSheet = true

                            }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ){
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                //.background(Color.Blue)
                            ){


                                //MOCKDATA
                                val initialMessages = listOf(
                                    org.ethereumhpone.database.model.Message(
                                        address = "me",
                                        body = "*txsent,1,0.08,ETH",
                                        subject = "8:10 PM"
                                    ),
                                    org.ethereumhpone.database.model.Message(
                                        address = "me",
                                        body = "Check it out!",
                                        subject = "8:07 PM"
                                    ),
                                    org.ethereumhpone.database.model.Message(
                                        address = "me",
                                        body = "Thank you!",
                                        subject = "8:06 PM",
                                    ),
                                    org.ethereumhpone.database.model.Message(
                                        address = "Taylor Brooks",
                                        body = "*txsent,1,0.001,ETH",
                                        subject = "8:05 PM"
                                    ),
                                    org.ethereumhpone.database.model.Message(
                                        address = "Taylor Brooks",
                                        body = "You can use all the same stuff",
                                        subject = "8:05 PM"
                                    ),
                                    org.ethereumhpone.database.model.Message(
                                        address = "Taylor Brooks",
                                        body = "@aliconors Take a look at the `Flow.collectAsStateWithLifecycle()` APIs",
                                        subject = "8:05 PM"
                                    ),
                                    org.ethereumhpone.database.model.Message(
                                        address = "Taylor Brooks",
                                        body = "Compose newbie as well, have you looked at the JetNews sample? " +
                                                "Most blog posts end up out of date pretty fast but this sample is always up to " +
                                                "date and deals with async data loading (it's faked but the same idea " +
                                                "applies)  https://goo.gle/jetnews",
                                        subject = "8:04 PM"
                                    ),
                                    org.ethereumhpone.database.model.Message(
                                        address = "me",
                                        body = "Compose newbie: I’ve scourged the internet for tutorials about async data " +
                                                "loading but haven’t found any good ones " +
                                                "What’s the recommended way to load async data and emit composable widgets?",
                                        subject = "8:03 PM"
                                    )

                                )

                                val authorMe = "me"


                                //MOCK
                                LazyColumn(
                                    reverseLayout = true,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ){
                                    for (index in initialMessages.indices) {



                                        val prevAuthor = initialMessages.getOrNull(index - 1)?.address
                                        val nextAuthor = initialMessages.getOrNull(index + 1)?.address
                                        val content = initialMessages[index]
                                        val isFirstMessageByAuthor = prevAuthor != content.address
                                        val isLastMessageByAuthor = nextAuthor != content.address
                                        item {
                                            if(content.body.startsWith("*txsent,")){
                                                TxMessage(
                                                    onAuthorClick = {  },
                                                    msg = content,
                                                    isUserMe = content.address == authorMe,
                                                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                    isLastMessageByAuthor = isLastMessageByAuthor
                                                )
                                            }else{
                                                Message(
                                                    onAuthorClick = {  },
                                                    msg = content,
                                                    isUserMe = content.address == authorMe,
                                                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                                                    isLastMessageByAuthor = isLastMessageByAuthor
                                                )
                                            }

                                        }

                                    }
                                }

                                //REAL
                                /*           when(chatUIState) {
                                //
                                //                is ChatUIState.Loading -> {
                                //
                                //                    Box(
                                //
                                //
                                //                        modifier = Modifier
                                //
                                //                            .weight(1f)
                                //
                                //                            .fillMaxSize()
                                //
                                //                            .padding(horizontal = 24.dp),
                                //
                                //                        contentAlignment = Alignment.Center
                                //
                                //                    ) {
                                //
                                //                        Text(
                                //
                                //                            text = "Loading...",
                                //
                                //                            fontSize = 12.sp,
                                //
                                //                            fontFamily = Fonts.INTER,
                                //
                                //
                                //                            color = Colors.WHITE,
                                //
                                //
                                //                            )
                                //
                                //                    }
                                //
                                //                }
                                //
                                //                is ChatUIState.Success -> {
                                //
                                //
                                //                    LazyColumn(
                                //
                                //                        reverseLayout = true,
                                //
                                //                        modifier = Modifier
                                //
                                //                            .weight(1f)
                                //
                                //                            .fillMaxWidth()
                                //
                                //                            .padding(horizontal = 24.dp)
                                //
                                //                    ) {
                                //
                                //                        chatUIState.messages.forEachIndexed { index, message ->
                                //
                                //                            val prevAuthor = chatUIState.messages.getOrNull(index - 1)?.address
                                //
                                //                            val nextAuthor = chatUIState.messages.getOrNull(index + 1)?.address
                                //
                                //                            val content = chatUIState.messages[index]
                                //
                                //                            val isFirstMessageByAuthor = prevAuthor != content.address
                                //
                                //                            val isLastMessageByAuthor = nextAuthor != content.address
                                //
                                //
                                //
                                //                            item {
                                //
                                //                                Message(
                                //
                                //                                    onAuthorClick = { },
                                //
                                //                                    msg = message,
                                //
                                //                                    isUserMe = false,//message.author == authorMe,
                                //
                                //                                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                                //
                                //                                    isLastMessageByAuthor = isLastMessageByAuthor
                                //
                                //                                )
                                //
                                //                            }
                                //
                                //                        }
                                //                    }
                                //                }
                                //            }*/

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
                                                    //onMessageSent(textState.text)
                                                    // Reset text field and close keyboard
                                                    textState = TextFieldValue()
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

                                                },//onMessageSent,
                                            ) {
                                                Icon(imageVector = Icons.Rounded.ArrowUpward,modifier= Modifier
                                                    .size(32.dp), contentDescription = "Send",tint = Color.White)
                                            }
                                        }

//                    if(textState.text.isNotBlank()){
//
//                    }

                                    }

                                    // Animated visibility will eventually remove the item from the composition once the animation has finished.
                                    AnimatedVisibility(showActionbar) {
//                    if (showActionbar){

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
                                        //}
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
                MessageOptionsScreen()
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
    ChatScreen(navigateBackToConversations={},chatUIState=ChatUIState.Loading)
}



private const val FRACTAL_SHADER_SRC = """
    uniform float2 size;
    uniform float time;
    uniform shader composable;
    
    float f(float3 p) {
        p.z -= time * 5.;
        float a = p.z * .1;
        p.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
        return .1 - length(cos(p.xy) + sin(p.yz));
    }
    
    half4 main(float2 fragcoord) { 
        float3 d = .5 - fragcoord.xy1 / size.y;
        float3 p=float3(0);
        for (int i = 0; i < 32; i++) {
          p += f(p) * d;
        }
        return ((sin(p) + float3(2, 5, 12)) / length(p)).xyz1;
    }
"""

@Composable
@Preview
fun TestComposable(){


    val shader = RuntimeShader(FRACTAL_SHADER_SRC)
//        val shader = RuntimeShader(FRACTAL_SHADER_SRC) // TODO: uncomment to see 2nd shader

    val COLOR_SHADER_SRC =
        """half4 main(float2 fragCoord) {
      return half4(1,0,0,1);
   }""".trimIndent()
    val fixedColorShader = RuntimeShader(COLOR_SHADER_SRC)


    Box(
        modifier =
        Modifier
            .size(500.dp)
    ) {
        BlurContainer(){
            Image(
                painter = painterResource(id = R.drawable.butterfly),
                modifier = Modifier
                    .onSizeChanged { size ->
                        shader.setFloatUniform(
                            "size",
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                    }
//                .graphicsLayer {
//                    clip = true
//                    //shader.setFloatUniform("time",timeMs.value)
//                    renderEffect =
//                        RenderEffect
//                            .createRuntimeShaderEffect(shader, "composable")
//                            .asComposeRenderEffect()
//                },
                ,
                contentScale = ContentScale.Crop,
                contentDescription = null)
        }


    }
}


