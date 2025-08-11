package org.ethereumhpone.messenger.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import org.ethereumhpone.chat.navigation.chatScreen
import org.ethereumhpone.chat.navigation.navigateToChatByAddresses
import org.ethereumhpone.chat.navigation.navigateToChatByAddressesWithContactName
import org.ethereumhpone.chat.navigation.navigateToChatByThreadId
import org.ethereumhpone.chat.navigation.navigateToChatLoading
import org.ethereumhpone.contracts.navigation.conversationsGraph
import org.ethereumhpone.contracts.navigation.conversationsGraphRoutePattern
import org.ethereumhpone.contracts.navigation.navigateToConversations
import org.ethereumhpone.messenger.ui.MessengerAppState
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import org.ethereumphone.onboarding.navigation.onboardingScreen
import org.ethereumphone.onboarding.navigation.onboardingRoute

@Composable
fun MessagingNavHost(
    messengerAppState: MessengerAppState,
    modifier: Modifier = Modifier,
    threadId: Int? = null,
    inputAddress: String? = null,
    contactName: String? = null,
    startDestination: String = conversationsGraphRoutePattern
){
    val context = LocalContext.current
    val navController = messengerAppState.navController
    val shouldShowOnboarding by messengerAppState.shouldShowOnboarding.collectAsState()

    // Only start navigation once the preference value has been loaded.
    LaunchedEffect(shouldShowOnboarding) {
        // While `shouldShowOnboarding` is null we are still loading, so do nothing.
        shouldShowOnboarding?.let { showOnboarding ->
            val currentRoute = navController.currentDestination?.route

            if (showOnboarding && currentRoute != onboardingRoute) {
                // Navigate to onboarding, removing any conversations screens from back stack.
                navController.navigate(onboardingRoute) {
                    popUpTo(conversationsGraphRoutePattern) { inclusive = true }
                    launchSingleTop = true
                }
            } else if (!showOnboarding && currentRoute != conversationsGraphRoutePattern) {
                // Ensure the user sees the main conversations stack and remove onboarding.
                navController.navigate(conversationsGraphRoutePattern) {
                    popUpTo(onboardingRoute) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }


    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    // Fade between the loading indicator and the real NavHost once the preference value is available.
    Crossfade(
        targetState = shouldShowOnboarding,
        animationSpec = tween(com.example.dgenlibrary.ui.theme.largeEnterDuration)
    ) { onboardingFlag ->
        if (onboardingFlag == null) {
            // Still loading preferences – show the matrix spinner.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(org.ethereumphone.dgenlibrary.theme.dgenBlack),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                // Ensure colors are up-to-date.
                val context = androidx.compose.ui.platform.LocalContext.current

                DgenLoadingMatrix(
                    unactiveLEDColor = secondaryColor,
                    activeLEDColor = primaryColor
                )
            }
        } else {
            // Preferences loaded – show the real navigation graph.
            NavHost(
                navController = navController,
                // Select the appropriate initial destination based on the onboarding flag so
                // that we don't show an intermediate screen before the navigation effect
                // has a chance to run.
                startDestination = if (onboardingFlag) onboardingRoute else conversationsGraphRoutePattern,
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
                    conversationDestination = {
                        chatScreen (
                            navController = navController,
                            onBackClick = {
                                // Check if we came from contacts app by looking for chat loading route in back stack
                                val backStackEntry = navController.previousBackStackEntry
                                val cameFromChatLoading = backStackEntry?.destination?.route?.contains("chat_loading_route") == true
                                
                                if (cameFromChatLoading) {
                                    // If we came from chat loading (which indicates navigation from contacts app),
                                    // navigate to inbox instead of going back to chat loading
                                    navController.navigate(conversationsGraphRoutePattern) {
                                        popUpTo(conversationsGraphRoutePattern) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    // Normal back navigation
                                    navController.popBackStack()
                                }
                            },
                        )
                    }
                )

                onboardingScreen(navController::navigateToConversations)
            }
            
            // Handle deep link navigation after NavHost and navigation graph are set up
            // Only handle external deep links, not internal navigation
            LaunchedEffect(threadId, inputAddress, contactName) {
                // If threadId is not null, navigate to the chat
                threadId?.let {
                    navController.navigateToChatByThreadId(threadId = it.toString())
                }
                
                // For inputAddress, only navigate to chat loading if we're starting fresh
                // This prevents conflict with internal contact selection
                inputAddress?.let { address ->
                    // Add a delay to see if any internal navigation happens first
                    kotlinx.coroutines.delay(100)
                    
                    // Only navigate to chat loading if we're still on the conversations route
                    // and haven't navigated elsewhere via internal contact selection
                    val currentRoute = navController.currentDestination?.route
                    if (currentRoute?.contains("conversations_route") == true) {
                        navController.navigateToChatLoading(listOf(address), contactName)
                    }
                }
            }
            
        }
    }
}
