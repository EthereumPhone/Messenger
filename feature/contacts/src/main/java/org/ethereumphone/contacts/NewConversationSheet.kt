package org.ethereumphone.contacts

import android.os.Build.VERSION.SDK_INT
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.pulseOpacity
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumphone.contacts.components.CreateGroupSheet
import org.ethereumphone.contacts.components.SelectMembersSheet
import org.ethereumhpone.database.model.ContactEntity
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.dgenlibrary.DgenSearchRow
import com.example.dgenlibrary.InfoScreen
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.backgrounds.FadeDirection
import com.example.dgenlibrary.ui.backgrounds.FadeEdge
import com.example.dgenlibrary.ui.theme.label_fontSize
import org.ethereumphone.dgenlibrary.showDgenToast


@Composable
fun NewConversationSheet(
    onDismiss: () -> Unit,
    onConversationCreated: (String) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    viewModel: ContactViewModel = hiltViewModel()
) {

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val queryResultUiState by viewModel.queryResultUiState.collectAsStateWithLifecycle()
    var resetGroupCreation by remember { mutableStateOf(0) }

    ConversationSheet(
        searchQuery = searchQuery,
        queryResultUiState = queryResultUiState,
        onContactsSelected = viewModel::getOrCreateConversation,
        onGroupCreated = viewModel::getOrCreateConversationWithGroupInfo,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDismiss = onDismiss,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        resetGroupCreationTrigger = resetGroupCreation
    )

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.NavigateToConversation -> {
                    onConversationCreated(event.id)
                }

                is UiEvent.ShowError -> {
                    if (event.message != "NOT_REGISTERED_WITH_XMTP") {
                        showDgenToast(context, event.message)
                    }
                    resetGroupCreation++
                }
            }
        }
    }

    // Clear search query when the sheet is dismissed/disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onSearchQueryChanged("")
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConversationSheet(
    searchQuery: String,
    queryResultUiState: QueryResultUiState,
    onContactsSelected: (List<String>) -> Unit,
    onGroupCreated: (List<String>, String?, String?, String?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    resetGroupCreationTrigger: Int = 0,
) {
    val context = LocalContext.current

    var isSearchFocused by remember { mutableStateOf(false) }

    var multiSelectMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ContactEntity>() }
    
    // Group creation state
    var showGroupCreation by remember { mutableStateOf(false) }
    var groupCreationFailed by remember { mutableStateOf(false) }

    LaunchedEffect(resetGroupCreationTrigger) {
        if (resetGroupCreationTrigger > 0 && showGroupCreation) {
            groupCreationFailed = true
            delay(400)
            onDismiss()
        }
    }
    
    // Local TextFieldValue state that syncs with searchQuery
    var textState by remember { mutableStateOf(TextFieldValue(searchQuery)) }
    
    // Sync textState with searchQuery when searchQuery changes externally (e.g., when cleared)
    LaunchedEffect(searchQuery) {
        if (textState.text != searchQuery) {
            textState = TextFieldValue(searchQuery)
        }
    }

    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }
    
    // Handle back press in multi-select or group creation mode
    if (multiSelectMode || showGroupCreation) {
        BackHandler {
            if (showGroupCreation) {
                showGroupCreation = false
            } else if (multiSelectMode) {
                multiSelectMode = false
                selectedItems.clear()
            }
        }
    }




    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if ( SDK_INT >= 28 ) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        AnimatedContent(
            targetState = when {
                showGroupCreation -> 2
                multiSelectMode -> 1
                else -> 0
            },
            transitionSpec = {
                fadeIn(animationSpec = tween(150, 150)) togetherWith
                        fadeOut(animationSpec = tween(150))
            }
        ) { state ->
            when (state) {
                0 -> {
                // Normal conversation selection
                // Wrap in Box to add touch-blocking overlay when in multi-select mode
                Box(modifier = Modifier.fillMaxSize()) {
                    DgenHeaderBackground(
                        title = "NEW CONVERSATION",
                        primaryColor = primaryColor,
                        onBackClick = onDismiss,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 24.dp,bottom = 16.dp) // fab size 64.dp
                        )
                        {
                            DgenSearchRow(
                                searchValue = textState,
                                onValueChange = { newTextFieldValue ->
                                    val processedText =
                                        newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")
                                    val newProcessedTextFieldValue =
                                        newTextFieldValue.copy(text = processedText)
                                    textState = newProcessedTextFieldValue
                                    onSearchQueryChanged(newProcessedTextFieldValue.text)
                                },
                                onClearValue = { onSearchQueryChanged("") },
                                modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                placeholder = {
                                    Text(
                                        text = "Search name, ENS or inbox ID".uppercase(),
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = primaryColor.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp
                                        )
                                    )
                                },
                                onFocusChanged = { focused ->
                                    isSearchFocused = focused
                                }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp,start = 12.dp,end = 12.dp)
                                    .clickable(enabled = !multiSelectMode && !showGroupCreation) { multiSelectMode = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                Text(
                                    text = "CREATE GROUP",
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        color = primaryColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = label_fontSize,
                                        lineHeight = label_fontSize,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Create Group",
                                    tint = primaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Box(
                                Modifier.fillMaxSize()
                            )
                            {

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    when(queryResultUiState) {
                                        is QueryResultUiState.Loading -> {
                                            // Remove the loading matrix here to prevent double loading
                                            // The ContactViewModel handles the conversation creation loading
                                        }
                                        is QueryResultUiState.Success -> {

                                            if(textState.text.isNotEmpty()) {
                                                item {
                                                    Spacer(Modifier.fillMaxWidth().height(16.dp))
                                                }
                                                queryResultUiState.manualContactEntity?.let {
                                                    item {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable(enabled = !multiSelectMode && !showGroupCreation) {
                                                                    showDgenToast(context, "Write to ${it.lookupKey}")
                                                                    onContactsSelected(listOf(it.lookupKey))
                                                                }
                                                        ) {
                                                            Text(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                text = "write to ${it.lookupKey}",
                                                                overflow = TextOverflow.Ellipsis,
                                                                style = TextStyle(
                                                                    fontFamily = PitagonsSans,
                                                                    color = primaryColor,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 20.sp,
                                                                    lineHeight = 20.sp,
                                                                    letterSpacing = 0.sp,
                                                                    textDecoration = TextDecoration.None
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }


                                            // Filter to only show contacts with valid eth addresses (not null or empty)
                                            val contactsWithEthAddress = queryResultUiState.contactEntities.filter {
                                                !it.ethAddress.isNullOrBlank()
                                            }

                                            if (contactsWithEthAddress.isEmpty()) {
                                                item {
                                                    Box(modifier = Modifier.fillParentMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        InfoScreen(
                                                            imageSize = 180.dp,
                                                            primaryColor = primaryColor,
                                                            description = "No contacts with Ethereum addresses"
                                                        )
                                                    }
                                                }
                                            }
                                            else {
                                                item {
                                                    Spacer(modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(16.dp))
                                                }

                                                items(contactsWithEthAddress) { contact ->
                                                    // add onCLick behaviour - disabled when in multi-select mode to prevent accidental navigation
                                                    Column(
                                                        modifier = Modifier.clickable(enabled = !multiSelectMode && !showGroupCreation) {
                                                            contact.ethAddress?.let {
                                                                onContactsSelected(listOf(it))
                                                            }
                                                        }
                                                    ) {
                                                        Text(
                                                            text = contact.name,
                                                            overflow = TextOverflow.Ellipsis,
                                                            style = TextStyle(
                                                                fontFamily = PitagonsSans,
                                                                color = primaryColor,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 22.sp,
                                                                lineHeight = 22.sp,
                                                                letterSpacing = 0.sp,
                                                                textDecoration = TextDecoration.None
                                                            )
                                                        )
                                                        // Display eth address or ENS instead of phone number
                                                        contact.ethAddress?.let { ethAddr ->
                                                            if (ethAddr.isNotBlank()) {
                                                                Text(
                                                                    text = when {
                                                                        ethAddr.endsWith(".eth") -> ethAddr
                                                                        ethAddr.length > 10 -> ethAddr.take(6) + "..." + ethAddr.takeLast(6)
                                                                        else -> ethAddr
                                                                    },
                                                                    overflow = TextOverflow.Ellipsis,
                                                                    style = TextStyle(
                                                                        fontFamily = PitagonsSans,
                                                                        color = primaryColor.copy(pulseOpacity),
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        fontSize = 16.sp,
                                                                        lineHeight = 16.sp,
                                                                        letterSpacing = 0.sp,
                                                                        textDecoration = TextDecoration.None
                                                                    ),
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(top = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                item {
                                                    Spacer(modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                FadeEdge(FadeDirection.Top,
                                    Modifier.align(Alignment.TopCenter),
                                    size = 16.dp
                                )
                                FadeEdge(FadeDirection.Bottom,
                                    Modifier.align(Alignment.BottomCenter),
                                    size = 16.dp
                                )
                            }
                        }
                    }
                
                // CRITICAL: Touch-blocking overlay when in multi-select mode
                // This ABSOLUTELY prevents any clicks from reaching the underlying content
                // during AnimatedContent transitions where both states are visible
                if (multiSelectMode || showGroupCreation) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        // Consume all pointer events to block them
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                    )
                }
                } // Close the outer Box for state 0
                }
                1 -> {
                    // Multi-select mode for group creation
                    SelectMembersSheet(
                        queryResultUiState = queryResultUiState,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onBackClick = { 
                            multiSelectMode = false 
                            selectedItems.clear()
                        },
                        onContactsSelected = { selectedContacts ->
                            selectedItems.clear()
                            selectedItems.addAll(selectedContacts)
                            showGroupCreation = true
                        },
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor
                    )
                }
                2 -> {
                    // Group creation screen
                    CreateGroupSheet(
                        members = selectedItems,
                        onBackClick = { showGroupCreation = false },
                        onCreateGroup = { members, groupName ->
                            // Get eth addresses from the selected contacts
                            val addresses = members.mapNotNull { it.ethAddress }
                            if (addresses.isNotEmpty()) {
                                onGroupCreated(addresses, groupName, null, null)
                            }
                        },
                        isCreationFailed = groupCreationFailed,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor
                    )
                }
            }
        }
    }

    // clear list if user quits multiselect
    LaunchedEffect(multiSelectMode) {
        if(!multiSelectMode && !showGroupCreation) {
            selectedItems.clear()
        }
    }
}



@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun previewContactSheet() {

    val contactEntities = listOf(
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again")
    )

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    ConversationSheet(
        searchQuery = "",
        queryResultUiState = queryResultUiState,
        onContactsSelected = {},
        onGroupCreated = { _, _, _, _ -> },
        onSearchQueryChanged = {},
        onDismiss = {},
        primaryColor = Color.Red,
        secondaryColor = Color.Red
    )
}


@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun previewNoContactsContactSheet() {

    val contactEntities = emptyList<ContactEntity>()

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    ConversationSheet(
        searchQuery = "",
        queryResultUiState = queryResultUiState,
        onContactsSelected = {},
        onGroupCreated = { _, _, _, _ -> },
        onSearchQueryChanged = {},
        onDismiss = {},
        primaryColor = Color.Red,
        secondaryColor = Color.Red
    )
}


@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun previewGroup() {

    val contactEntities = listOf(
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
    )

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    CreateGroupSheet(
        members = contactEntities,
        onBackClick = {},
        onCreateGroup = { _, _ -> },
        primaryColor = Color.Red,
        secondaryColor = Color.Red
    )
}