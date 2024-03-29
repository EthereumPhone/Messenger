package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumhpone.data.manager.NotificationManager
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class DeleteMessagesReceiver @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val notificationManager: NotificationManager,
): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {

        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)
        val messageIds = intent.getLongArrayExtra("messageIds") ?: longArrayOf()

        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.deleteMessage(*messageIds)
            conversationRepository.updateConversations(threadId)
            notificationManager.update(threadId)
            pendingResult.finish()
        }
    }
}