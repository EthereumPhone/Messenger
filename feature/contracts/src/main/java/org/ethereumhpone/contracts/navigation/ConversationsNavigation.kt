package org.ethereumhpone.contracts.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import org.ethereumhpone.contracts.ContactRoute

import androidx.navigation.compose.*
import org.ethereumhpone.database.model.Contact


const val conversationsGraphRoutePattern = "conversations_graph"
const val conversationsRoute = "conversations_route"

fun NavController.navigateToConversations(navOptions: NavOptions? = null) {
    this.navigate(conversationsGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.conversationsGraph(
    navigateToChat: (String, Contact?) -> Unit, //threadId Long -> String
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = conversationsGraphRoutePattern,
        startDestination = conversationsRoute
    ) {
        composable(
            route = conversationsRoute
        ) {
            ContactRoute(
                navigateToChat = navigateToChat,


            )
        }
        nestedGraphs()
    }
}


