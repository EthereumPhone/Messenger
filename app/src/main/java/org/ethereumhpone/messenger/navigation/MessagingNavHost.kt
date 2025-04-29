package org.ethereumhpone.messenger.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import org.ethereumhpone.chat.navigation.chatScreen
import org.ethereumhpone.chat.navigation.navigateToChatByAddresses
import org.ethereumhpone.chat.navigation.navigateToChatByThreadId
import org.ethereumhpone.contracts.navigation.conversationsGraph
import org.ethereumhpone.contracts.navigation.conversationsGraphRoutePattern
import org.ethereumhpone.contracts.navigation.navigateToConversations
import org.ethereumhpone.messenger.ui.MessengerAppState
import org.ethereumphone.onboarding.navigation.navigateToOnboarding
import org.ethereumphone.onboarding.navigation.onboardingScreen

@Composable
fun MessagingNavHost(
    messengerAppState: MessengerAppState,
    modifier: Modifier = Modifier,
    threadId: Int? = null,
    inputAddress: String? = null,
    startDestination: String = conversationsGraphRoutePattern
){
    val navController = messengerAppState.navController
    val shouldShowOnboarding by messengerAppState.shouldShowOnboarding.collectAsState()


    // If threadId is not null, navigate to the chat
    threadId?.let {
        LaunchedEffect(it) {
            navController.navigateToChatByThreadId(threadId = it.toString())
        }
    }


    if (shouldShowOnboarding) {
        navController.navigateToOnboarding()
    }

    inputAddress?.let {
        LaunchedEffect(inputAddress) {
            navController.navigateToChatByAddresses(listOf(inputAddress))
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }, // Start from right
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth }, // Exit to left
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth }, // Start from left
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth }, // Exit to right
                animationSpec = tween(300)
            )
        }
    ) {
        conversationsGraph (
            onConversationClick = navController::navigateToChatByThreadId,
            openNewConversation = { contacts ->
                messengerAppState.navigateToConversation(contacts.map { it.getDefaultNumber()?.address ?: it.numbers[0].address })
            },
            conversationDestination = {
                chatScreen (
                    onBackClick = navController::popBackStack,
                )
            }
        )

        onboardingScreen(navController::navigateToConversations)

    }
}
