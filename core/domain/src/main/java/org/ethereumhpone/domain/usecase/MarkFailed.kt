package org.ethereumhpone.domain.usecase

import org.ethereumhpone.domain.manager.NotificationManager
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class MarkFailed @Inject constructor(
    private val messageRepository: MessageRepository,
    private val notificationManager: NotificationManager
) {

    suspend operator fun invoke(id: Long, resultCode: Int) {
        messageRepository.markFailed(id, resultCode)
        notificationManager.notifyFailed(id)
    }
}