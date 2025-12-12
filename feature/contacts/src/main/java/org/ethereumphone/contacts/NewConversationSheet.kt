package org.ethereumphone.contacts

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
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
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.example.dgenlibrary.ui.theme.mediumExitDuration
import com.example.dgenlibrary.ui.theme.pulseOpacity
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import kotlinx.coroutines.flow.collectLatest
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumphone.contacts.components.CreateGroupSheet
import org.ethereumphone.contacts.components.NewConversationHeader
import org.ethereumphone.contacts.components.SelectMembersSheet
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethosmobile.contacts.ui.components.DgenCursorSearchTextfield
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.dgenlibrary.components.ActionButton
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.smallDuration
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.screens.InfoScreen
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
    ConversationSheet(
        searchQuery = searchQuery,
        queryResultUiState = queryResultUiState,
        onContactsSelected = viewModel::getOrCreateConversation,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDismiss = onDismiss,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor
    )

    val context = LocalContext.current
    // handles navigation and displaying of error
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.NavigateToConversation -> {
                    onConversationCreated(event.id)
                }

                is UiEvent.ShowError -> {
                    // Show themed dgen toast
                    showDgenToast(context, event.message)
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
    onSearchQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
) {
    val context = LocalContext.current

    //variables for searchbar
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showAddMembers by remember { mutableStateOf(false) }
    val selectedGroupMembers = remember { mutableStateListOf<String>() }

    var multiSelectMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ContactEntity>() }
    
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

    // Back closes the "add members" pop up
    if (showAddMembers) {
        BackHandler { showAddMembers = false }
    }




    val animatedColor by animateColorAsState(
        targetValue = if (isSearchFocused) secondaryColor else Color.Transparent,
        animationSpec = tween(durationMillis = mediumEnterDuration),
        label = "color"
    )

    // Lambda to clear the current search value and notify the change upstream

    LaunchedEffect(isSearchFocused) {
        if (isSearchFocused) {
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
            focusManager.clearFocus()
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
        if (showAddMembers) {
            AddGroupMembersSheetContent(
                searchQuery = searchQuery,
                queryResultUiState = queryResultUiState,
                onSearchQueryChanged = onSearchQueryChanged,
                onDismiss = { showAddMembers = false },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                onSelectionChanged = { selected ->
                    selectedGroupMembers.clear()
                    selectedGroupMembers.addAll(selected)
                },
            )
        } else {
        AnimatedContent(
            multiSelectMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(150, 150)) togetherWith
                        fadeOut(animationSpec = tween(150))
            }
        ) { open ->
            if(!open){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp) // fab size 64.dp
                ) {
                    SecondaryScreenHeader(
                        title = "NEW CONVERSATION".uppercase(),
                        primaryColor = primaryColor,
                        onDismiss = onDismiss,
                    )


                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        )
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        drawRoundRect(
                                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                            color = animatedColor,
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.searchicon),
                                        contentDescription = "Search",
                                        tint = primaryColor,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .pointerInput(Unit) {
                                                detectTapGestures {
                                                    isSearchFocused = true
                                                }
                                            }
                                    )
                                }


                                DgenCursorSearchTextfield(
                                    value = textState,
                                    onValueChange = { newTextFieldValue ->
                                        // Process the text to remove spaces after periods
                                        val processedText =
                                            newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")

                                        // Create a new TextFieldValue with the processed text and updated selection
                                        val newProcessedTextFieldValue =
                                            newTextFieldValue.copy(text = processedText)
                                        textState = newProcessedTextFieldValue
                                        onSearchQueryChanged(newProcessedTextFieldValue.text)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    singleLine = true,
                                    maxFieldHeight = 50.dp,
                                    cursorColor = primaryColor,
                                    cursorWidth = 16.dp,
                                    cursorHeight = 48.dp,
                                    textfieldFocusManager = focusManager,
                                    onFocusChanged = { focused ->
                                        isSearchFocused = focused
                                    },
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
                                    textStyle = TextStyle(
                                        fontFamily = PitagonsSans,
                                        color = dgenWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 20.sp,
                                        lineHeight = 20.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    ),
                                )


                                androidx.compose.animation.AnimatedVisibility(
                                    modifier = Modifier,
                                    visible = isSearchFocused,
                                    enter = fadeIn(animationSpec = tween(mediumEnterDuration)),
                                    exit = fadeOut(animationSpec = tween(mediumExitDuration))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(end = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {

                                        ActionButton(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .drawBehind {
                                                    drawCircle(
                                                        color = primaryColor,
                                                    )
                                                },
                                            onClick = { onSearchQueryChanged("") },
                                            icon = {
                                                Icon(
                                                    contentDescription = "Clear",
                                                    imageVector = Icons.Rounded.Clear,
                                                    tint = secondaryColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            enter = fadeIn(tween(smallDuration)) + expandVertically(tween(smallDuration)),
                            exit = fadeOut(tween(smallDuration)) + shrinkVertically(tween(smallDuration)),
                            visible = textState.text.isBlank()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(end = 12.dp, start = 24.dp, top = 24.dp, bottom = 12.dp)
                                    .clickable { showAddMembers = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {

                                Text(
                                    text = "NEW GROUP",
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        color = primaryColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = body1_fontSize,
                                        lineHeight = body1_fontSize,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(onClick = { showAddMembers = true }, modifier = Modifier.size(40.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = "Create new group",
                                        tint = primaryColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }






                    Box(
                        Modifier.fillMaxSize()
                    ) {

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
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
                                                        .clickable {
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
                                                    gifEnabledLoader = gifEnabledLoader,
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
                                                .height(4.dp))
                                        }
                                        
                                        items(contactsWithEthAddress) { contact ->
                                            // add onCLick behaviour
                                            Column(
                                                modifier = Modifier.clickable {
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
                                                                color = dgenWhite, //primaryColor.copy(pulseOpacity),
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

                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .align(Alignment.TopCenter)
                            .background(Brush.verticalGradient(listOf(dgenBlack, Color.Transparent))))

                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, dgenBlack))))

                    }
                }
            }
            else {
                //TODO: Add group selection feature
//                SelectMembersSheet(
//                    queryResultUiState = queryResultUiState,
//                    onSearchQueryChanged = onSearchQueryChanged,
//                    onBackClick = { multiSelectMode = false },
//                    onContactsSelected = {  } //TODO: add logic back when groups are supported,
//                )
            }
        }
        }
    }

    // clear list if user quits multiselect
    LaunchedEffect(multiSelectMode) {
        if(!multiSelectMode) {
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
        "",
        queryResultUiState,
        {},
        {},
        {},
        Color.Red,
        Color.Red
    )
}


@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun previewNoContactsContactSheet() {

    val contactEntities = emptyList<ContactEntity>()

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    ConversationSheet(
        "",
        queryResultUiState,
        {},
        {},
        {},
        Color.Red,
        Color.Red
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
        contactEntities,
        {},
        {},
    )
}