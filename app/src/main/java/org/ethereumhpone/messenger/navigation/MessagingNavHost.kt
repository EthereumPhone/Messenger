package org.ethereumhpone.messenger.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    startDestination: String = conversationsGraphRoutePattern
){
    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ){



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