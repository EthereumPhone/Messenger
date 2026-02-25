package org.ethereumhpone.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ConfirmationOverlay
import com.example.dgenlibrary.SimpleDgenTextfield
import com.example.dgenlibrary.button.DgenSecondaryButton
import com.example.dgenlibrary.components.MemberItem
import com.example.dgenlibrary.ui.backgrounds.DgenNavigationBackground
import com.example.dgenlibrary.ui.backgrounds.FadeDirection
import com.example.dgenlibrary.ui.backgrounds.FadeEdge
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body2_fontSize
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.smallDuration
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenRed
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.Recipient

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupDetailsSheet(
    conversation: Conversation,
    primaryColor: Color,
    secondaryColor: Color,
    canManageMembers: Boolean = false,
    isSuperAdmin: Boolean = false,
    onBackClick: () -> Unit,
    onUpdateGroupName: (String) -> Unit,
    onUpdateGroupDescription: (String) -> Unit,
    onAddMembers: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onLeaveGroup: () -> Unit,
    onRemoveGroup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }
    var showRemoveGroupConfirmDialog by remember { mutableStateOf(false) }
    var showRemoveMemberDialog by remember { mutableStateOf<Recipient?>(null) }

    val otherRecipientIds = remember(conversation.recipients) {
        conversation.getOtherRecipients().map { it.id }.toSet()
    }

    var editNameState by remember(isEditMode) {
        mutableStateOf(TextFieldValue(conversation.title ?: ""))
    }
    var editDescriptionState by remember(isEditMode) {
        mutableStateOf(TextFieldValue(conversation.description ?: ""))
    }

    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    DgenNavigationBackground(
        modifier = modifier,
        primaryColor = primaryColor,
        focusManager = focusManager,
        onBackClick = onBackClick,
        headerContent = {
            Row(
                Modifier.fillMaxWidth()
            ){
                Text(
                    text = "GROUP DETAILS",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isEditMode) {
                        onUpdateGroupName(editNameState.text.trim())
                        onUpdateGroupDescription(editDescriptionState.text.trim())
                    }
                    isEditMode = !isEditMode
                }) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditMode) "Save" else "Edit",
                        tint = if (isEditMode) primaryColor else primaryColor.copy(alpha = 0.6f),
                        modifier = if (isEditMode) Modifier.size(28.dp) else Modifier
                    )
                }
            }

        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SimpleDgenTextfield(
                    value = editNameState,
                    onValueChange = { editNameState = it },
                    keyboardtype = KeyboardType.Text,
                    onEditDone = { },
                    readOnly = !isEditMode,
                    singleLine = true,
                    cursorColor = primaryColor,
                    activeColor = primaryColor,
                    view = view,
                    textfieldFocusManager = focusManager,
                    placeholder = {
                        Text(
                            text = if (isEditMode) "Type group name" else "Unnamed Group",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = primaryColor.copy(alpha = 0.45f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = body2_fontSize,
                            )
                        )
                    },
                    labelContent = {
                        Text(
                            text = "GROUP NAME",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = label_fontSize
                            )
                        )
                    }
                )

                SimpleDgenTextfield(
                    value = editDescriptionState,
                    onValueChange = { editDescriptionState = it },
                    keyboardtype = KeyboardType.Text,
                    onEditDone = { },
                    readOnly = !isEditMode,
                    singleLine = false,
                    maxLines = 4,
                    cursorColor = primaryColor,
                    activeColor = primaryColor,
                    view = view,
                    textfieldFocusManager = focusManager,
                    placeholder = {
                        Text(
                            text = if (isEditMode) "Type description" else "No description",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = primaryColor.copy(alpha = 0.45f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = body2_fontSize,
                            )
                        )
                    },
                    labelContent = {
                        Text(
                            text = "DESCRIPTION",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = label_fontSize
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MEMBERS ${conversation.getMemberCount()}",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )

                        AnimatedVisibility(
                            visible = isEditMode && canManageMembers,
                            enter = fadeIn(tween(smallDuration)),
                            exit = fadeOut(tween(smallDuration))
                        ) {
                            IconButton(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAddMembers()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Add members",
                                    tint = primaryColor
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = conversation.recipients,
                                key = { it.address }
                            ) { recipient ->
                                val isMe = recipient.id !in otherRecipientIds
                                val truncatedAddress = recipient.address.take(10) + "..." + recipient.address.takeLast(6)

                                MemberItem(
                                    modifier = Modifier.animateItemPlacement(),
                                    header = if (isMe) {
                                        if (isSuperAdmin) "You - Super Admin" else "You"
                                    } else {
                                        recipient.contact?.name
                                            ?: recipient.ens
                                            ?: truncatedAddress
                                    },
                                    subheader = if (isMe) {
                                        truncatedAddress
                                    } else if (recipient.contact?.name != null || recipient.ens != null) {
                                        truncatedAddress
                                    } else "",
                                    primaryColor = primaryColor,
                                    actionButton = {
                                            if (isEditMode && canManageMembers) {
                                                IconButton(
                                                    onClick = {
                                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        if (isMe) showLeaveConfirmDialog = true
                                                        else showRemoveMemberDialog = recipient
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PersonRemove,
                                                        contentDescription = if (isMe) "Leave group" else "Remove member",
                                                        tint = dgenRed.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                    }
                                )
                            }
                        }

                        FadeEdge(
                            FadeDirection.Top,
                            Modifier.align(Alignment.TopCenter),
                            size = 8.dp
                        )
                        FadeEdge(
                            FadeDirection.Bottom,
                            Modifier.align(Alignment.BottomCenter),
                            size = 8.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (isSuperAdmin) {
                    DgenSecondaryButton(
                        text = "Remove Group",
                        containerColor = dgenRed,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showRemoveGroupConfirmDialog = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    DgenSecondaryButton(
                        text = "Leave Group",
                        containerColor = dgenRed,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showLeaveConfirmDialog = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            FadeEdge(
                FadeDirection.Top,
                Modifier.align(Alignment.TopCenter),
                size = 16.dp
            )
            FadeEdge(
                FadeDirection.Bottom,
                Modifier.align(Alignment.BottomCenter),
                size = 16.dp
            )
        }
    }

    // Leave Group Confirmation Overlay
    ConfirmationOverlay(
        visible = showLeaveConfirmDialog,
        description = "Leave this group?",
        extraDescription = "You will no longer receive messages from this group.",
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        cancelButtonText = "CANCEL",
        confirmButtonText = "LEAVE",
        onCancel = { showLeaveConfirmDialog = false },
        onConfirm = {
            onLeaveGroup()
            showLeaveConfirmDialog = false
        }
    )

    // Remove Group Confirmation Overlay (super admin)
    ConfirmationOverlay(
        visible = showRemoveGroupConfirmDialog,
        description = "Remove this group?",
        extraDescription = "All members will be removed and the group will be deleted.",
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        cancelButtonText = "CANCEL",
        confirmButtonText = "REMOVE",
        onCancel = { showRemoveGroupConfirmDialog = false },
        onConfirm = {
            onRemoveGroup()
            showRemoveGroupConfirmDialog = false
        }
    )

    // Remove Member Confirmation Overlay
    showRemoveMemberDialog?.let { recipient ->
        val memberName = recipient.contact?.name
            ?: recipient.ens
            ?: (recipient.address.take(10) + "..." + recipient.address.takeLast(6))
        ConfirmationOverlay(
            visible = true,
            description = "Remove $memberName from the group?",
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            cancelButtonText = "CANCEL",
            confirmButtonText = "REMOVE",
            onCancel = { showRemoveMemberDialog = null },
            onConfirm = {
                onRemoveMember(recipient.id)
                showRemoveMemberDialog = null
            }
        )
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
        onBackClick = {},
        onUpdateGroupName = {},
        onUpdateGroupDescription = {},
        onAddMembers = {},
        onRemoveMember = {},
        onLeaveGroup = {},
        onRemoveGroup = {}
    )
}
