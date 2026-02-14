package org.ethereumhpone.contracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.domain.manager.NetworkManager
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.model.Conversation
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import javax.inject.Inject
import kotlin.math.acos

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    private val xmtpClientManager: XmtpClientManager,
    private val ensResolver: ENS,
    private val syncRepository: SyncRepository,
    private val networkManager: NetworkManager,
): ViewModel() {

    // Expose network connectivity status separately
    val isOnline: StateFlow<Boolean> = networkManager.isOnline
        .stateIn(
            scope = viewModelScope,
            initialValue = true,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    // Track locally hidden (deleted) conversations to avoid reappearing after remote sync.
    private val hiddenConversationIds = MutableStateFlow(
        savedStateHandle.get<List<String>>("hidden_conversation_ids")?.toSet() ?: emptySet()
    )

    // Keep the UI in a `Loading` state until we have at least one conversation. This prevents the
    // temporary "No conversations" screen from flashing when data is still being fetched/synced.
    val conversationState: StateFlow<ConversationUIState> =
        combine(
            conversationRepository.getConversations(),
            hiddenConversationIds,
        ) { conversations, hiddenIds ->
            val visible = conversations.filterNot { hiddenIds.contains(it.id) }
            if (visible.isEmpty()) ConversationUIState.Empty else ConversationUIState.Success(visible)
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = ConversationUIState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun setConversationAsRead(conversationId: String, seen: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationRepository.updateSeenConversation(conversationId, seen)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationRepository.deleteConversation(conversationId)
            // Hide locally so it doesn't reappear if remote sync re-inserts it
            val updated = hiddenConversationIds.value + conversationId
            hiddenConversationIds.value = updated
            savedStateHandle["hidden_conversation_ids"] = updated.toList()
        }
    }

    fun unhideConversation(conversationId: String) {
        // Allow showing the conversation again (e.g., when user starts it from New Conversation)
        val updated = hiddenConversationIds.value - conversationId
        hiddenConversationIds.value = updated
        savedStateHandle["hidden_conversation_ids"] = updated.toList()
    }

    fun updateConsentState(conversationId: String, address: Boolean) {
        TODO()
    }

    fun resolveENS(ensName: String): String {
        if (ENSName(ensName).isPotentialENSDomain()) {
            val address = ensResolver.getAddress(ENSName(ensName))
            return address?.toString() ?: ""
        } else {
            return ""
        }
    }

    fun setConversationArchived(conversationId: String, archived: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            conversationRepository.updateArchivedConversation(conversationId, archived)
        }
    }

    fun syncContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.syncContacts()
        }
    }

}

sealed interface ConversationUIState {
    object Loading : ConversationUIState
    object Empty : ConversationUIState
    data class Success(val conversations: List<Conversation>): ConversationUIState
}
