package org.ethereumhpone.domain.usecase

import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class MarkSent @Inject constructor(
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(id: Long) {
        messageRepository.markSent(id)
    }
}