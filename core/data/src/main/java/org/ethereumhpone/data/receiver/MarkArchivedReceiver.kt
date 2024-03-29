package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.NotificationManager
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class MarkArchivedReceiver @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val notificationManager: NotificationManager
    ) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)

        CoroutineScope(Dispatchers.IO).launch {
            conversationRepository.markArchived(threadId)
            messageRepository.markRead(threadId)
            notificationManager.update(threadId)
            pendingResult.finish()
        }
    }
}