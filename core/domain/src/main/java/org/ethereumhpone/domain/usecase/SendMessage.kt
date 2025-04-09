package org.ethereumhpone.domain.usecase

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumphone.model.Reaction
import javax.inject.Inject

class SendMessage @Inject constructor(
    private val context: Context,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment> = emptyList(),
        reaction: Reaction?
    ) {
        messageRepository.sendMessage(threadId, body, replyReference, attachments, reaction)
    }
}