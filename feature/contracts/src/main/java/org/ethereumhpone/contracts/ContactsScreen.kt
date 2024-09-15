package org.ethereumhpone.contracts

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.ethereumhpone.contracts.ui.ContactSheet
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.theme.Colors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Conversation
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Date
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.ethereumhpone.chat.components.trimEthereumAddress
import org.ethereumhpone.contracts.ui.ChatListInfo
import org.ethereumhpone.contracts.ui.ChatListItem
import org.ethereumhpone.database.model.Message


@Composable
fun ContactRoute(
    modifier: Modifier = Modifier,
    navigateToChat: (String, List<String>) -> Unit,
    viewModel: ContactViewModel = hiltViewModel()
) {
    val conversationState by viewModel.conversationState.collectAsStateWithLifecycle()
    val showHiddenButton by viewModel.showHiddenButton.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList())

    ContactScreen(
        modifier = modifier,
        contacts = contacts,
        conversationState = conversationState,
        showHiddenButton = showHiddenButton,
        contactsClicked = { selectedContacts ->
            navigateToChat("0", selectedContacts.map { it.getDefaultNumber()?.address ?: it.numbers[0].address }) },
        markAccepted = { id, address -> viewModel.setConversationAsAccepted(id, address) },
        conversationClicked = { id ->
            viewModel.setConversationAsRead(id.toLong())
            navigateToChat(id, emptyList())
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ContactScreen(
    contacts: List<Contact>,
    conversationState: ConversationUIState,
    contactsClicked: (List<Contact>) -> Unit,
    conversationClicked: (String) -> Unit,
    showHiddenButton: Boolean = false,
    markAccepted: (Long, String) -> Unit,
    modifier: Modifier = Modifier
){

    val context = LocalContext.current

    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()


    //ModalSheets
    var showContactSheet by remember { mutableStateOf(false) }
    val modalContactSheetState = rememberModalBottomSheetState(true)

    val contactsPermissionsToRequest = listOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_SMS
    )

    val contactsPermissionState = rememberMultiplePermissionsState(permissions = contactsPermissionsToRequest)

    var showHiddenConversations by remember { mutableStateOf(false) }

    Scaffold (
        containerColor = Color.Black,
        topBar = {
            ethOSHeader(
                title=  "Messaging",
                titleSize =  24.sp,
                isTrailContent = true,
                trailContent = {

                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(42.dp)
                        ,
                        enabled = true,
                        onClick = {
                            showContactSheet = true
                        },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ){
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add contact", tint = Colors.WHITE, modifier = Modifier.size(32.dp))
                        }

                    }
                },
                isBeginContent = true,
                beginContent = {
                    Box(
                        contentAlignment = Alignment.Center
                    ){
                        // Nothing
                    }
                }
            )
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ){ paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)) {

            when(conversationState){
                is ConversationUIState.Loading ->{
                    Box(contentAlignment = Alignment.Center,modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Loading",
                            fontSize = 14.sp,
                            fontFamily = Fonts.INTER,
                            fontWeight = FontWeight.SemiBold,
                            color = Colors.WHITE,
                        )
                    }
                }
                is ConversationUIState.Empty ->{
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No conversations",
                            fontSize = 14.sp,
                            fontFamily = Fonts.INTER,
                            fontWeight = FontWeight.SemiBold,
                            color = Colors.WHITE,
                        )
                    }
                }
                is ConversationUIState.Success -> {

                    val coroutineScope = rememberCoroutineScope()
                    val tabs = listOf("Inbox","Unaccepted")
                    // Display 10 items
                    val pagerState = rememberPagerState(pageCount = {
                        tabs.size
                    })

                    TabRow(
                        containerColor = Colors.TRANSPARENT,
                        contentColor = Colors.WHITE,
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp,end = 12.dp),
                        selectedTabIndex = pagerState.currentPage,
                        divider = { Divider(color = Colors.TRANSPARENT) },
                        indicator = { tabPositions ->
                            if (pagerState.currentPage < tabPositions.size) {
                                TabRowDefaults.Indicator(
                                    color = Colors.WHITE,
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                )
                            }
                        }
                    ){
                        tabs.forEachIndexed { index, s ->
                            Tab(
                                selectedContentColor = Colors.WHITE,
                                unselectedContentColor = Colors.GRAY,
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    //tabIndex = index
                                    coroutineScope.launch {
                                        // Call scroll to on pagerState
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = s,
                                        color = if(pagerState.currentPage == index) Colors.WHITE else Colors.GRAY,
                                        fontSize = 14.sp,
                                        fontFamily = Fonts.INTER,
                                        fontWeight = FontWeight.SemiBold,

                                        )
                                },

                                )
                        }
                    }
                    HorizontalPager(state = pagerState) { page ->

                        when (page) {
                            //TODO: Add logic
                            0 -> {
                                if(conversationState.conversations.isNotEmpty()){
                                    Box(modifier = Modifier.weight(1f)) {
                                        LazyColumn(
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        ){
                                            conversationState.conversations.filter { !it.isUnknown }.filter { it.date > 0 }.sortedBy { it.date }.reversed().forEach { conversation ->
                                                item {

                                                    val dates = conversation.lastMessage?.date?.let { Date(it) }
                                                    ChatListItem(
                                                        image = {
                                                            if (conversation.recipients.get(0).contact?.photoUri != null) {
                                                                Image(
                                                                    painter = rememberAsyncImagePainter(model = conversation.recipients.get(0).contact?.photoUri), // Replace 'contact.image' with the correct URI variable from your 'Contact' object
                                                                    contentDescription = "Contact Image",
                                                                    contentScale = ContentScale.Crop,
                                                                    modifier = Modifier
                                                                        .size(62.dp) // Set the size of the image
                                                                        .clip(CircleShape) // Apply a circular shape
                                                                )
                                                            } else {
                                                                Image(
                                                                    painter = painterResource(id = R.drawable.nouns),
                                                                    contentDescription = "Contact Image",
                                                                    modifier = Modifier
                                                                        .size(62.dp) // Set the size of the image
                                                                        .clip(CircleShape) // Apply a circular shape
                                                                )
                                                            }
                                                        },
                                                        header = conversation.recipients.get(0).getDisplayName(),
                                                        subheader = conversation.lastMessage?.getSummary() ?: "",
                                                        time = dates, //conversation.lastMessage?.date, // convertLongToTime(conversation.lastMessage?.date ?: 0L),
                                                        unreadConversation = conversation.unread,
                                                        onClick = {
                                                            conversationClicked(conversation.id.toString())
                                                        },
                                                        onClickLeft = {
                                                            //TODO: Add logic (archive)
                                                            Toast.makeText(context, "Left", Toast. LENGTH_SHORT).show()
                                                        },
                                                        onClickRight = {
                                                            //TODO: Add logic (delete)
                                                            Toast.makeText(context, "Right", Toast. LENGTH_SHORT).show()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                else{
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No conversations",
                                            fontSize = 20.sp,
                                            fontFamily = Fonts.INTER,
                                            fontWeight = FontWeight.Medium,
                                            color = Colors.GRAY,
                                        )
                                    }
                                }
                            }
                            1 -> {
                                //TODO: Add logic (unaccepted messages)
                                if(conversationState.conversations.isNotEmpty()){
                                    Box(modifier = Modifier.weight(1f)) {
                                        LazyColumn(
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        ){
                                            conversationState.conversations.filter { it.isUnknown }.filter { it.date > 0 }.sortedBy { it.date }.reversed().forEach { conversation ->
                                                item {

                                                    val dates = conversation.lastMessage?.date?.let { Date(it) }
                                                    ChatListItem(
                                                        image = {
                                                            if (conversation.recipients.get(0).contact?.photoUri != null) {
                                                                Image(
                                                                    painter = rememberAsyncImagePainter(model = conversation.recipients.get(0).contact?.photoUri), // Replace 'contact.image' with the correct URI variable from your 'Contact' object
                                                                    contentDescription = "Contact Image",
                                                                    contentScale = ContentScale.Crop,
                                                                    modifier = Modifier
                                                                        .size(62.dp) // Set the size of the image
                                                                        .clip(CircleShape) // Apply a circular shape
                                                                )
                                                            } else {
                                                                Image(
                                                                    painter = painterResource(id = R.drawable.nouns),
                                                                    contentDescription = "Contact Image",
                                                                    modifier = Modifier
                                                                        .size(62.dp) // Set the size of the image
                                                                        .clip(CircleShape) // Apply a circular shape
                                                                )
                                                            }
                                                        },
                                                        header = conversation.recipients.get(0).getDisplayName(),
                                                        subheader = conversation.lastMessage?.getSummary() ?: "",
                                                        time = dates, //conversation.lastMessage?.date, // convertLongToTime(conversation.lastMessage?.date ?: 0L),
                                                        unreadConversation = conversation.unread,
                                                        onClick = {
                                                            conversationClicked(conversation.id.toString())
                                                        },
                                                        onClickLeft = {
                                                            //TODO: Add logic (archive)
                                                            Toast.makeText(context, "Left", Toast. LENGTH_SHORT).show()
                                                        },
                                                        onClickRight = {
                                                            //TODO: Add logic (delete)
                                                            Toast.makeText(context, "Right", Toast. LENGTH_SHORT).show()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                else{
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No conversations",
                                            fontSize = 20.sp,
                                            fontFamily = Fonts.INTER,
                                            fontWeight = FontWeight.Medium,
                                            color = Colors.GRAY,
                                        )
                                    }
                                }
                            }
                        }
                    }



                }
            }

        }

    }

    if(showHiddenConversations){
        // Popup that lists conversations names that have unknown set to true
        if (conversationState is ConversationUIState.Success) {
            val allConvos = conversationState.conversations
            ShowHiddenConversationsPopup(
                hiddenConversations = allConvos.filter { it.isUnknown },
                onApprove = { id, address ->
                    showHiddenConversations = false
                    markAccepted(id, address)
                },
                onDismiss = { showHiddenConversations = false }
            )
        }
    }

    if(showContactSheet){
        ModalBottomSheet(
            containerColor= Colors.BLACK,
            contentColor= Colors.WHITE,

            onDismissRequest = {
                scope.launch {
                    modalContactSheetState.hide()
                }.invokeOnCompletion {
                    if(!modalContactSheetState.isVisible) showContactSheet = false
                }
            },
            sheetState = modalContactSheetState
        ) {

            LaunchedEffect(key1 = contactsPermissionState.allPermissionsGranted) {
                if (!contactsPermissionState.allPermissionsGranted) {
                    contactsPermissionState.launchMultiplePermissionRequest()
                }
            }

            if (contactsPermissionState.allPermissionsGranted) {
                ContactSheet(
                    contacts = contacts,
                    onContactsSelected = {
                        showContactSheet = false
                        contactsClicked(it)
                    }
                )
            }
        }
    }
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return format.format(date)
}

@Composable
fun ShowHiddenConversationsPopup(
    hiddenConversations: List<Conversation>,
    onApprove: (Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Colors.BLACK
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hidden Conversations",
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontFamily = Fonts.INTER,
                    color = Colors.WHITE,
                )

                LazyColumn {
                    items(hiddenConversations) { conversation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = trimEthereumAddress(conversation.getConversationTitle()) + ": " + conversation.lastMessage?.body, // Assuming Conversation has a 'name' property
                                modifier = Modifier.weight(1f),
                                fontFamily = Fonts.INTER,
                                color = Colors.WHITE,
                            )
                            // Icons.Default.Check
                            androidx.compose.material.IconButton(
                                onClick = {
                                    onApprove(conversation.id, conversation.getConversationTitle())
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Approve hidden conversation",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
@Preview
fun PreviewShowHiddenConversationsPopup(){
    ShowHiddenConversationsPopup(
        listOf(
            Conversation(
                id= 0,
                recipients = emptyList(),
                lastMessage =  Message(
                    body = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor"
                ),
                title = "Mark Katakowski"
            )
        ),
        {_,_ ->},
        {}
    )
}

@Composable
@Preview
fun PreviewContactScreen(){
    ContactScreen(
        emptyList(),
        ConversationUIState.Success(
            conversations = listOf(
                Conversation(
                    id= 0,
                    recipients = emptyList(),
                    lastMessage =  Message(
                        body = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor"
                    ),
                    title = "Mark Katakowski"
                )
            )
        ),
        {},
        {},
        markAccepted = {_,_ ->}
    )
}