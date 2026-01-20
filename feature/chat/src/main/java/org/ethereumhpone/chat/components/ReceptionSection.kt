package org.ethereumhpone.chat.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//@Composable
//fun RecipientSection(
//    recipientUiState: RecipientUiState,
//    selectedContact: Contact? = null,
//    hasContactsWithEth: Boolean = false,
//    onContentChanged: (String) -> Unit,
//    onContactIconClick: () -> Unit = {},
//    onClearContact: () -> Unit = {},
//    shouldDismissKeyboard: Boolean = false,
//    onKeyboardDismissed: () -> Unit = {}
//) {
//    // #region agent log
//    Log.d("DEBUG_AGENT", "RecipientSection:entry - hasContactsWithEth=$hasContactsWithEth, selectedContact=${selectedContact?.name ?: "null"}, shouldShowContactButton=${hasContactsWithEth && selectedContact == null}, hypothesisId=A,B")
//    // #endregion
//
//    val primaryColor = SystemColorManager.primaryColor
//    val secondaryColor = SystemColorManager.secondaryColor
//    val focusManager = LocalFocusManager.current
//
//    val (headerText, headerColor) = when {
//        selectedContact != null -> "SENDING TO CONTACT" to primaryColor
//        recipientUiState.ensError.isNotEmpty() -> "ENS ERROR" to dgenRed
//        recipientUiState.isResolving -> "RESOLVING ENS..." to dgenOrche
//        recipientUiState.recipientAddress.endsWith(".eth") -> "ENS RESOLVED" to dgenGreen
//        else -> "TARGET ADDRESS" to primaryColor
//    }
//
//    // Keep cursor position
//    var textFieldValue by remember { mutableStateOf(TextFieldValue(recipientUiState.recipientAddress)) }
//    LaunchedEffect(recipientUiState) {
//        if (textFieldValue.text != recipientUiState.recipientAddress) {
//            textFieldValue = TextFieldValue(
//                text = recipientUiState.recipientAddress,
//                selection = TextRange(recipientUiState.recipientAddress.length)
//            )
//        }
//    }
//
//    // Clear keyboard when ViewModel indicates it should be dismissed
//    LaunchedEffect(shouldDismissKeyboard) {
//        if (shouldDismissKeyboard) {
//            focusManager.clearFocus()
//            onKeyboardDismissed()
//        }
//    }
//
//    DgenTextfield(
//        value = textFieldValue,
//        onValueChange = { new ->
//            textFieldValue = new
//            onContentChanged(new.text)
//        },
//        showTextField = selectedContact == null,
//        headerContent = {
//            // Header with contacts icon
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = headerText,
//                    color = headerColor,
//                    style = TextStyle(
//                        fontFamily = SpaceMono,
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = label_fontSize,
//                        lineHeight = label_fontSize,
//                        letterSpacing = 1.sp,
//                        textDecoration = TextDecoration.None,
//                        textAlign = TextAlign.Left
//                    )
//                )
//
//                Spacer(modifier = Modifier.width(25.dp))
//
//                // #region agent log
//                Log.d("DEBUG_AGENT", "RecipientSection:buttonCheck - hasContactsWithEth=$hasContactsWithEth, selectedContactIsNull=${selectedContact == null}, willShowButton=${hasContactsWithEth && selectedContact == null}, hypothesisId=A")
//                // #endregion
//
//                // Only show contacts icon if we have contacts with ETH addresses and no contact is selected
//                if (hasContactsWithEth && selectedContact == null) {
//                    IconButton(
//                        onClick = onContactIconClick,
//                        modifier = Modifier.size(32.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Rounded.Contacts,
//                            contentDescription = "Select contact",
//                            tint = primaryColor,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                }
//            }
//        },
//        customContent = {
//            // Show contact UI if a contact is selected
//            if (selectedContact != null) {
//                Column(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Spacer(modifier = Modifier.fillMaxWidth().height(4.dp))
//                    SelectedContact(
//                        contact = selectedContact,
//                        primaryColor = primaryColor,
//                        secondaryColor = secondaryColor,
//                        onClear = onClearContact
//                    )
//                }
//            }
//        },
//        placeholder = {
//            Text(
//                text = "Address",
//                style = TextStyle(
//                    fontFamily = PitagonsSans,
//                    color = dgenWhite.copy(pulseOpacity),
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 24.sp
//                ),
//            )
//        },
//        keyboardType = KeyboardType.Text,
//        cursorColor = primaryColor,
//        activeColor = primaryColor
//    )
//}
