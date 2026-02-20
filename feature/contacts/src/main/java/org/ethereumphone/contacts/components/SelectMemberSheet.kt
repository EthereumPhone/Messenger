package org.ethereumphone.contacts.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Clear
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.components.ActionButton
import org.ethosmobile.contacts.ui.components.DgenCursorSearchTextfield
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenRed
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.contacts.QueryResultUiState
import org.ethereumphone.contacts.R

private const val mediumEnterDuration = 300
private const val mediumExitDuration = 150

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectMembersSheet(
    queryResultUiState: QueryResultUiState,
    onSearchQueryChanged: (String) -> Unit,
    onContactsSelected: (List<ContactEntity>) -> Unit,
    onBackClick: () -> Unit,
    primaryColor: Color = dgenTurqoise,
    secondaryColor: Color = dgenOcean
) {
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

    // Search focus state
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val buttonAlpha by animateFloatAsState(
        if (selectedItems.size > 0) 1f else 0f,
        tween(300)
    )
    var showFinalGroupSheet by remember { mutableStateOf(false) }

    // Animated background color for search field
    val animatedColor by androidx.compose.animation.animateColorAsState(
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

        AnimatedContent(
            showFinalGroupSheet,
            transitionSpec = {
                fadeIn(animationSpec = tween(150, 150)) togetherWith
                        fadeOut(animationSpec = tween(150))
            }
        ) { open ->
            if (!open){
                DgenHeaderBackground(
//                    headerContent = {
//                        Text(
//                            text = "SELECT MEMBERS",
//                            style = TextStyle(
//                                fontFamily = PitagonsSans,
//                                color = primaryColor,
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 20.sp,
//                                lineHeight = 20.sp,
//                                letterSpacing = 0.sp,
//                                textDecoration = TextDecoration.None
//                            ),
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis,
//                            modifier = Modifier,
//                        )
//
//                        Surface(
//                            color = primaryColor,
//                            shape = CircleShape,
//                            modifier = Modifier
//                                .height(25.dp)
//                                .alpha(buttonAlpha)
//                                .pointerInput(Unit) {
//                                    detectTapGestures {
//                                        if (selectedItems.size > 1) {
//                                            // Need at least 2 members for a group
//                                            onContactsSelected(selectedItems.toList())
//                                        }
//                                    }
//                                }
//                        ){
//                            Row(
//                                modifier = Modifier.padding(horizontal = 8.dp),
//                                horizontalArrangement = Arrangement.Center
//                            ) {
//                                Text(
//                                    text= "NEXT (${selectedItems.size})",
//                                    color = secondaryColor,
//                                    style = TextStyle(
//                                        fontFamily = SpaceMono,
//                                        color = dgenWhite,
//                                        fontWeight = FontWeight.Normal,
//                                        fontSize = 16.sp,
//                                        lineHeight = 16.sp,
//                                        letterSpacing = 0.sp,
//                                        textDecoration = TextDecoration.None
//                                    ),
//                                    modifier = Modifier.padding(horizontal = 8.dp)
//                                )
//                            }
//                        }
//                    },
                    title = "SELECT MEMBERS",
                    primaryColor = primaryColor,
                    onBackClick = onBackClick
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 24.dp,bottom = 16.dp) // fab size 64.dp
                    )
                    {
                        // Search Row with clear button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 12.dp,end = 12.dp)
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
                                        val processedText = newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")
                                        val newProcessedTextFieldValue = newTextFieldValue.copy(text = processedText)
                                        textState = newProcessedTextFieldValue
                                        onSearchQueryChanged(newProcessedTextFieldValue.text)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
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
                                            text = "Search name, ENS or address".uppercase(),
                                            style = TextStyle(
                                                fontFamily = SpaceMono,
                                                color = primaryColor.copy(alpha = 0.45f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 18.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
                                    modifier = Modifier,
                                    visible = isSearchFocused && textState.text.isNotEmpty(),
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
                                            onClick = {
                                                textState = TextFieldValue("")
                                                onSearchQueryChanged("")
                                            },
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

                        // Navigate to review sheet when members are selected
                        if (selectedItems.isNotEmpty()) {
                            Surface(
                                color = primaryColor,
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clickable { showFinalGroupSheet = true }
                            ) {
                                Text(
                                    text = "REVIEW SELECTED (${selectedItems.size})",
                                    color = secondaryColor,
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Results list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp,end = 12.dp)
                                ,
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            when(queryResultUiState) {
                                is QueryResultUiState.Loading -> {}
                                is QueryResultUiState.Success -> {
                                    // Show manual entry option (ENS/address) when text is entered
                                    if (textState.text.isNotEmpty()) {
                                        queryResultUiState.manualContactEntity?.let { manualEntry ->
                                            val isManualSelected = selectedItems.any {
                                                it.lookupKey == manualEntry.lookupKey ||
                                                        it.ethAddress == manualEntry.ethAddress
                                            }

                                            item {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            if (isManualSelected) {
                                                                selectedItems.removeAll {
                                                                    it.lookupKey == manualEntry.lookupKey ||
                                                                            it.ethAddress == manualEntry.ethAddress
                                                                }
                                                            } else {
                                                                selectedItems.add(manualEntry)
                                                            }
                                                        }
                                                        .padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Add \"${manualEntry.lookupKey}\"",
                                                            overflow = TextOverflow.Ellipsis,
                                                            style = TextStyle(
                                                                fontFamily = PitagonsSans,
                                                                color = primaryColor,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 18.sp,
                                                                lineHeight = 20.sp,
                                                                textDecoration = TextDecoration.None
                                                            ),
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = if (manualEntry.lookupKey.contains(".eth")) "ENS name" else "Ethereum address",
                                                            style = TextStyle(
                                                                fontFamily = SpaceMono,
                                                                color = primaryColor.copy(alpha = 0.6f),
                                                                fontWeight = FontWeight.Normal,
                                                                fontSize = 12.sp
                                                            )
                                                        )
                                                    }

                                                    // Checkbox indicator
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(
                                                                if (isManualSelected) primaryColor else Color.Transparent,
                                                                CircleShape
                                                            )
                                                            .then(
                                                                if (!isManualSelected) {
                                                                    Modifier.drawBehind {
                                                                        drawCircle(
                                                                            color = primaryColor,
                                                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                                                        )
                                                                    }
                                                                } else Modifier
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isManualSelected) {
                                                            Text(
                                                                text = "✓",
                                                                color = secondaryColor,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            item {
                                                Spacer(Modifier.height(8.dp))
                                            }
                                        }
                                    }

                                    // Filter to only show contacts with valid eth addresses
                                    val contactsWithEthAddress = queryResultUiState.contactEntities.filter {
                                        !it.ethAddress.isNullOrBlank()
                                    }

                                    items(contactsWithEthAddress) { contact ->
                                        SelectableMemberItem(
                                            isSelected = selectedItems.contains(contact),
                                            header = contact.name,
                                            subheader = contact.ethAddress?.let { addr ->
                                                when {
                                                    addr.endsWith(".eth") -> addr
                                                    addr.length > 10 -> addr.take(6) + "..." + addr.takeLast(6)
                                                    else -> addr
                                                }
                                            } ?: "",
                                            onCheckClick = {
                                                if (contact in selectedItems) selectedItems.remove(contact) else selectedItems.add(contact)
                                            },
                                            primaryColor = primaryColor,
                                            secondaryColor = secondaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                DgenHeaderBackground(
                    title = "SELECTED MEMBERS",
                    primaryColor = primaryColor,
                    onBackClick = { showFinalGroupSheet = false }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 56.dp, bottom = 16.dp)
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(selectedItems.toList()) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name.ifBlank { item.ethAddress ?: item.lookupKey },
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1,
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
                                        item.ethAddress?.let { addr ->
                                            val displayAddr = when {
                                                addr.endsWith(".eth") -> addr
                                                addr.length > 10 -> addr.take(6) + "..." + addr.takeLast(6)
                                                else -> addr
                                            }
                                            Text(
                                                text = displayAddr,
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 1,
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = primaryColor.copy(0.45f),
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
                                    IconButton(onClick = { selectedItems.remove(item) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Remove",
                                            tint = dgenRed,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            color = primaryColor,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(Alignment.TopCenter)
                                .clickable { onContactsSelected(selectedItems.toList()) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "DEBUG: NEXT STEP",
                                    color = secondaryColor,
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        lineHeight = 16.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
