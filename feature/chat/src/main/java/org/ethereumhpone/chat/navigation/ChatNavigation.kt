package org.ethereumhpone.chat.navigation

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ethereumhpone.chat.ChatRoute
import org.ethereumhpone.chat.ChatLoadingRoute
import org.ethereumhpone.chat.RequestTransactionScreenRoute
import org.ethereumhpone.chat.SendTransactionScreenRoute
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
const val chatLoadingRoute = "chat_loading_route"
private const val chatSendRouteSuffix = "send"
private const val chatRequestRouteSuffix = "request"


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

internal class ContactNameArgs(val contactName: String?) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(savedStateHandle.get<String>(contactArg)?.let { URLDecoder.decode(it, URL_CHARACTER_ENCODING) })
}

fun NavController.navigateToChatByThreadId(threadId: String) {
    val encodedThreadId = URLEncoder.encode(threadId, URL_CHARACTER_ENCODING)
    this.navigate("$chatRoute/thread/$encodedThreadId") {
        launchSingleTop = true
    }
}

fun NavController.navigateToChatByAddresses(addresses: List<String>) {
    val encodedAddresses = Uri.encode(Converters().fromStringList(addresses))
    this.navigate("$chatRoute/addresses/$encodedAddresses") {
        launchSingleTop = true
    }
}

fun NavController.navigateToChatByAddressesWithContactName(addresses: List<String>, contactName: String) {
    val encodedAddresses = Uri.encode(Converters().fromStringList(addresses))
    val encodedContactName = URLEncoder.encode(contactName, URL_CHARACTER_ENCODING)
    this.navigate("$chatRoute/addresses/$encodedAddresses?contactName=$encodedContactName") {
        launchSingleTop = true
    }
}

fun NavController.navigateToChatLoading(addresses: List<String>, contactName: String?) {
    val encodedAddresses = Uri.encode(Converters().fromStringList(addresses))
    val route = if (contactName != null) {
        val encodedContactName = URLEncoder.encode(contactName, URL_CHARACTER_ENCODING)
        "$chatLoadingRoute/$encodedAddresses?contactName=$encodedContactName"
    } else {
        "$chatLoadingRoute/$encodedAddresses"
    }
    this.navigate(route) {
        launchSingleTop = true
    }
}

fun NavController.navigateToChatSend(threadId: String) {
    val encodedThreadId = URLEncoder.encode(threadId, URL_CHARACTER_ENCODING)
    this.navigate("$chatRoute/thread/$encodedThreadId/$chatSendRouteSuffix") {
        launchSingleTop = true
    }
}

fun NavController.navigateToChatRequest(threadId: String) {
    val encodedThreadId = URLEncoder.encode(threadId, URL_CHARACTER_ENCODING)
    this.navigate("$chatRoute/thread/$encodedThreadId/$chatRequestRouteSuffix") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.chatScreen(
    navController: NavController,
    onBackClick: () -> Unit,
) {
    composable(
        route = "$chatRoute/thread/{$threadIdArg}",
        arguments = listOf(
            navArgument(threadIdArg) { type = NavType.StringType }
        ),
    ) {
        ChatRoute(
            onBackClick = onBackClick,
            onNavigateToSend = navController::navigateToChatSend,
            onNavigateToRequest = navController::navigateToChatRequest
        )
    }

    composable(
        route = "$chatRoute/addresses/{$addressesArg}?contactName={$contactArg}",
        arguments = listOf(
            navArgument(addressesArg) { type = NavType.StringType },
            navArgument(contactArg) { 
                type = NavType.StringType 
                nullable = true
                defaultValue = null
            }
        ),
    ) {
        ChatRoute(
            onBackClick = onBackClick,
            onNavigateToSend = navController::navigateToChatSend,
            onNavigateToRequest = navController::navigateToChatRequest
        )
    }
    
    composable(
        route = "$chatLoadingRoute/{$addressesArg}?contactName={$contactArg}",
        arguments = listOf(
            navArgument(addressesArg) { type = NavType.StringType },
            navArgument(contactArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        ),
    ) { 
        ChatLoadingRoute(
            onNavigateToChat = { conversationId ->
                navController.navigateToChatByThreadId(conversationId)
            }, 
            onNavigateBack = onBackClick,
            onNavigateToContacts = {
                // Navigate back to contacts app
                val context = navController.context
                val contactsIntent = context.packageManager.getLaunchIntentForPackage("org.ethereumhpone.contacts")
                if (contactsIntent != null) {
                    contactsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(contactsIntent)
                } else {
                    // Fallback: try to open contacts using system intent
                    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(fallbackIntent)
                    } catch (e: Exception) {
                        // If all fails, just go back
                        onBackClick()
                    }
                }
            }
        )
    }

    composable(
        route = "$chatRoute/thread/{$threadIdArg}/$chatSendRouteSuffix",
        arguments = listOf(
            navArgument(threadIdArg) { type = NavType.StringType }
        )
    ) {
        val threadId = it.arguments?.getString(threadIdArg).orEmpty()
        SendTransactionScreenRoute(
            onBackClick = {
                if (threadId.isNotBlank()) {
                    navController.popBackStack("$chatRoute/thread/$threadId", false)
                } else {
                    navController.popBackStack()
                }
            }
        )
    }

    composable(
        route = "$chatRoute/thread/{$threadIdArg}/$chatRequestRouteSuffix",
        arguments = listOf(
            navArgument(threadIdArg) { type = NavType.StringType }
        )
    ) {
        val threadId = it.arguments?.getString(threadIdArg).orEmpty()
        RequestTransactionScreenRoute(
            onBackClick = {
                if (threadId.isNotBlank()) {
                    navController.popBackStack("$chatRoute/thread/$threadId", false)
                } else {
                    navController.popBackStack()
                }
            }
        )
    }
}
