package org.ethereumhpone.chat.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import org.ethereumhpone.chat.ChatRoute

const val chatGraphRoutePattern = "chat_graph"
const val chatRoute = "chat_route"

fun NavController.navigateToChat(navOptions: NavOptions? = null) {
    this.navigate(chatGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.chatGraph(
    navigateBackToConversations: () -> Unit,
//    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = chatGraphRoutePattern,
        startDestination = chatRoute
    ) {
        composable(
            route = chatRoute
        ) {
            ChatRoute(
                navigateBackToConversations = navigateBackToConversations
            )
        }
//        nestedGraphs()
    }
}