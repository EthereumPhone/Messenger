package org.ethereumhpone.messenger.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController

import org.ethereumhpone.chat.navigation.chatScreen
import org.ethereumhpone.chat.navigation.navigateToChat
import org.ethereumhpone.contracts.ContactScreen
import org.ethereumhpone.contracts.navigation.conversationsGraph
import org.ethereumhpone.contracts.navigation.conversationsGraphRoutePattern
import org.ethereumhpone.contracts.navigation.conversationsRoute
import org.ethereumhpone.contracts.navigation.navigateToConversations

@Composable
fun MessagingNavHost(
    modifier: Modifier = Modifier,
    threadId: Int? = null,
    startDestination: String = conversationsGraphRoutePattern
){
    val navController = rememberNavController()

    // If threadId is not null, navigate to the chat
    threadId?.let {
        LaunchedEffect(it) {
            navController.navigateToChat(threadId = it.toString())
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
            navigateToChat = navController::navigateToChat,
            nestedGraphs = {
                chatScreen (
                    onBackClick = navController::popBackStack,
                )
            }
        )
    }
}
