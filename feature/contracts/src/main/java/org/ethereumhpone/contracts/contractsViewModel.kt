package org.ethereumhpone.contracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    private val xmtpClientManager: XmtpClientManager,
    private val syncRepository: SyncRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
): ViewModel() {

    val conversationState: StateFlow<ConversationUIState> = conversationRepository.getConversations()
        .flowOn(Dispatchers.IO)
        .map { conversations ->
            conversations.forEach {
                println("DEBUGGGGG: ${it.lastMessage?.body}, ${it.unknown}")
            }
            val filteredConversations = conversations.filter { it.date > 0 }.sortedBy { it.date }.reversed() // Filter out conversations with unknown set to true
            ConversationUIState.Success(filteredConversations)
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = ConversationUIState.Empty,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    val showHiddenButton: StateFlow<Boolean> = conversationRepository.getConversations()
        .flowOn(Dispatchers.IO)
        .map { conversations ->
            conversations.any { it.unknown } // Check if any conversation has unknown set to true
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = false, // False by default
            started = SharingStarted.WhileSubscribed(5_000)
        )


    val contacts: Flow<List<Contact>> = contactRepository.getContacts()

    fun setConversationAsRead(conversationId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            conversationRepository.markRead(conversationId)
        }
    }

    fun setConversationAsAccepted(conversationId: Long, address: String) {
       CoroutineScope(Dispatchers.IO).launch {
           val clientState = xmtpClientManager.clientState.first {
               it == XmtpClientManager.ClientState.Ready
           }

           if (clientState == XmtpClientManager.ClientState.Ready) {
               xmtpClientManager.client.contacts.allow(listOf(address))
           }
           conversationRepository.markAccepted(conversationId)
       }
    }

}

sealed interface ConversationUIState {
    object Loading : ConversationUIState
    object Empty : ConversationUIState
    data class Success(val conversations: List<Conversation>): ConversationUIState
}
