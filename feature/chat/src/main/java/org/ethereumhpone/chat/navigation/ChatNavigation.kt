package org.ethereumhpone.chat.navigation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import org.ethereumhpone.chat.ChatRoute
import java.net.URLDecoder
import java.net.URLEncoder


private val URL_CHARACTER_ENCODING = Charsets.UTF_8.name()

@VisibleForTesting
internal const val threadIdArg = "threadId"

const val chatGraphRoutePattern = "chat_graph"
const val chatRoute = "chat_route"


internal class ThreadIdArgs(val threadId: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(URLDecoder.decode(checkNotNull(savedStateHandle[threadIdArg]), URL_CHARACTER_ENCODING))
}

fun NavController.navigateToChat(threadId: String) {

    val encodedThreadId = URLEncoder.encode(threadId, URL_CHARACTER_ENCODING)

    this.navigate("$chatRoute/$encodedThreadId") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.chatScreen(
    onBackClick: () -> Unit,
) {
        composable(
            route = "$chatRoute/{$threadIdArg}",
            arguments = listOf(
                navArgument("threadId") { type = NavType.StringType },
            ),
        ) {
            val threadId = it.arguments?.getString("threadId")

            ChatRoute(
                navigateBackToConversations = onBackClick,
                threadId = threadId
            )
        }
}