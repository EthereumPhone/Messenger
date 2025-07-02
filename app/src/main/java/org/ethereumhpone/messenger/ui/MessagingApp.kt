package org.ethereumhpone.messenger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumhpone.messenger.navigation.MessagingNavHost
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
    onDismissSettingsDialog:() -> Unit,
) {

    if (showSettingsDialog) {
        SettingsDialog(onDismissSettingsDialog)
    }

    val sheetState = rememberModalBottomSheetState(true)

    val isInbox by messengerAppState.isInboxScreen.collectAsState()
    val density = LocalDensity.current
    var visible by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = dgenBlack,
        /*
        floatingActionButton = {    if (isInbox) {
                FloatingActionButton(onClick = onFabClick) { Icon(Icons.Default.Add, "") }
            }
        },
         */


    ) { padding ->
        Box( modifier = Modifier.fillMaxSize().padding(padding).background(dgenBlack)) {
            MessagingNavHost(
                messengerAppState = messengerAppState,
                threadId = threadId,
                inputAddress = inputAddress
            )
        }
    }
}