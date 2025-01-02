package org.ethereumhpone.messenger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ethereumhpone.chat.navigation.navigateToChatByAddresses
import org.ethereumhpone.contracts.navigation.navigateToConversations
import org.ethereumhpone.datastore.MessengerPreferences

@Composable
fun rememberMessengerAppState(
    messengerPreferences: MessengerPreferences,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController()
): MessengerAppState {

    return remember(
        navController,
        coroutineScope,
        messengerPreferences
    ) {
        MessengerAppState(
            navController,
            coroutineScope,
            messengerPreferences
        )
    }
}

@Stable
class MessengerAppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    messengerPreferences: MessengerPreferences
) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)

    val currentDestination: NavDestination?
        @Composable get() {
            // Collect the currentBackStackEntryFlow as a state
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            // Fallback to previousDestination if currentEntry is null
            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val shouldShowOnboarding = messengerPreferences.prefs
        .map { !it.shouldHideOnboarding }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )


    fun navigateToConversation(addresses: List<String>) = navController.navigateToChatByAddresses(addresses)

}