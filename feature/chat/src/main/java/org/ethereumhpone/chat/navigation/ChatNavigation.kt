package org.ethereumhpone.chat.navigation

import android.net.Uri
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
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.util.Converters
import java.net.URLDecoder
import java.net.URLEncoder


private val URL_CHARACTER_ENCODING = Charsets.UTF_8.name()

@VisibleForTesting
internal const val threadIdArg = "threadId"

@VisibleForTesting
internal const val addressesArg = "addresses"

@VisibleForTesting
internal const val contactArg = "contact"


const val chatGraphRoutePattern = "chat_graph"
const val chatRoute = "chat_route"


internal class ThreadIdArgs(val threadId: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(URLDecoder.decode(checkNotNull(savedStateHandle[threadIdArg]), URL_CHARACTER_ENCODING))
}

internal class AddressesArgs(val addresses: List<String>) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(Converters().toStringList(Uri.decode(checkNotNull(savedStateHandle[addressesArg]))))}

fun NavController.navigateToChat(
    threadId: String = "0L",
    addresses: List<String> = emptyList()
) {
    val encodedThreadId = URLEncoder.encode(threadId, URL_CHARACTER_ENCODING)
    //val encodedAddresses = URLEncoder.encode(Converters().fromStringList(addresses), URL_CHARACTER_ENCODING)
    val encodedAddresses = Uri.encode(Converters().fromStringList(addresses))

    this.navigate("$chatRoute/$encodedThreadId/$encodedAddresses") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.chatScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = "$chatRoute/{$threadIdArg}/{$addressesArg}",
        arguments = listOf(
            navArgument("threadId") { type = NavType.StringType },
            navArgument("addresses") { type = NavType.StringType },
        ),
    ) {
        val threadId = it.arguments?.getString("threadId")

        ChatRoute(
            navigateBackToConversations = onBackClick,
        )
    }
}