package org.ethereumhpone.contracts

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.ethereumhpone.contracts.ui.ChatListItem
import org.ethereumhpone.contracts.ui.ContactSheet
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.theme.Colors

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Conversation
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Date
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.ethereumhpone.contracts.ui.ChatListInfo
import org.ethereumhpone.database.model.Message


@Composable
fun ContactRoute(
    modifier: Modifier = Modifier,
    navigateToChat: (String) -> Unit,
    viewModel: ContactViewModel = hiltViewModel()
){
    val context = LocalContext.current

    val conversationState by viewModel.conversationState.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList())



    ContactScreen(
        contacts = contacts,
        conversationState = conversationState,
        navigateToChat = navigateToChat,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ContactScreen(
    contacts: List<Contact>,
    conversationState: ConversationUIState,
    navigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier
){

    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()


    //ModalSheets
    var showContactSheet by remember { mutableStateOf(false) }
    val modalContactSheetState = rememberModalBottomSheetState(true)

    var showCameraWithPerm by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val contactsPermissionsToRequest = listOf(
        Manifest.permission.READ_CONTACTS
    )

    val contactsPermissionState = rememberMultiplePermissionsState(permissions = contactsPermissionsToRequest)




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
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Loading",
                            fontSize = 12.sp,
                            fontFamily = Fonts.INTER,
                            color = Colors.WHITE,
                        )
                    }
                }
                is ConversationUIState.Empty ->{
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No conversations",
                            fontSize = 12.sp,
                            fontFamily = Fonts.INTER,
                            color = Colors.WHITE,
                        )
                    }
                }
                is ConversationUIState.Success -> {

                   if(conversationState.conversations.isNotEmpty()){
                       Box(modifier = Modifier.weight(1f)) {
                           LazyColumn(
                               modifier = Modifier.padding(horizontal = 12.dp)
                           ){
                               conversationState.conversations.sortedBy { it.date }.reversed().forEach { conversation ->
                                   item {
                                       ChatListInfo(
                                           header = conversation.recipients.get(0).getDisplayName(),
                                           subheader = conversation.lastMessage?.getText() ?: "",
                                           ens = "",
                                           time = convertLongToTime(conversation.lastMessage?.date ?: 0L),
                                           unreadConversation = conversation.unread,
                                           onClick = { navigateToChat(conversation.id.toString()) }
                                       )
                                   }

                               }

                           }
                       }
                   }else{
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

                else -> {}
            }

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
//                coroutineScope.launch {
//                    getContacts(context)
//                }
                val allowedContacts = contacts//.filter { it.name.isNotEmpty() }

                ContactSheet(allowedContacts)
            }


        }
    }
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return format.format(date)
}

fun currentTimeToLong(): Long {
    return System.currentTimeMillis()
}

fun convertDateToLong(date: String): Long {
    val df = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return df.parse(date).time
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
        {}
    )
}