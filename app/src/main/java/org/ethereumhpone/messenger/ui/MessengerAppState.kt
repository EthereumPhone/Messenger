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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ethereumhpone.chat.navigation.navigateToChatByAddresses
import org.ethereumhpone.contracts.navigation.conversationsRoute
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


    val isInboxScreen: StateFlow<Boolean> = navController.currentBackStackEntryFlow
        .map { backStackEntry ->
            backStackEntry.destination.route == conversationsRoute
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // The onboarding screen should be displayed when either:
    // 1. The user has not chosen to hide it yet (first app launch), OR
    // 2. The user has not enabled XMTP support (`useXmtp` is still false).
    // This ensures that users who skipped XMTP setup will continue to see the onboarding
    // until they finish the process, while users who completed it will no longer be prompted.
    val shouldShowOnboarding = messengerPreferences.prefs
        .map { !it.shouldHideOnboarding || !it.useXmtp }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // Default to `null` while the preference is being loaded. This allows the UI layer
            // to display a proper loading indicator instead of briefly showing the wrong
            // destination.
            initialValue = null
        )


    fun navigateToConversation(addresses: List<String>) = navController.navigateToChatByAddresses(addresses)

}