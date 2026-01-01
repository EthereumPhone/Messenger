package org.ethereumphone.contacts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.example.dgenlibrary.ui.theme.mediumExitDuration
import com.messenger.terminalsdk.TerminalSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.contacts.QueryResultUiState
import org.ethosmobile.contacts.ui.components.DgenCursorSearchTextfield
import org.ethereumphone.contacts.components.SquareCheckBox
import org.ethereumphone.contacts.BuildConfig

@Composable
fun AddGroupMembersSheet(
    queryResultUiState: QueryResultUiState,
    onSearchQueryChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    onContactsSelected: (List<ContactEntity>) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    terminalSDK: TerminalSDK? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Search bar state
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    // Selection state
    val selectedMembers = remember { mutableStateListOf<ContactEntity>() }
    
    // Local search text state
    var searchTextState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    
    // Get contacts list from UI state
    val contactsList = when (queryResultUiState) {
        is QueryResultUiState.Success -> queryResultUiState.contactEntities
        else -> emptyList()
    }
    
    // Filter contacts with eth addresses
    val contactsWithEthAddress = remember(contactsList) {
        contactsList.filter { !it.ethAddress.isNullOrBlank() }
    }
    
    // Animated color for search bar background
    val animatedColor by animateColorAsState(
        targetValue = if (isSearchFocused) secondaryColor else Color.Transparent,
        animationSpec = tween(durationMillis = mediumEnterDuration),
        label = "searchBarColor"
    )
    
    // Clear button fade animation
    val clearButtonAlpha by animateFloatAsState(
        targetValue = if (isSearchFocused && searchTextState.text.isNotEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = mediumExitDuration),
        label = "clearButtonAlpha"
    )
    
    // Terminal button setup - display NEXT button when sheet appears
    // Following the same pattern as TokenLauncherScreen for proper terminal lifecycle
    DisposableEffect(Unit) {
        // Display NEXT button when entering this sheet
        scope.launch(Dispatchers.IO) {
            try {
                // Small delay to ensure smooth transition
                delay(100)
                terminalSDK?.displayNext { 
                    // Navigate to next screen with selected members
                    // Must dispatch to Main thread since this callback runs on a background thread
                    // and onContactsSelected modifies Compose state
                    scope.launch(Dispatchers.Main) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (selectedMembers.isNotEmpty()) {
                            onContactsSelected(selectedMembers.toList())
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Clean up terminal button when leaving this sheet
        onDispose { 
            scope.launch(Dispatchers.IO) { 
                try {
                    terminalSDK?.finishScreen()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } 
        } 
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
            .pointerInput(Unit) {
                detectTapGestures { 
                    // Consume touch events to prevent dismissal
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header
            SecondaryScreenHeader(
                title = "ADD MEMBERS".uppercase(),
                primaryColor = primaryColor,
                onDismiss = onBackClick,
            )
            
            // Search Bar with animated background
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .drawBehind {
                        drawRoundRect(
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            color = animatedColor,
                        )
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.searchicon),
                    contentDescription = "Search",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    DgenCursorSearchTextfield(
                        value = searchTextState,
                        onValueChange = { newValue ->
                            searchTextState = newValue
                            onSearchQueryChanged(newValue.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isSearchFocused = focusState.isFocused
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
                            fontSize = 20.sp
                        ),
                        cursorColor = primaryColor,
                        singleLine = true
                    )
                }
                
                // Clear button
                AnimatedVisibility(
                    visible = clearButtonAlpha > 0.1f,
                    enter = fadeIn(animationSpec = tween(mediumEnterDuration)),
                    exit = fadeOut(animationSpec = tween(mediumExitDuration))
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(primaryColor, CircleShape)
                            .clickable {
                                searchTextState = TextFieldValue("")
                                onSearchQueryChanged("")
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear",
                            tint = dgenBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Members count label
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
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append("${selectedMembers.size}")
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
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )
            
            // Contact list with gradients
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contactsWithEthAddress) { contact ->
                        val ethAddr = contact.ethAddress?.trim().orEmpty()
                        val isSelected = selectedMembers.contains(contact)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = ethAddr.isNotBlank()) {
                                    if (isSelected) {
                                        selectedMembers.remove(contact)
                                    } else {
                                        selectedMembers.add(contact)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Contact Name
                                Text(
                                    text = contact.name.ifBlank { 
                                        ethAddr.let { addr ->
                                            when {
                                                addr.endsWith(".eth") -> addr
                                                addr.length > 16 -> addr.take(8) + "..." + addr.takeLast(6)
                                                else -> addr
                                            }
                                        }
                                    },
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
                                
                                // ETH Address (truncated or ENS)
                                if (ethAddr.isNotBlank() && contact.name.isNotBlank()) {
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
                                checked = isSelected,
                                primaryColor = primaryColor,
                                onCheckedChange = {
                                    if (it) {
                                        selectedMembers.add(contact)
                                    } else {
                                        selectedMembers.remove(contact)
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Top gradient fade
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
                
                // Bottom gradient fade
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
        
        // DEBUG Button (only visible when BuildConfig.DEBUG is true)
        if (BuildConfig.DEBUG) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(4f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = primaryColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = primaryColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            // Debug action - navigate to EditGroupInfoSheet
                            println("DEBUG: Opening EditGroupInfoSheet with ${selectedMembers.size} members")
                            if (selectedMembers.isNotEmpty()) {
                                onContactsSelected(selectedMembers.toList())
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DEBUG: EDIT GROUP",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}