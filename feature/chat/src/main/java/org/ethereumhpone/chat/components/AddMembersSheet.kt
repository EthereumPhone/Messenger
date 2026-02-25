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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.dgenlibrary.DgenSearchRow
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.messenger.terminalsdk.TerminalSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenRed
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise

@Composable
fun AddMembersSheet(
    eligibleContacts: List<ContactEntity>,
    onAddMembers: (List<ContactEntity>) -> Unit,
    onBackClick: () -> Unit,
    primaryColor: Color = dgenTurqoise,
    secondaryColor: Color = dgenOcean,
) {
    val selectedItems = remember { mutableStateListOf<ContactEntity>() }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val terminalSDK = remember {
        try { TerminalSDK(context) } catch (_: Exception) { null }
    }

    val addMembersHandler = {
        onAddMembers(selectedItems.toList())
    }

    suspend fun showTerminalButton() {
        if (terminalSDK?.isAvailable() != true) return
        terminalSDK.displayAddMember(addMembersHandler)
    }

    suspend fun hideTerminalButton() {
        if (terminalSDK?.isAvailable() != true) return
        terminalSDK.removeAddMember()
    }

    val hasSelectedItems = selectedItems.isNotEmpty()
    DisposableEffect(hasSelectedItems) {
        if (hasSelectedItems) {
            coroutineScope.launch {
                try {
                    showTerminalButton()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            coroutineScope.launch {
                try {
                    hideTerminalButton()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        onDispose {
            // Use a standalone scope so cleanup isn't cancelled when the composable
            // leaves composition (rememberCoroutineScope is cancelled at that point).
            CoroutineScope(Dispatchers.Main.immediate + SupervisorJob()).launch {
                try {
                    hideTerminalButton()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        var isFirstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isFirstResume) {
                    isFirstResume = false
                    return@LifecycleEventObserver
                }
                if (selectedItems.isNotEmpty()) {
                    coroutineScope.launch {
                        delay(500)
                        try {
                            showTerminalButton()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val searchQuery = textState.text.trim()

    val filteredContacts = remember(eligibleContacts, searchQuery) {
        val contactsWithEth = eligibleContacts.filter { !it.ethAddress.isNullOrBlank() }
        if (searchQuery.isEmpty()) {
            contactsWithEth
        } else {
            contactsWithEth.filter { contact ->
                val address = contact.ethAddress.orEmpty()
                contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.lookupKey.contains(searchQuery, ignoreCase = true) ||
                    address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val manualEntry = remember(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            ContactEntity(
                lookupKey = searchQuery,
                ethAddress = searchQuery,
                name = searchQuery
            )
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        DgenHeaderBackground(
            title = "ADD MEMBERS",
            primaryColor = primaryColor,
            onBackClick = onBackClick
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, bottom = 16.dp)
            ) {
                DgenSearchRow(
                    searchValue = textState,
                    onValueChange = { newTextFieldValue ->
                        val processedText = newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")
                        textState = newTextFieldValue.copy(text = processedText)
                    },
                    onClearValue = {
                        textState = TextFieldValue("")
                    },
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    placeholder = {
                        Text(
                            text = "SEARCH NAME, ENS OR ADDRESS",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor.copy(alpha = 0.45f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (searchQuery.isNotEmpty() && manualEntry != null) {
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
                                                        style = Stroke(width = 2.dp.toPx())
                                                    )
                                                }
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {}
                            }
                        }

                        item {
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    items(filteredContacts) { contact ->
                        AddMembersSelectableItem(
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

@Composable
private fun AddMembersSelectableItem(
    header: String,
    subheader: String = "",
    isSelected: Boolean,
    onCheckClick: () -> Unit = {},
    primaryColor: Color = dgenTurqoise,
    secondaryColor: Color = dgenOcean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onCheckClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = header,
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
            if (subheader.isNotEmpty()) {
                Text(
                    text = subheader,
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

        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isSelected) primaryColor else Color.Transparent,
                    shape = RoundedCornerShape(0.dp)
                )
                .then(
                    if (!isSelected) {
                        Modifier.drawBehind {
                            drawRect(
                                color = primaryColor,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = secondaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
