package org.ethereumhpone.chat


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository
): ViewModel() {

    private val threadIdArgs: ThreadIdArgs = ThreadIdArgs(savedStateHandle)


    val chatState: StateFlow<ChatUIState> = messageRepository.getMessages(threadId = threadIdArgs.threadId.toLong(),"").map {
        ChatUIState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = ChatUIState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )




}

sealed interface ChatUIState {
    object Loading : ChatUIState
    data class Success(val messages: List<Message>): ChatUIState
}
