package org.ethereumhpone.messenger.ui

import androidx.compose.runtime.Composable
import org.ethereumhpone.messenger.navigation.MessagingNavHost

@Composable
fun MessagingApp(
    messengerAppState: MessengerAppState,
    threadId: Int? = null,
    inputAddress: String? = null
) {




    MessagingNavHost(
        messengerAppState = messengerAppState,
        threadId = threadId,
        inputAddress = inputAddress
    )
}