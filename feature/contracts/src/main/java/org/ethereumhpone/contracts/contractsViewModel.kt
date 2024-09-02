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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.contracts.ui.isAddress
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.domain.model.XMTPConversationDB
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    private val xmtpClientManager: XmtpClientManager,
    private val xmtpConversationDB: XMTPConversationDB,
    private val syncRepository: SyncRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
): ViewModel() {

    val conversationState: StateFlow<ConversationUIState> = combine(
        conversationRepository.getConversations(),
        xmtpConversationDB.getAllConversations()
    ) { repoConversations, xmtpConversations ->
        // Combine both lists
        val filteredConvos = repoConversations.filter { !xmtpConversationDB.isConversationInXMTP(it.id.toString()) }
        val allConversations = (filteredConvos + xmtpConversations).distinctBy { it.id }

        // Filter and sort the conversations
        val filteredConversations = allConversations
            .filter { it.date > 0 } // Filter out conversations with date <= 0
            .sortedBy { it.date }   // Sort by date
            .reversed()             // Reverse to have most recent first

        ConversationUIState.Success(filteredConversations)
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            initialValue = ConversationUIState.Empty,
            started = SharingStarted.WhileSubscribed(5_000)
        )


    val contacts: Flow<List<Contact>> = contactRepository.getContacts()

    fun setConversationAsRead(conversationId: Long) {
        if (xmtpConversationDB.isConversationInXMTP(conversationId.toString())) {
            xmtpConversationDB.updateConversationReadStatus(conversationId.toString(), true)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                conversationRepository.markRead(conversationId)
            }
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

           xmtpConversationDB.markConversationAccepted(conversationId.toString())
       }
    }

}

sealed interface ConversationUIState {
    object Loading : ConversationUIState
    object Empty : ConversationUIState
    data class Success(val conversations: List<Conversation>): ConversationUIState
}
