package org.ethereumhpone.chat.components

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
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
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.body2_fontSize
import com.example.dgenlibrary.ui.theme.label_fontSize
import org.ethereumphone.dgenlibrary.components.ConfirmationOverlay
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethereumphone.dgenlibrary.components.SimpleDgenTextfield
import org.ethereumphone.dgenlibrary.showDgenToast
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenRed
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.Recipient

@Composable
fun GroupDetailsSheet(
    conversation: Conversation,
    primaryColor: Color,
    secondaryColor: Color,
    isAdmin: Boolean = false,
    onBackClick: () -> Unit,
    onUpdateGroupName: (String) -> Unit,
    onUpdateGroupDescription: (String) -> Unit,
    onAddMembers: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onLeaveGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    
    // Text field states
    var groupNameState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(conversation.title ?: ""))
    }
    var groupDescriptionState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(conversation.description ?: ""))
    }
    
    var showRemoveMemberOverlay by remember { mutableStateOf<Recipient?>(null) }
    var showLeaveGroupOverlay by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    
    // Get unique members by address
    val uniqueMembers = remember(conversation.recipients) {
        conversation.recipients.distinctBy { it.address }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header - same as SecondaryScreenHeader used in ChatScreen style
            SecondaryScreenHeader(
                title = "GROUP DETAILS",
                primaryColor = primaryColor,
                onDismiss = onBackClick
            )
            
            // Scrollable content with fade overlays
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Group Name Field
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleDgenTextfield(
                            modifier = Modifier.fillMaxWidth(),
                            value = groupNameState,
                            onValueChange = { newValue ->
                                if (isAdmin) {
                                    groupNameState = newValue
                                } else {
                                    showDgenToast(context, "Only admins can change the group name")
                                }
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
                            readOnly = !isAdmin,
                            onEditDone = {
                                if (isAdmin && groupNameState.text.isNotBlank()) {
                                    onUpdateGroupName(groupNameState.text.trim())
                                } else if (!isAdmin) {
                                    showDgenToast(context, "Only admins can change the group name")
                                }
                            },
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
                    
                    // Group Description Field
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleDgenTextfield(
                            modifier = Modifier.fillMaxWidth(),
                            value = groupDescriptionState,
                            onValueChange = { newValue ->
                                groupDescriptionState = newValue
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
                            singleLine = false,
                            maxLines = 3,
                            onEditDone = {
                                onUpdateGroupDescription(groupDescriptionState.text.trim())
                            },
                            placeholder = if (groupDescriptionState.text.isEmpty()) {
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
                                text = "DESCRIPTION".uppercase(),
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = primaryColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = label_fontSize
                                )
                            )
                        }
                    }
                    
                    // Members Section Header - same style as EditGroupInfoSheet
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
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
                                    append("${uniqueMembers.size}")
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
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                    
                    // Members List - unique members with remove icon
                    items(
                        items = uniqueMembers,
                        key = { it.address }
                    ) { recipient ->
                        GroupMemberItem(
                            recipient = recipient,
                            primaryColor = primaryColor,
                            showRemoveIcon = isAdmin,
                            onRemoveClick = { showRemoveMemberOverlay = recipient }
                        )
                    }
                    
                    // Leave Group Button - SpaceMono, uppercase, dgenRed, transparent background
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        TextButton(
                            onClick = { showLeaveGroupOverlay = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "LEAVE GROUP",
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = body1_fontSize,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                )
                            )
                        }
                    }
                    
                    // Bottom spacer for fade overlay
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Top gradient fade overlay
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
                
                // Bottom gradient fade overlay
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
        
        // Leave Group Confirmation Overlay - Full screen
        if (showLeaveGroupOverlay) {
            ConfirmationOverlay(
                visible = true,
                description = "Leave this group?",
                extraDescription = "You will no longer receive messages from this group.",
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                onDelete = {
                    onLeaveGroup()
                    showLeaveGroupOverlay = false
                },
                onCancel = {
                    showLeaveGroupOverlay = false
                }
            )
        }
        
        // Remove Member Confirmation Overlay - Full screen
        showRemoveMemberOverlay?.let { recipient ->
            val displayName = recipient.contact?.name 
                ?: recipient.ens 
                ?: recipient.address.let { addr ->
                    if (addr.length > 16) addr.take(8) + "..." + addr.takeLast(6) else addr
                }
            
            ConfirmationOverlay(
                visible = true,
                description = "Remove $displayName from the group?",
                extraDescription = "This member will no longer receive messages from this group.",
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                onDelete = {
                    onRemoveMember(recipient.id)
                    showRemoveMemberOverlay = null
                },
                onCancel = {
                    showRemoveMemberOverlay = null
                }
            )
        }
    }
}

@Composable
private fun GroupMemberItem(
    recipient: Recipient,
    primaryColor: Color,
    showRemoveIcon: Boolean = false,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ethAddr = recipient.address.trim()
    
    // Row layout with remove icon on the right, vertically centered
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Member info column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recipient.contact?.name?.ifBlank { null }
                    ?: recipient.ens
                    ?: ethAddr.let { addr ->
                        when {
                            addr.endsWith(".eth") -> addr
                            addr.length > 16 -> addr.take(8) + "..." + addr.takeLast(6)
                            else -> addr
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
                ),
                maxLines = 1
            )
            
            // Show eth address if name or ENS is displayed
            if ((recipient.contact?.name?.isNotBlank() == true || recipient.ens != null) && ethAddr.isNotBlank()) {
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
                        .padding(top = 2.dp),
                    maxLines = 1
                )
            }
        }
        
        // Remove icon - only shown for admins
        if (showRemoveIcon) {
            Icon(
                imageVector = Icons.Rounded.Clear,
                contentDescription = "Remove member",
                tint = primaryColor,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRemoveClick() }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewGroupDetailsSheet() {
    GroupDetailsSheet(
        conversation = Conversation(
            id = "1",
            title = "XMTP Developers",
            description = "A group for XMTP developers to discuss the protocol.",
            recipients = listOf(
                Recipient(
                    id = "user1",
                    address = "0x1234567890abcdef1234567890abcdef12345678",
                    ens = "alice.eth",
                    contact = Contact("lk1", "Alice", null, "0x123")
                ),
                Recipient(
                    id = "user2",
                    address = "0xabcdef1234567890abcdef1234567890abcdef12",
                    ens = null,
                    contact = Contact("lk2", "Bob", null, "0xabc")
                ),
                Recipient(
                    id = "user3",
                    address = "0x9876543210fedcba9876543210fedcba98765432",
                    ens = "charlie.base.eth",
                    contact = null
                )
            ),
            draft = null,
            lastMessage = null,
            isGroup = true,
            clientInbox = "myInbox"
        ),
        primaryColor = dgenTurqoise,
        secondaryColor = dgenBlack,
        isAdmin = true,
        onBackClick = {},
        onUpdateGroupName = {},
        onUpdateGroupDescription = {},
        onAddMembers = {},
        onRemoveMember = {},
        onLeaveGroup = {}
    )
}
