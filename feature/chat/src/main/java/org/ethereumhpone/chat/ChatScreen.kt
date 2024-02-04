package org.ethereumhpone.chat

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.runtime.Composable
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
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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


@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    navigateBackToConversations: () -> Unit
){
    ChatScreen(
        navigateBackToConversations = navigateBackToConversations
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    navigateBackToConversations: () -> Unit
){

//    Column(
//        verticalArrangement = Arrangement.SpaceBetween,
//        modifier = modifier
//            .background(Color.Black)
//            .fillMaxSize()
//    ) {
//
//        Header(
//            name = "Mark Katakowski",
//            image = "",
//            onBackClick = {},
//            isTrailContent = false,
//            trailContent= {},
//        )
//
//
//

    val focusManager: FocusManager = LocalFocusManager.current


    val initialMessages = listOf(

            org.ethereumhpone.chat.model.Message(
                "me",
                "Thank you!",
                "8:06 PM",
                R.drawable.ethos
            ),
            org.ethereumhpone.chat.model.Message(
                "Taylor Brooks",
                "You can use all the same stuff",
                "8:05 PM"
            ),
            org.ethereumhpone.chat.model.Message(
                "Taylor Brooks",
                "@aliconors Take a look at the `Flow.collectAsStateWithLifecycle()` APIs",
                "8:05 PM"
            ),
            org.ethereumhpone.chat.model.Message(
                "John Glenn",
                "Compose newbie as well, have you looked at the JetNews sample? " +
                        "Most blog posts end up out of date pretty fast but this sample is always up to " +
                        "date and deals with async data loading (it's faked but the same idea " +
                        "applies)  https://goo.gle/jetnews",
                "8:04 PM"
            ),
            org.ethereumhpone.chat.model.Message(
                "me",
                "Compose newbie: I’ve scourged the internet for tutorials about async data " +
                        "loading but haven’t found any good ones " +
                        "What’s the recommended way to load async data and emit composable widgets?",
                "8:03 PM"
            )

        )
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
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ){ paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {



            val authorMe = "me"

        LazyColumn(
            reverseLayout = true,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ){
            initialMessages.forEachIndexed {  index, message ->

                val prevAuthor = initialMessages.getOrNull(index - 1)?.author
                val nextAuthor = initialMessages.getOrNull(index + 1)?.author
                val content = initialMessages[index]
                val isFirstMessageByAuthor = prevAuthor != content.author
                val isLastMessageByAuthor = nextAuthor != content.author

                item {
                    Message(
                        onAuthorClick = {  },
                        msg = message,
                        isUserMe = message.author == authorMe,
                        isFirstMessageByAuthor = isFirstMessageByAuthor,
                        isLastMessageByAuthor = isLastMessageByAuthor
                    )
                }

            }
        }


            Column(modifier = modifier.padding(top = 8.dp, bottom = 24.dp, end = 12.dp, start = 12.dp)) {




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
                            .border(2.dp, Colors.DARK_GRAY, RoundedCornerShape(35.dp))
                            .heightIn(min = 56.dp, max = 100.dp)
                            .onFocusChanged { state ->
                                if (lastFocusState != state.isFocused) {

                                    if (state.isFocused) {
                                        currentInputSelector = InputSelector.NONE
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


                    if(textState.text.isNotBlank()){
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

                }

                if (showActionbar){
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

                if (showSelectionbar){
                    Surface(
                        color = Colors.TRANSPARENT,
                        tonalElevation = 8.dp
                    ) {
                        when (currentInputSelector) {
                            InputSelector.EMOJI -> FunctionalityNotAvailablePanel("Emoji")
                            InputSelector.WALLET -> FunctionalityNotAvailablePanel("Wallet")
                            InputSelector.PICTURE -> FunctionalityNotAvailablePanel("Picture") // TODO: link to Camera
                            else -> {
                                throw NotImplementedError()
                            }
                        }
                    }
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
}

//fun onTextFieldFocused(focused: Boolean){
//    if (focused) {
//        currentInputSelector = InputSelector.NONE
//        resetScroll()
//    }
//    textFieldFocusState = focused
//}

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
                //onChangeShowActionBar()
//                                showActionbar = !showActionbar
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
    ChatScreen(navigateBackToConversations={})
}