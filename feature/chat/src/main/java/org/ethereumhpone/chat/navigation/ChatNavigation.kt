package org.ethereumhpone.chat.navigation

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.SharedTransitionScope
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ethereumhpone.chat.ChatRoute
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.util.Converters
import java.net.URLDecoder
import java.net.URLEncoder


private val URL_CHARACTER_ENCODING = Charsets.UTF_8.name()

@VisibleForTesting
internal const val threadIdArg = "threadId"

@VisibleForTesting
internal const val contactArg = "contact"


const val chatGraphRoutePattern = "chat_graph"
const val chatRoute = "chat_route"


internal class ThreadIdArgs(val threadId: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(URLDecoder.decode(checkNotNull(savedStateHandle[threadIdArg]), URL_CHARACTER_ENCODING))
}

fun NavController.navigateToChat(threadId: String, contact: Contact?) {

    val encodedThreadId = URLEncoder.encode(threadId, URL_CHARACTER_ENCODING)


    if (contact != null) {
        val encodedContact =  URLEncoder.encode(Converters().toContact(contact), URL_CHARACTER_ENCODING)

        this.navigate("$chatRoute/$encodedThreadId/$encodedContact") {
            launchSingleTop = true
        }
    } else {
        this.navigate("$chatRoute/$encodedThreadId") {
            launchSingleTop = true
        }
    }
}

fun NavGraphBuilder.chatScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = "$chatRoute/{$threadIdArg}/{$contactArg}",
        arguments = listOf(
            navArgument("threadId") { type = NavType.StringType },
            navArgument("contact") { type = NavType.StringType },
        ),
    ) {
        val threadId = it.arguments?.getString("threadId")

        ChatRoute(
            navigateBackToConversations = onBackClick,
            threadId = threadId,
        )
    }

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