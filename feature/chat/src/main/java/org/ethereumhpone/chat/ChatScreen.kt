package org.ethereumhpone.chat

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import org.ethereumhpone.chat.components.ChatHeader
import org.ethereumhpone.chat.components.ContactSheet
import org.ethereumhpone.chat.components.FunctionalityNotAvailablePanel
import org.ethereumhpone.chat.components.ModalSelector
import org.ethereumhpone.chat.components.WalletSelector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ethereumhpone.chat.components.ComposablePosition
import org.ethereumhpone.chat.components.ConversationChat
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.Attachment


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    navigateBackToConversations: () -> Unit,
    threadId: String?,
    viewModel: ChatViewModel = hiltViewModel(),
){
    val chatUIState by viewModel.chatState.collectAsStateWithLifecycle()
    val recipient by viewModel.recipient.collectAsStateWithLifecycle()

    ChatScreen(
        chatUIState = chatUIState,
        recipient = recipient,
        navigateBackToConversations = navigateBackToConversations,
        onSendMessageClicked = viewModel::sendMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    chatUIState: ChatUIState,
    recipient: Recipient?,
    navigateBackToConversations: () -> Unit,
    onSendMessageClicked: (String, List<Attachment>) -> Unit,
){
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()


    //ModalSheets
    var showAssetSheet = remember { mutableStateOf(false) }
    val modalAssetSheetState = rememberModalBottomSheetState(true)

    var currentInputSelector = rememberSaveable { mutableStateOf(InputSelector.NONE) }

    var currentModalSelector = rememberSaveable { mutableStateOf(ModalSelector.CONTACT) }

    val dismissKeyboard = { currentInputSelector.value = InputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector.value != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }

    var textState = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var showActionbar = remember { mutableStateOf(false) }
    var showSelectionbar = remember { mutableStateOf(false) }



    // Used to decide if the keyboard should be shown
    var textFieldFocusState = remember { mutableStateOf(false) }

    val controller = LocalSoftwareKeyboardController.current




    //Message
    val focusMode = remember {
        mutableStateOf(false)
    }

    val composablePositionState = remember { mutableStateOf(ComposablePosition()) }//gets offset of message composable

    var focusedmessage = remember { mutableStateOf(
        org.ethereumhpone.database.model.Message(
            address = "me",
            body = "",
            subject = ""
        )
    ) }

    val profileview = remember {
        mutableStateOf(false)
    }


    val context = LocalContext.current






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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
//            SharedTransitionLayout {
//                AnimatedContent(
//                    profileview,
//                    label = "profile_view"
//                ) { targetState ->
//                    if (!targetState.value) {


                        ConversationChat(
                            chatUIState = chatUIState,
                            recipient = recipient,
                            navigateBackToConversations = navigateBackToConversations,
                            onSendMessageClicked = onSendMessageClicked,
                            currentModalSelector = currentModalSelector,
                            composablePositionState = composablePositionState,
                            showActionbar = showActionbar,
                            showAssetSheet = showAssetSheet,
                            controller = controller,
                            dismissKeyboard = dismissKeyboard,
                            showSelectionbar = showSelectionbar,
                            textState = textState,
                            currentInputSelector = currentInputSelector,
                            focusMode = focusMode,
                            focusedmessage = focusedmessage,
                            textFieldFocusState = textFieldFocusState,
                            profileview = profileview,
//                            animatedVisibilityScope = this@AnimatedContent,
//                            sharedTransitionScope = this@SharedTransitionLayout
                        )

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
                                focusedmessage.value,composablePositionState, focusMode
                            )
                        }

//                    } else {
//                        ProfileDetailScreen(
//                            onBack = {
//                                profileview.value = false
//                            },
//                            recipient = recipient,
//                            animatedVisibilityScope = this@AnimatedContent,
//                            sharedTransitionScope = this@SharedTransitionLayout
//                        )
//                    }
//
//                }
//
//
//
//            }
        }



            //Asset ModalSheet

            if(showAssetSheet.value){
                ModalBottomSheet(
                    containerColor= Colors.BLACK,
                    contentColor= Colors.WHITE,

                    onDismissRequest = {
                        scope.launch {
                            modalAssetSheetState.hide()
                        }.invokeOnCompletion {
                            if(!modalAssetSheetState.isVisible) showAssetSheet.value = false
                        }
                    },
                    sheetState = modalAssetSheetState
                ) {
                    when(currentModalSelector.value){
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
fun PreviewChatScreen(){
    //ChatScreen(navigateBackToConversations={},chatUIState=ChatUIState.Success(listOf()))
}