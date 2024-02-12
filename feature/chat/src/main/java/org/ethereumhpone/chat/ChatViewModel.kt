package org.ethereumhpone.chat

import android.os.Message
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository
): ViewModel() {

    private val threadIdArgs: ThreadIdArgs = ThreadIdArgs(savedStateHandle)


    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: Flow<List<Message>> = _messages

    init {
        // Collect conversations from the repository and post values to the MutableStateFlow
        viewModelScope.launch {
            messageRepository.getMessages(threadIdArgs.threadId.toLong(),"").collect { list ->
                _messages.value = list
            }
        }
    }

}
