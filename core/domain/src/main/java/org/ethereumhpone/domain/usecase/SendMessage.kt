package org.ethereumhpone.domain.usecase

import android.content.Context
import kotlinx.coroutines.flow.first
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class SendMessage @Inject constructor(
    private val context: Context,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment> = listOf(),
    ) {
        if(addresses.isEmpty()) return

        val newThreadId = when(threadId) {
            0L -> TelephonyCompat.getOrCreateThreadId(context, addresses.toSet())
            else -> threadId
        }

        messageRepository.sendMessage(subId, newThreadId, addresses, body, attachments)

        val conversationId = when(threadId) {
            0L -> conversationRepository.getOrCreateConversation(addresses).first()?.id
            else -> threadId
        }

        conversationId?.let {
            conversationRepository.updateConversations(it)
            conversationRepository.markUnarchived(it)
        }
    }
}