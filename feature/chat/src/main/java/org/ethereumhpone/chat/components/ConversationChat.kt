package org.ethereumhpone.chat.components

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumhpone.chat.ChatUIState
import org.ethereumhpone.chat.MessageOptionsScreen
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.Attachment
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ConversationChat(
    modifier: Modifier = Modifier,
    chatUIState: ChatUIState,
    recipient: Recipient?,
    navigateBackToConversations: () -> Unit,
    onSendMessageClicked: (String, List<Attachment>) -> Unit,

    currentModalSelector: MutableState<ModalSelector>,
    composablePositionState: MutableState<ComposablePosition>,
    showActionbar: MutableState<Boolean>,
    showAssetSheet: MutableState<Boolean>,
    controller: SoftwareKeyboardController?,
    dismissKeyboard: () -> Unit,
    showSelectionbar: MutableState<Boolean>,
    textState: MutableState<TextFieldValue>,
    currentInputSelector: MutableState<InputSelector>,
    focusMode: MutableState<Boolean>,
    focusedmessage: MutableState<Message>,
    textFieldFocusState: MutableState<Boolean>,
    profileview: MutableState<Boolean>,

//    sharedTransitionScope: SharedTransitionScope,
//    animatedVisibilityScope: AnimatedVisibilityScope

){

        Box(modifier = modifier
            .fillMaxSize()
            .customBlur(if (focusMode.value) 100f else 0f)){

            //Profile Vie
                    Column(modifier = Modifier.fillMaxSize()) {
                        recipient?.let {
                            ChatHeader(
                                name = it.getDisplayName(),
                                image = it.contact?.photoUri,
                                phoneNumber = it.contact?.numbers,
//                        ens = emptyList(),
                                onBackClick = navigateBackToConversations,
                                isTrailContent = false,
                                trailContent= {},
                                onContactClick = {
                                    currentModalSelector.value = ModalSelector.CONTACT
                                    profileview.value = !profileview.value
                                    //showAssetSheet.value = true
                                },
                                profileview = profileview
                                //modifier = Modifier.fillMaxSize()
                            )
                        }
                        //-----------

//                        AnimatedVisibility(
//                            focusMode.value,
//                            enter = fadeIn(
//                                animationSpec = tween(300),
//                            ),
//                            exit = fadeOut(
//                                animationSpec = tween(300,),
//                            )
//                        ){
//
//                        }
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ){
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
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



                                                Log.d("DEBUG 200", content.toString())

                                                item {
                                                    Message(

                                                        msg = message,

                                                        isUserMe = message.isMe(),

                                                        isFirstMessageByAuthor = isFirstMessageByAuthor,

                                                        isLastMessageByAuthor = isLastMessageByAuthor,

                                                        composablePositionState = composablePositionState,

                                                        onLongClick = {
                                                            focusedmessage.value = message
                                                            focusMode.value = true
                                                        },

                                                        )

                                                }

                                            }
                                        }
                                    }

                                    else -> {

                                    }
                                }


                                AnimatedVisibility(
                                    visible = !profileview.value,
                                    enter  = expandVertically(),
                                    exit = shrinkVertically(),
                                ) {
                                    Column(
                                        modifier = modifier
                                            .padding(top = 8.dp, bottom = 24.dp, end = 12.dp, start = 12.dp)
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

                                                    showActionbar.value = !showActionbar.value

                                                    if(showSelectionbar.value){
                                                        showSelectionbar.value = false
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
                                                value = textState.value,
                                                onValueChange = { textState.value = it },
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
                                                                currentInputSelector.value =
                                                                    InputSelector.NONE
                                                                //resetScroll()
                                                            }
                                                            textFieldFocusState.value =
                                                                state.isFocused

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
                                                textState.value.text.isNotBlank(),
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

                                                        if(showSelectionbar.value){
                                                            showSelectionbar.value = false
                                                        }

                                                        if(showActionbar.value){
                                                            showActionbar.value = false
                                                            startAnimation = !startAnimation
                                                        }



                                                        controller?.hide() // Keyboard

                                                        lastFocusState = false
                                                        textFieldFocusState.value = false
                                                        onSendMessageClicked(textState.value.text, listOf())
                                                        textState.value = TextFieldValue()
                                                    },
                                                ) {
                                                    Icon(imageVector = Icons.Rounded.ArrowUpward,modifier= Modifier
                                                        .size(32.dp), contentDescription = "Send",tint = Color.White)
                                                }
                                            }

                                        }

                                        // Animated visibility will eventually remove the item from the composition once the animation has finished.
                                        AnimatedVisibility(showActionbar.value) {

                                            SelectorComponent(
                                                currentInputSelector = currentInputSelector,
                                                onSelectorChange = {
                                                    currentInputSelector.value = it
                                                },
                                                onShowSelectionbar = {
                                                    if (!showSelectionbar.value) {
                                                        showSelectionbar.value = true
                                                    }
                                                },
                                                onHideKeyboard = { controller?.hide() },
                                            )
                                        }


                                        AnimatedVisibility(showSelectionbar.value) {
                                            if(showActionbar.value){
                                                Surface(
                                                    color = Colors.TRANSPARENT,
                                                    tonalElevation = 8.dp
                                                ) {
                                                    when (currentInputSelector.value) {
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
//                }


        }



}