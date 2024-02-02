package org.ethereumhpone.messenger

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import org.ethereumhpone.chat.navigation.chatGraph
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
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ){

        chatGraph(
            navigateBackToConversations = navController::popBackStack
        )

        conversationsGraph (
            navigateToChat = navController::navigateToChat
        )
    }

//    NavHost(navController = navController, startDestination = "home") {
//        composable("home") {
//            ContactScreen(navigateToChat={})
//        }
////        composable("addProject") {
////            AddProjectScreen()
////        }
////        composable("addTask") {
////            AddTaskScreen()
////        }
//    }
}