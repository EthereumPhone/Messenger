package org.ethereumhpone.messenger.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import org.ethereumhpone.messenger.navigation.MessagingNavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingApp(
    threadId: Int? = null
){
    MessagingNavHost(threadId = threadId)
}