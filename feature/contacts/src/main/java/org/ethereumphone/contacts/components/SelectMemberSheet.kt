package org.ethereumphone.contacts.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.DgenSearchRow
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenRed
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import com.messenger.terminalsdk.TerminalSDK
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.contacts.QueryResultUiState

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

    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var showFinalGroupSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val terminalSDK = remember {
        try { TerminalSDK(context) } catch (_: Exception) { null }
    }

    val hasSelectedItems = selectedItems.isNotEmpty()
    DisposableEffect(hasSelectedItems) {
        if (hasSelectedItems) {
            coroutineScope.launch {
                try {
                    if (terminalSDK?.isAvailable() == true) {
                        terminalSDK.displayNext {
                            showFinalGroupSheet = true
                            onContactsSelected(selectedItems.toList())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            coroutineScope.launch {
                try {
                    if (terminalSDK?.isAvailable() == true) {
                        terminalSDK.removeNext()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        onDispose {
            coroutineScope.launch {
                try {
                    if (terminalSDK?.isAvailable() == true) {
                        terminalSDK.removeNext()
                    }
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
                            if (terminalSDK?.isAvailable() == true) {
                                terminalSDK.displayNext {
                                    showFinalGroupSheet = true
                                    onContactsSelected(selectedItems.toList())
                                }
                            }
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
                        DgenSearchRow(
                            searchValue = textState,
                            onValueChange = { newTextFieldValue ->
                                val processedText = newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")
                                val newProcessedTextFieldValue = newTextFieldValue.copy(text = processedText)
                                textState = newProcessedTextFieldValue
                                onSearchQueryChanged(newProcessedTextFieldValue.text)
                            },
                            onClearValue = {
                                textState = TextFieldValue("")
                                onSearchQueryChanged("")
                            },
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            placeholder = {
                                Text(
                                    text = "Search name, ENS or address".uppercase(),
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

                        // Results list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp,end = 12.dp)
                                ,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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

                    }
                }
            }
        }
    }
}
