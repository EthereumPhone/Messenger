package org.ethereumhpone.contracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
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
    private val conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    private val xmtpClientManager: XmtpClientManager,
    private val ensResolver: ENS,
    private val syncRepository: SyncRepository,
): ViewModel() {

    // Keep the UI in a `Loading` state until we have at least one conversation. This prevents the
    // temporary "No conversations" screen from flashing when data is still being fetched/synced.
    val conversationState: StateFlow<ConversationUIState> = conversationRepository.getConversations()
        .map { conversations ->
            if (conversations.isEmpty()) {
                ConversationUIState.Loading
            } else {
                ConversationUIState.Success(conversations)
            }
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

        }
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

}

sealed interface ConversationUIState {
    object Loading : ConversationUIState
    object Empty : ConversationUIState
    data class Success(val conversations: List<Conversation>): ConversationUIState
}
