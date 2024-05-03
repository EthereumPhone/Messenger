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
import org.ethereumhpone.database.util.Converters
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.usecase.SendMessage
import java.net.URLDecoder
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val sendMessage: SendMessage,
    private val permissionManager: PermissionManager
): ViewModel() {

    private val threadIdArgs: ThreadIdArgs = ThreadIdArgs(savedStateHandle)

    val contact = if (savedStateHandle.get<String>("contact") != null) Converters().fromContact(URLDecoder.decode(savedStateHandle.get<String>("contact"), Charsets.UTF_8.name())) else null

    val chatState: StateFlow<ChatUIState> = messageRepository.getMessages(threadId = threadIdArgs.threadId.toLong(),"").flowOn(Dispatchers.IO).map {
        ChatUIState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = ChatUIState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val conversation = conversationRepository.getConversation(threadIdArgs.threadId.toLong())
        .stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5_000)
        )



    val recipient: StateFlow<Recipient?> = conversation.map {
        it?.recipients?.get(0) ?: contact?.let { realContact ->
            Recipient(
                id = 0L,
                contact = realContact,
                lastUpdate = realContact.lastUpdate,
                address = realContact.numbers[0].address
            )
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    fun sendMessage(messageBody: String, attachments: List<Attachment>) {
        //if(!permissionManager.isDefaultSms()) return
        if(!permissionManager.hasSendSms()) return

        val subId = -1 //TODO: Add sunscroptionId logic


        // this sends a message for an existing conversation
        conversation.value?.let {
            // send message to convo with only one recipient
            if(it.recipients.size == 1) {
                val address = it.recipients.map { it.address }

                viewModelScope.launch {
                    sendMessage(subId, it.id, address, messageBody, attachments)
                }
            }
        }

        //TODO: Create a new conversation with one address


    }


}

sealed interface ChatUIState {
    object Loading : ChatUIState
    data class Success(val messages: List<Message>): ChatUIState
}
