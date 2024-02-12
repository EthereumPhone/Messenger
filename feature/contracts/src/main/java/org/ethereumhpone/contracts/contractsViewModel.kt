package org.ethereumhpone.contracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import javax.inject.Inject


@HiltViewModel
class ContactViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
): ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: Flow<List<Conversation>> = _conversations

    init {
        // Collect conversations from the repository and post values to the MutableStateFlow
        viewModelScope.launch {
            conversationRepository.getConversations().collect { conversationsList ->
                _conversations.value = conversationsList
            }
        }
    }


}
