package org.ethereumhpone.messenger.ui

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import org.ethereumhpone.messenger.navigation.MessagingNavHost

@Composable
fun MessagingApp(
    messengerAppState: MessengerAppState,
    threadId: Int? = null,
    inputAddress: String? = null
) {

    MessagingApp(
        messengerAppState = messengerAppState,
        showSettingsDialog = false,
        threadId = threadId,
        inputAddress = inputAddress
    ) { }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MessagingApp(
    messengerAppState: MessengerAppState,
    showSettingsDialog: Boolean,
    threadId: Int? = null,
    inputAddress: String? = null,
    onTopAppBarActionClick: () -> Unit
) {

    Scaffold(
        containerColor = Color.Black,
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
            val destination = messengerAppState.currentDestination

            if(destination != null) {
                CenterAlignedTopAppBar(
                    title = { Text("Messenger", fontSize = 24.sp) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White
                    )
                )
            }

            MessagingNavHost(
                messengerAppState = messengerAppState,
                threadId = threadId,
                inputAddress = inputAddress
            )
        }
    }
}