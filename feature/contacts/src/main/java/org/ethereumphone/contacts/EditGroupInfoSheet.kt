package org.ethereumphone.contacts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.messenger.terminalsdk.TerminalSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.dgenlibrary.components.SimpleDgenTextfield
import androidx.compose.ui.platform.LocalView
import com.example.dgenlibrary.ui.theme.body2_fontSize
import org.ethereumphone.contacts.BuildConfig

@Composable
fun EditGroupInfoSheet(
    members: List<ContactEntity>,
    onBackClick: () -> Unit,
    onCreateGroup: (List<ContactEntity>, String) -> Unit,
    onMemberRemoved: (String) -> Unit = {},
    primaryColor: Color,
    secondaryColor: Color,
    terminalSDK: TerminalSDK? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val haptics = LocalHapticFeedback.current
    
    // Group name state
    var groupNameState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    
    // Loading state to prevent multiple clicks
    var isCreating by remember { mutableStateOf(false) }
    
    // Mutable list of members for removal
    val currentMembers = remember { mutableStateListOf<ContactEntity>() }
    LaunchedEffect(members) {
        currentMembers.clear()
        currentMembers.addAll(members)
    }
    
    // Terminal button setup - display CREATE GROUP button when sheet appears
    // Following the same pattern as TokenLauncherScreen for proper terminal lifecycle
    DisposableEffect(Unit) {
        // Display CREATE GROUP button when entering this sheet
        scope.launch(Dispatchers.IO) {
            try {
                // Small delay to ensure smooth transition
                delay(100)
                terminalSDK?.displayCreateGroup { 
                    // Trigger group creation
                    // The callback already runs on Main thread via MiniDisplayTouchHandler.getMainExecutor()
                    // so we can directly access Compose state and haptics
                    if (groupNameState.text.isNotBlank() && currentMembers.isNotEmpty()) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        println("DEBUG: Creating group with name=${groupNameState.text}, members=${currentMembers.size}")
                        isCreating = true
                        onCreateGroup(currentMembers.toList(), groupNameState.text.trim())
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
    
    // Show loading screen when creating with fade animation
    AnimatedVisibility(
        visible = isCreating,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 50
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dgenBlack),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing animation for text
            val infiniteTransition = rememberInfiniteTransition(label = "textPulse")
            val textAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "textAlpha"
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                DgenLoadingMatrix(
                    unactiveLEDColor = secondaryColor,
                    activeLEDColor = primaryColor
                )
                
                Text(
                    text = "CREATING GROUP",
                    modifier = Modifier.alpha(textAlpha),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        letterSpacing = 0.sp
                    )
                )
            }
        }
    }
    
    // Show main content when not creating
    if (!isCreating) {
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
                    title = "EDIT GROUP".uppercase(),
                    primaryColor = primaryColor,
                    onDismiss = onBackClick,
                )
                
                // Group Name Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {
                    SimpleDgenTextfield(
                        modifier = Modifier.fillMaxWidth(),
                        value = groupNameState,
                        onValueChange = { newValue ->
                            groupNameState = newValue
                        },
                        textfieldFocusManager = focusManager,
                        keyboardtype = KeyboardType.Text,
                        textStyle = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = body2_fontSize
                        ),
                        activeColor = primaryColor,
                        cursorColor = primaryColor,
                        cursorWidth = 16.dp,
                        cursorHeight = 32.dp,
                        singleLine = true,
                        maxLines = 1,
                        onEditDone = {},
                        placeholder = if (groupNameState.text.isEmpty()) {
                            {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "ENTER",
                                    style = TextStyle(
                                        fontFamily = PitagonsSans,
                                        color = dgenWhite.copy(alpha = 0.45f),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = body2_fontSize
                                    )
                                )
                            }
                        } else null,
                        view = view,
                    ) {
                        Text(
                            text = "GROUP NAME".uppercase(),
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = label_fontSize
                            )
                        )
                    }
                }
                
                // Members Count Label
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
                            append("${currentMembers.size}")
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
                
                // Member List with remove button
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentMembers) { contact ->
                            val ethAddr = contact.ethAddress?.trim().orEmpty()
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
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
                                
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Remove",
                                    tint = primaryColor,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            if (ethAddr.isNotBlank()) {
                                                currentMembers.remove(contact)
                                                onMemberRemoved(ethAddr)
                                            }
                                        }
                                )
                            }
                        }
                    }
                    
                    // Top gradient
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
                    
                    // Bottom gradient
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
            
            // DEBUG Button
            if (BuildConfig.DEBUG) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.BottomEnd),
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
                            .alpha(if (groupNameState.text.isNotBlank() && currentMembers.isNotEmpty()) 1f else 0.35f)
                            .clickable(enabled = groupNameState.text.isNotBlank() && currentMembers.isNotEmpty()) {
                                // Debug action - trigger existing create group logic (same as terminal screen click)
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                println("DEBUG: Creating group with name=${groupNameState.text}, members=${currentMembers.size}")
                                isCreating = true
                                onCreateGroup(currentMembers.toList(), groupNameState.text.trim())
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DEBUG: CREATE GROUP",
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
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
private fun EditGroupInfoSheet_CreatingPreview() {
    // Pulsing animation for text
    val infiniteTransition = rememberInfiniteTransition(label = "textPulsePreview")
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlphaPreview"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            DgenLoadingMatrix(
                unactiveLEDColor = dgenOcean,
                activeLEDColor = dgenTurqoise
            )
            
            Text(
                text = "CREATING GROUP",
                modifier = Modifier.alpha(textAlpha),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    letterSpacing = 0.sp
                )
            )
        }
    }
}