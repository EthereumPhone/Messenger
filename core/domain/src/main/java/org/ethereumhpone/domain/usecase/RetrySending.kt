package org.ethereumhpone.domain.usecase


import kotlinx.coroutines.flow.filterNotNull
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class RetrySending @Inject constructor(
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(id: String) {
        messageRepository.markSending(id)
        messageRepository.getMessage(id)
            .filterNotNull()
            .collect { message ->
            when(message.isSms()) {
                true -> messageRepository.sendSms(message)
                false -> messageRepository.resendMms(message)
            }
        }
    }
}