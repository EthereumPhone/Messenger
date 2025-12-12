package org.ethereumphone.contacts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.example.dgenlibrary.ui.theme.mediumExitDuration
import org.ethereumphone.dgenlibrary.components.ActionButton
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethosmobile.contacts.ui.components.DgenCursorSearchTextfield
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.dgenlibrary.R

@Composable
fun AddGroupMembersSheet(
    onDismiss: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onSelectionChanged: (List<String>) -> Unit,
    viewModel: ContactViewModel = hiltViewModel(),
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val queryResultUiState by viewModel.queryResultUiState.collectAsStateWithLifecycle()

    AddGroupMembersSheetContent(
        searchQuery = searchQuery,
        queryResultUiState = queryResultUiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDismiss = onDismiss,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        onSelectionChanged = onSelectionChanged,
    )
}

@Composable
internal fun AddGroupMembersSheetContent(
    searchQuery: String,
    queryResultUiState: QueryResultUiState,
    onSearchQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onSelectionChanged: (List<String>) -> Unit,
) {
    // Search bar state
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Keep local TextFieldValue (same approach as NewConversationSheet)
    var textState by remember { mutableStateOf(TextFieldValue(searchQuery)) }
    LaunchedEffect(searchQuery) {
        if (textState.text != searchQuery) {
            textState = TextFieldValue(searchQuery)
        }
    }

    val selectedEthAddresses = remember { mutableStateListOf<String>() }
    LaunchedEffect(selectedEthAddresses.size) {
        onSelectionChanged(selectedEthAddresses.toList())
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isSearchFocused) secondaryColor else Color.Transparent,
        animationSpec = tween(durationMillis = mediumEnterDuration),
        label = "color"
    )

    LaunchedEffect(isSearchFocused) {
        if (isSearchFocused) {
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            SecondaryScreenHeader(
                title = "ADD MEMBERS".uppercase(),
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
                                painter = painterResource(R.drawable.searchicon),
                                contentDescription = "Search",
                                tint = primaryColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures { isSearchFocused = true }
                                    }
                            )
                        }

                        DgenCursorSearchTextfield(
                            value = textState,
                            onValueChange = { newTextFieldValue ->
                                val processedText =
                                    newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")
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
                            onFocusChanged = { focused -> isSearchFocused = focused },
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

                        AnimatedVisibility(
                            visible = isSearchFocused,
                            enter = fadeIn(
                                animationSpec = tween(mediumEnterDuration)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(mediumExitDuration)
                            )
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
                                        .drawBehind { drawCircle(color = primaryColor) },
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
            }
            
            Spacer(Modifier.fillMaxWidth().height(24.dp))
            Text(
                text = buildAnnotatedString {
                    append("MEMBERS ")
                    withStyle(
                        style = SpanStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None)
                        ) {
                            append("${selectedEthAddresses.size}")
                        }
                },
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (queryResultUiState) {
                        is QueryResultUiState.Loading -> {
                            // no-op (matches existing sheet behavior)
                        }

                        is QueryResultUiState.Success -> {
                            // Show all contacts with ethAddress (ViewModel already filters, but keep it safe)
                            val contactsWithEthAddress = queryResultUiState.contactEntities
                                .filter { !it.ethAddress.isNullOrBlank() }

                            items(contactsWithEthAddress) { contact ->
                                val ethAddr = contact.ethAddress?.trim().orEmpty()
                                val checked = ethAddr.isNotBlank() && selectedEthAddresses.contains(ethAddr)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = ethAddr.isNotBlank()) {
                                            if (checked) selectedEthAddresses.remove(ethAddr)
                                            else selectedEthAddresses.add(ethAddr)
                                        }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
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
                                                    color = dgenWhite,
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

                                    SquareCheckBox(
                                        checked = checked,
                                        primaryColor = primaryColor,
                                        onCheckedChange = { newChecked ->
                                            if (ethAddr.isBlank()) return@SquareCheckBox
                                            if (newChecked) {
                                                if (!selectedEthAddresses.contains(ethAddr)) selectedEthAddresses.add(ethAddr)
                                            } else {
                                                selectedEthAddresses.remove(ethAddr)
                                            }
                                        }
                                    )
                                }
                            }

                            item {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(dgenBlack, Color.Transparent)
                            )
                        )
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, dgenBlack)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun SquareCheckBox(
    checked: Boolean,
    primaryColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(0.dp)
    Box(
        modifier = modifier
            .size(24.dp)
            .border(
                width = 2.dp,
                color = primaryColor,
                shape = shape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = modifier
                    .size(16.dp)
                    .background(
                        color = if (checked) primaryColor else Color.Transparent,
                        shape = shape
                    )
            )
        }
    }
}

@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "AddGroupMembersSheet")
@Composable
private fun PreviewAddGroupMembersSheet() {
    val contacts = listOf(
        ContactEntity(name = "Alice", lookupKey = "alice", ethAddress = "alice.eth"),
        ContactEntity(name = "Bob", lookupKey = "bob", ethAddress = "0x1234567890abcdef1234567890abcdef12345678"),
        ContactEntity(name = "Charlie", lookupKey = "charlie", ethAddress = "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd"),
        ContactEntity(name = "Dgen Friend With A Long Name", lookupKey = "dgen", ethAddress = "dgenfriend.base.eth"),
    )

    val uiState = QueryResultUiState.Success(
        manualContactEntity = null,
        contactEntities = contacts
    )

    // local selection state just for preview
    var selected by remember { mutableStateOf<List<String>>(emptyList()) }

    AddGroupMembersSheetContent(
        searchQuery = "",
        queryResultUiState = uiState,
        onSearchQueryChanged = {},
        onDismiss = {},
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF0B2A33),
        onSelectionChanged = { selected = it },
    )
}

