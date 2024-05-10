package org.ethereumhpone.chat


import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.navigation.AddressesArgs
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.database.model.Conversation
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
    private val contactRepository: ContactRepository,
    private val sendMessageUseCase: SendMessage,
    private val permissionManager: PermissionManager
): ViewModel() {

    private val threadId = ThreadIdArgs(savedStateHandle).threadId.toLong()
    private val addresses = AddressesArgs(savedStateHandle).addresses

    val conversationState = merge(
        conversationRepository.getConversation(threadId), // initial Conversation
        selectedConversationState(addresses, conversationRepository)
    ).stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val messagesState = conversationState
        .filterNotNull()
        .flatMapLatest {
            messageRepository.getMessages(it.id).map(MessagesUiState::Success)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MessagesUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val recipientState = conversationState
        .filterNotNull()
        .map {
            if (it.recipients.isNotEmpty()) {
                it.recipients[0]
            } else {

                Recipient(address = addresses[0])
            }
    }.stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5_000)
        )




    fun sendMessage(messageBody: String, attachments: List<Attachment>) {
        //if(!permissionManager.isDefaultSms()) return
        if(!permissionManager.hasSendSms()) {
            //TODO: add request permission
            return
        }

        val subId = -1 //TODO: Add sunscroptionId logic


        // this sends a message for an existing conversation
        conversationState.value?.let { convo ->
            // send message to convo with only one recipient
            if(convo.recipients.size == 1) {
                val address = convo.recipients.map { it.address }

                viewModelScope.launch {
                    sendMessageUseCase(subId, convo.id, address, messageBody, attachments)
                }
            }
        }

        //TODO: Create a new conversation with one address


    }
}

/**
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun selectedConversationState(
    addresses: List<String>,
    conversationRepository: ConversationRepository
): Flow<Conversation?> =
    conversationRepository.getOrCreateConversation(addresses).flatMapLatest { convo ->
        val threadId = convo?.id ?: 0

        if (threadId > 0) {
            // If the threadID exists in roomDB or ContentProvider
            conversationRepository.getConversation(threadId)
        } else {
            // Otherwise, monitor conversations until one is created
            conversationRepository.getConversations().map {
                val actualThreadId =
                    conversationRepository.getOrCreateConversation(addresses).first()?.id ?: 0

                when (actualThreadId) {
                    0L -> Conversation()
                    else -> conversationRepository.getConversation(actualThreadId).first()
                }
            }
        }
}

sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(val messages: List<Message>): MessagesUiState
}
