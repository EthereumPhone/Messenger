package org.ethereumhpone.chat

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.chat.components.message.MessageItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ethereumhpone.chat.components.ChatBottomAppBar
import org.ethereumhpone.chat.components.ChatTopAppBar
import org.ethereumhpone.chat.components.message.ComposablePosition
import org.ethereumhpone.chat.util.generateTestMessages
import org.ethereumhpone.chat.util.truncateToDate
import org.ethereumhpone.chat.util.truncateToMinute
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.domain.model.Attachment
import org.ethereumphone.dgenlibrary.components.TimeHeader
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient


@Composable
fun ChatRoute(
    onBackClick: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    mediaViewModel: MediaViewModel = hiltViewModel()
){
    val videoPlayer = mediaViewModel.exoPlayer
    val messagesUiState by chatViewModel.messagesState.collectAsStateWithLifecycle()
    val contacts by chatViewModel.contacts.collectAsStateWithLifecycle()
    val media by chatViewModel.media.collectAsStateWithLifecycle()
    val recipients by chatViewModel.recipients.collectAsStateWithLifecycle()
    //val tokenBalance by chatViewModel.ethBalance.collectAsStateWithLifecycle()
    val chainName by chatViewModel.chainName.collectAsStateWithLifecycle()
    val attachments by chatViewModel.attachments.collectAsStateWithLifecycle()
   // val ensAddress by chatViewModel.ensAddress.collectAsStateWithLifecycle()

    val selectedMessaged by chatViewModel.selectedMessages.collectAsStateWithLifecycle()





    ChatScreen(
        messageUiState = messagesUiState,
        recipientUiState = recipients,
        contactEntities = contacts,
        media = media,
        attachments = attachments,
        navigateBackToConversations = onBackClick,
        tokenBalance = 2.456, //tokenBalance,
        chainName = chainName,
        videoPlayer = videoPlayer,
        //selectedMessaged = selectedMessaged,
        onSendEthClicked = { },
        /*onSendMessageClicked = chatViewModel::sendMessage,
        onDeleteMessage = chatViewModel::deleteMessage,
        onFocusedMessageUpdate = {},
        onPrepareVideo = mediaViewModel::addVideoUri,
        onContactSelected = chatViewModel::parseContact,
        onToggleAttachment = chatViewModel::toggleAttachment,
        onRemoveSelectedMessage = chatViewModel::removeSelectedMessage,
        onOpenContact = chatViewModel::onOpenContact,
        onAddSelectedMessage = chatViewModel::addSelectedMessage,*/
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    messageUiState: MessageUiState,
    recipientUiState: RecipientUiState,
    contactEntities: List<ContactEntity> = emptyList(),
    media: List<Uri> = emptyList(),
    attachments: Set<Attachment> = emptySet(),
    navigateBackToConversations: () -> Unit,
    onSendEthClicked: (amount: Double) -> Unit,
    tokenBalance: Double = 0.0,
    chainName: String = "?",
    videoPlayer: Player? = null,
    /*onOpenContact: () -> Unit,
    selectedMessaged: List<Message?> = emptyList(),
    onContactSelected: (ContactEntity) -> Unit,
    onToggleAttachment: (Attachment) -> Unit,
    onSendMessageClicked: (String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onFocusedMessageUpdate: (Message) -> Unit,
    onPrepareVideo: (Uri) -> Unit,
    onRemoveSelectedMessage: (Message) -> Unit,
    onAddSelectedMessage: (Message) -> Unit,*/
) {


    //handle focus
    val showBottomSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    //gets offset of message composable
    val composablePositionState = remember { mutableStateOf(ComposablePosition()) }


    val selectMode = remember { mutableStateOf(false) }
    val selectedMessagesMap = remember { mutableMapOf<Message, Boolean>() }

    var showOverlay = remember { mutableStateOf(false) }
    var hasMultipleLines = remember { mutableStateOf(false) }
    val expand = remember { mutableStateOf(false) }
    var shouldRotate by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler(showOverlay.value || (WindowInsets.isImeVisible && !showBottomSheet)) {
        if (showOverlay.value) {
            showOverlay.value = false
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }




    Box(
        modifier = Modifier.fillMaxSize().imePadding()
    ){
        Scaffold (
            topBar = {
                ChatTopAppBar(
                    "",
                    recipientUiState = recipientUiState,
                    onTitleClicked = {},
                    onBackClicked = navigateBackToConversations
                )
            },
            bottomBar = {
                ChatBottomAppBar(
                    attachments,
                    onToggleAttachment = { it -> }, //onToggleAttachment,
                    onSendClick =  { it -> }, //onSendMessageClicked
                    hasMultipleLines =  hasMultipleLines,
                    expand = expand,
                    openAction = {
                        // Show overlay when this action is triggered
                        showOverlay.value = true
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },



                )
            },
            containerColor = dgenBlack,
        ) { paddingValues ->


            Box(modifier= Modifier.fillMaxSize().padding(paddingValues)){
                when(messageUiState) {
                    is MessageUiState.Success -> {
                        val messages = messageUiState.messageEntities
                        val sortedMessages = messages.reversed().sortedBy {truncateToDate(it.date) }

                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier.fillMaxSize(),
                        ) {

                            sortedMessages.reversed().forEachIndexed { index, message ->


                                /*
                                val prevAuthor = messages.getOrNull(messages.indexOf(message) - 1)?.recipient?.id
                                val nextAuthor = messages.getOrNull(messages.indexOf(message) + 1)?.recipient?.id
                                val isFirstMessageByAuthor = prevAuthor != message.recipient.id
                                val isLastMessageByAuthor = nextAuthor != message.recipient.id
                                 */


                                val prevAuthor = messages.getOrNull(messages.indexOf(message) - 1)?.recipient?.id
                                val isFirstMessageByAuthor = prevAuthor != message.recipient.id





                                val prevDate = messages.getOrNull(messages.indexOf(message) - 1)?.date

                                val newprevDate =
                                    prevDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date;


                                val nextDate = message.date.toLocalDateTime(TimeZone.currentSystemDefault())?.date;

                                if (newprevDate != nextDate){
                                    item {
                                        TimeHeader(message.date)
                                    }
                                }

                                item {
                                    MessageItem(
                                        onAuthorClick = { },
                                        msg = message,

                                        composablePositionState = composablePositionState,
                                        player = videoPlayer,
                                        onPrepareVideo = { it -> },//{ onPrepareVideo(it) },
                                        onLongClick = {},//{ onFocusedMessageUpdate(message) },
                                        name = "TEST", // "recipients.first().getDisplayName()", //TODO FIX THIS
                                        isSelected = selectedMessagesMap.contains(message),
                                        selectMode = selectMode,
                                        isXMTP = true,
                                        onSelect = { selectedMessage ->
                                            // invert boolean or add
                                            selectedMessagesMap.compute(selectedMessage) { _, isChecked ->
                                                isChecked?.let { !it } ?: true
                                            }
                                        },
                                        onDoubleClick = { selectMode.value = !selectMode.value },
                                        isFirstMessageByAuthor = isFirstMessageByAuthor
                                    )
                                }

                            }




                        }
                    }
                    else -> {

                    }
                }
            }


        }

        // Overlay with AnimatedVisibility for fade effect
        AnimatedVisibility(
            visible = showOverlay.value,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {

            LaunchedEffect(showOverlay.value) {
                if (showOverlay.value) {
                    delay(300)
                    shouldRotate = true
                } else {
                    shouldRotate = false
                }
            }

            var alpha1 by remember { mutableStateOf(0f) }
            var alpha2 by remember { mutableStateOf(0f) }
            var alpha3 by remember { mutableStateOf(0f) }
            val delayBetweenTexts = 25

            // Animation spec
            val animationSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)

            // Trigger animations when parent becomes visible
            LaunchedEffect(showOverlay.value) {
                if (showOverlay.value) {
                    // Reset states
                    alpha1 = 0f
                    alpha2 = 0f
                    alpha3 = 0f

                    // Start sequential animations
                    delay(100) // Small initial delay

                    // Animate first text
                    animate(0f, 1f, animationSpec = animationSpec) { value, _ ->
                        alpha1 = value
                    }

                    delay(delayBetweenTexts.toLong())

                    // Animate second text
                    animate(0f, 1f, animationSpec = animationSpec) { value, _ ->
                        alpha2 = value
                    }

                    delay(delayBetweenTexts.toLong())

                    // Animate third text
                    animate(0f, 1f, animationSpec = animationSpec) { value, _ ->
                        alpha3 = value
                    }
                } else {
                    // Reset when hiding
                    alpha1 = 0f
                    alpha2 = 0f
                    alpha3 = 0f
                }
            }

            // Create animated rotation value
            val rotation by animateFloatAsState(
                targetValue = if (shouldRotate) 45f else 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "rotation"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dgenBlack)
                    .clickable { showOverlay.value = false },
                contentAlignment = Alignment.BottomStart
            ) {
                // Your overlay content here
                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                        //.align(Alignment.Center)
                        // Prevent clicks on the content from closing the overlay
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Do nothing to prevent propagation */ },
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Text(
                            text = "Video",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 40.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            ),
                            modifier = Modifier.alpha(alpha3)
                        )
                        Text(
                            text = "Image",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 40.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            ),
                            modifier = Modifier.alpha(alpha2)
                        )
                        Text(
                            text = "Send",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 40.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            ),
                            modifier = Modifier.alpha(alpha1)
                        )
                    }



                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                    ) {
                        IconButton(
                            onClick = {
                                showOverlay.value = false
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                Color.Transparent,
                                dgenTurqoise
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {

                            Icon(
                                modifier = Modifier.size(36.dp).graphicsLayer{
                                    rotationZ = rotation
                                },
                                imageVector = Icons.Outlined.Add,
                                tint = dgenTurqoise,
                                contentDescription = "collapse"
                            )
                        }
                    }
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
    recipientEntities: List<RecipientEntity>,
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
        /*
        recipients[0].contact?.ethAddress?.let {
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
         */

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
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
private fun PreviewChatScreen() {



    val messageUiState = MessageUiState.Success(generateTestMessages())
    val recipientUiState = RecipientUiState.Success(
        listOf(
            Recipient(
                id = "userB",
                address = "0xDeF456HodlGuyWallet",
                ens = "hodl.eth",
                contact = Contact("lk2", "Bob", null, "0x456")
            )
        )
    )
    ChatScreen(
        messageUiState = messageUiState,
        recipientUiState = recipientUiState,
        navigateBackToConversations = { },
        tokenBalance = 2.456,
        onSendEthClicked = { it -> },
    )
    /*
    ChatScreen(
        messagesUiState = messageUiState,
        recipients = listOf(
            Recipient(
                id = "userB",
                address = "0xDeF456HodlGuyWallet",
                ens = "hodl.eth",
                contact = Contact("lk2", "Bob", null, "0x456")
            )
        ),
        navigateBackToConversations={},
        onPhoneClicked = {},
        onSendEthClicked = {},
        onOpenContact = {},
        onContactSelected = {},
        onToggleAttachment = {},
        onSendMessageClicked = {},
        onDeleteMessage = {},
        onFocusedMessageUpdate = {},
        onPrepareVideo = {},
        onRemoveSelectedMessage = {},
        onAddSelectedMessage = {},
        videoPlayer = null
    )
     */


}