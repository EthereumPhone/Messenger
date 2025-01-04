package org.ethereumhpone.chat.navigation

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ethereumhpone.chat.ChatRoute
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


internal class ThreadIdArgs(val threadId: String?) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(savedStateHandle.get<String>(threadIdArg)?.let { URLDecoder.decode(it, URL_CHARACTER_ENCODING) })
}

internal class AddressesArgs(val addresses: List<String>?) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        savedStateHandle.get<String>(addressesArg)?.let {
            Converters().toStringList(Uri.decode(it))
        } ?: emptyList()
    )
}

fun NavController.navigateToChatByThreadId(threadId: String = "0L") {
    val encodedThreadId = URLEncoder.encode(threadId, URL_CHARACTER_ENCODING)
    this.navigate("$chatRoute/thread/$encodedThreadId") {
        launchSingleTop = true
    }
}

fun NavController.navigateToChatByAddresses(
    addresses: List<String> = emptyList()
) {
    val encodedAddresses = Uri.encode(Converters().fromStringList(addresses))
    this.navigate("$chatRoute/addresses/$encodedAddresses") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.chatScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = "$chatRoute/thread/{$threadIdArg}",
        arguments = listOf(
            navArgument(threadIdArg) { type = NavType.StringType }
        ),
    ) { ChatRoute(onBackClick = onBackClick) }

    composable(
        route = "$chatRoute/addresses/{$addressesArg}",
        arguments = listOf(
            navArgument(addressesArg) { type = NavType.StringType }
        ),
    ) { ChatRoute(onBackClick = onBackClick) }
}
