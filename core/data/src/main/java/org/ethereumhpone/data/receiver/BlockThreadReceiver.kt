package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.NotificationManager
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.blocking.BlockingClient
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class BlockThreadReceiver @Inject constructor(
    private val blockingClient: BlockingClient,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val notificationManager: NotificationManager
): HiltBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)

        CoroutineScope(Dispatchers.IO).launch {
            val conversation = conversationRepository.getConversation(threadId).first()!!

            try {
                blockingClient.block(conversation.recipients.map { it.address })
                conversationRepository.markBlocked(listOf(threadId), 0, null)
                messageRepository.markRead(threadId)
                notificationManager.update(threadId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}