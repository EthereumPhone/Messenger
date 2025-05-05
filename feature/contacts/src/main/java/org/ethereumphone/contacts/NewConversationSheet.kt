package org.ethereumphone.contacts

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import kotlinx.coroutines.flow.collectLatest
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.chat.components.OldSchoolThickCursorTextField
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.contacts.components.CheckBox
import org.ethereumphone.contacts.components.CreateGroupSheet
import org.ethereumphone.contacts.components.NewConversationHeader
import org.ethereumphone.contacts.components.SelectMembersSheet


@Composable
fun NewConversationSheet(
    onDismiss: () -> Unit,
    onConversationCreated: (String) -> Unit,
    viewModel: ContactViewModel = hiltViewModel()
) {
    val queryResultUiState by viewModel.queryResultUiState.collectAsStateWithLifecycle()
    ConversationSheet(
        queryResultUiState = queryResultUiState,
        onContactsSelected = {
            viewModel.getOrCreateConversation(it)
            // navigate to chat if conversation exists
        },
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDismiss = onDismiss
    )

    // handles navigation and displaying of error
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.NavigateToConversation -> {
                    onConversationCreated(event.id)
                }

                is UiEvent.ShowError -> {
                    // You can show a snackbar here if needed
                    // scaffoldState.snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConversationSheet(
    queryResultUiState: QueryResultUiState,
    onContactsSelected: (List<ContactEntity>) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var multiSelectMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ContactEntity>() }

    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, bottom = 24.dp) // fab size 64.dp
                ) {
                    NewConversationHeader(
                        title = "NEW CONVERSATION",
                        onBackClick = onDismiss
                    )


                    Row(
                        Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ){
                            AnimatedVisibility(
                                textState.text.isBlank(),
                                enter = fadeIn(tween(300)) + expandHorizontally(tween(300)),
                                exit = fadeOut(tween(300)) + shrinkHorizontally(tween(300)),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.searchicon),
                                    contentDescription = "Searching",
                                    tint = dgenTurqoise.copy(alpha = 0.45f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            OldSchoolThickCursorTextField(
                                singleLine = true,
                                maxFieldHeight = 50.dp,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                value = textState,
                                onValueChange = { newTextFieldValue ->
                                    // Process the text to remove spaces after periods
                                    val processedText = newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")

                                    // Create a new TextFieldValue with the processed text and updated selection
                                    val newProcessedTextFieldValue = newTextFieldValue.copy(text = processedText)
                                    textState = newProcessedTextFieldValue
                                    onSearchQueryChanged(newProcessedTextFieldValue.text)
                                },
                                cursorHeight = 48f,
                                placeholder = {
                                        Text(
                                            text = "Search name or phonenumber".uppercase(),
                                            style = TextStyle(
                                                fontFamily = SpaceMono,
                                                color = dgenTurqoise.copy(alpha = 0.45f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 20.sp
                                            )
                                        )
                                },
                                textStyle = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = dgenWhite,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ),
                                cursorColor = dgenWhite,
                            )

                    }



                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when(queryResultUiState) {
                            is QueryResultUiState.Loading -> {}
                            is QueryResultUiState.Success -> {

                                stickyHeader {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(dgenBlack)
                                            .padding(vertical = 8.dp)
                                    ) {

                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .pointerInput(Unit) {
                                                    detectTapGestures {
                                                        multiSelectMode = true
                                                    }
                                                },
                                            text = "MAKE NEW GROUP",
                                            style = TextStyle(
                                                fontFamily = PitagonsSans,
                                                color = dgenTurqoise,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 20.sp
                                            )
                                        )

                                    }
                                }

                                if(textState.text.isNotEmpty()){
                                    queryResultUiState.manualContactEntity?.let {
                                        item {

                                            Text(
                                                modifier = Modifier.fillMaxWidth(),
                                                text = "write to ${it.lookupKey}",
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenTurqoise,
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



                                if (queryResultUiState.contactEntities.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier
                                            .fillParentMaxHeight(0.5f)
                                            .fillParentMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No contacts available".uppercase(),
                                                fontSize = 24.sp,
                                                style = TextStyle(
                                                    fontFamily = SpaceMono,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 24.sp
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    items(queryResultUiState.contactEntities) { contact ->
                                        // add onCLick behaviour
                                        Column(modifier = Modifier
                                            .clickable { onContactsSelected(listOf(contact)) }
                                        ) {
                                            Text(
                                                text = contact.name,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 22.sp,
                                                    lineHeight = 22.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None
                                                )
                                            )
                                            if(contact.numbers.firstOrNull()?.address != null){
                                                Text(
                                                    text = contact.numbers.firstOrNull()?.address!!,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = TextStyle(
                                                        fontFamily = PitagonsSans,
                                                        color = dgenTurqoise.copy(0.45f),
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 16.sp,
                                                        lineHeight = 16.sp,
                                                        letterSpacing = 0.sp,
                                                        textDecoration = TextDecoration.None
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
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
            else {
                SelectMembersSheet(
                    queryResultUiState = queryResultUiState,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onBackClick = { multiSelectMode = false },
                    onContactsSelected = onContactsSelected
                )
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
        queryResultUiState,
        {},
        {},
        {}
    )
}


@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun previewNoContactsContactSheet() {

    val contactEntities = emptyList<ContactEntity>()

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    ConversationSheet(
        queryResultUiState,
        {},
        {},
        {},
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