package org.ethereumhpone.domain.usecase

import android.telephony.SmsMessage
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.ethereumhpone.domain.blocking.BlockingClient
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.manager.NotificationManager
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class ReceiveSms @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val blockingClient: BlockingClient,
    private val notificationManager: NotificationManager,
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(subId: Int, messages: Array<SmsMessage>) {
        if(messages.isNotEmpty()) return

        val address = messages[0].displayOriginatingAddress
        val action = blockingClient.shouldBlock(address)

        if (action is BlockingClient.Action.Block) return

        val time = messages[0].timestampMillis
        val body = messages
            .mapNotNull { messages -> messages.displayMessageBody }
            .reduce { body, new -> body + new  }

        val message = messageRepository.insertReceivedSms(subId, address, body, time)

        when (action) {
            is BlockingClient.Action.Block -> {
                messageRepository.markRead(message.threadId)
                conversationRepository.markBlocked(listOf(message.threadId), 0, action.reason)
            }
            is BlockingClient.Action.Unblock -> conversationRepository.markUnblocked(message.threadId)
            else -> Unit
        }

        conversationRepository.updateConversations(message.threadId)

        val conversation = conversationRepository.getOrCreateConversation(message.threadId).first()
        conversation?.let {
            if(!it.blocked) {
                if(it.archived) conversationRepository.markUnarchived(it.id)
                notificationManager.update(it.id)
            }
        }


    }
}