package org.ethereumhpone.domain.usecase

import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumphone.model.Reaction
import org.xmtp.android.library.Conversation
import javax.inject.Inject

class SendMessageWithConversation @Inject constructor(
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(
        xmtpConversation: Conversation,
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment> = emptyList(),
        reaction: Reaction?
    ) {
        messageRepository.sendMessageWithConversation(xmtpConversation, threadId, body, replyReference, attachments, reaction)
    }
}