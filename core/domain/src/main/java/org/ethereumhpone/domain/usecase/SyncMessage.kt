package org.ethereumhpone.domain.usecase

import android.net.Uri
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject


class SyncMessage @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val syncRepository: SyncRepository

) {

    suspend operator fun invoke(uri: Uri) {
        val message = syncRepository.syncMessage(uri)
        message?.let {
            conversationRepository.updateConversations(message.threadId)
        }
    }
}