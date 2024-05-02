package org.ethereumhpone.chat


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
): ViewModel() {

    private val threadIdArgs: ThreadIdArgs = ThreadIdArgs(savedStateHandle)

    private var addressNoContact: MutableStateFlow<String> = MutableStateFlow("")


    val chatState: StateFlow<ChatUIState> = messageRepository.getMessages(threadId = threadIdArgs.threadId.toLong(),"").flowOn(Dispatchers.IO).map {
        ChatUIState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = ChatUIState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val recipient: StateFlow<Recipient?> = conversationRepository.getConversation(threadIdArgs.threadId.toLong()).flowOn(Dispatchers.IO).map {
        it?.recipients?.get(0) ?: Recipient(
            id = 0,
            address = addressNoContact.value
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(5_000)
    )


}

sealed interface ChatUIState {
    object Loading : ChatUIState
    data class Success(val messages: List<Message>): ChatUIState
}
