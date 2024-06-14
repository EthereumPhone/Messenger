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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val syncRepository: SyncRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
): ViewModel() {

    val conversationState: StateFlow<ConversationUIState> = conversationRepository.getConversations()
        .map(ConversationUIState::Success)
        .stateIn(
        scope = viewModelScope,
        initialValue = ConversationUIState.Empty,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val contacts: Flow<List<Contact>> = contactRepository.getContacts()

    fun setConversationAsRead(conversationId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            conversationRepository.markRead(conversationId)
        }
    }

}

sealed interface ConversationUIState {
    object Loading : ConversationUIState
    object Empty : ConversationUIState
    data class Success(val conversations: List<Conversation>): ConversationUIState
}
