package org.ethereumhpone.messenger.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import org.ethereumhpone.messenger.navigation.MessagingNavHost
import org.ethereumphone.contacts.ContactSheet
import org.ethereumphone.settings.SettingsDialog

@Composable
fun MessagingApp(
    messengerAppState: MessengerAppState,
    threadId: Int? = null,
    inputAddress: String? = null
) {

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showContactSheet by rememberSaveable { mutableStateOf(false) }

    MessagingApp(
        messengerAppState = messengerAppState,
        showSettingsDialog = showSettingsDialog,
        showContactSheet = showContactSheet,
        threadId = threadId,
        inputAddress = inputAddress,
        onTopAppBarActionClick = { showSettingsDialog = true },
        onFabClick = { showContactSheet = true },
        onDismissContactSheet = { showContactSheet = false },
        onDismissSettingsDialog = { showSettingsDialog = false }
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MessagingApp(
    messengerAppState: MessengerAppState,
    showSettingsDialog: Boolean,
    showContactSheet: Boolean,
    threadId: Int? = null,
    inputAddress: String? = null,
    onTopAppBarActionClick: () -> Unit,
    onFabClick: () -> Unit,
    onDismissSettingsDialog:() -> Unit,
    onDismissContactSheet: () -> Unit
) {

    if (showSettingsDialog) {
        SettingsDialog(onDismissSettingsDialog)
    }

    val sheetState = rememberModalBottomSheetState(true)

    if (showContactSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissContactSheet,
            sheetState = sheetState,
            containerColor = Color.Black
        ) {
            ContactSheet(
                onDismiss = onDismissContactSheet,
                onContactsSelected = { contacts ->
                    onDismissContactSheet()
                    //TODO: CHANGE TO NOT ONLY LOOK FOR PHONE NUMBER !!!URGENT!!!
                    messengerAppState.navigateToConversation(contacts.map { it.getDefaultNumber()?.address ?: it.numbers[0].address }) }
            )
        }
    }

    val isInbox by messengerAppState.isInboxScreen.collectAsState()


    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            if (isInbox) {
                FloatingActionButton(onClick = onFabClick) { Icon(Icons.Default.Add, "") }
            }
        },
        topBar = {
            if(isInbox) {
                CenterAlignedTopAppBar(
                    title = { Text("Messenger", fontSize = 24.sp) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White
                    ),
                    actions = { IconButton(onTopAppBarActionClick) { Icon(Icons.Default.Settings, "options") } }
                )
            }
        },
        contentWindowInsets =  ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)

        ) {


            MessagingNavHost(
                messengerAppState = messengerAppState,
                threadId = threadId,
                inputAddress = inputAddress
            )
        }
    }
}