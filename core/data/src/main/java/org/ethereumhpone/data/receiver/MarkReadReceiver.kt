package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.NotificationManagerImpl
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class MarkReadReceiver @Inject constructor(
    private val messageRepository: MessageRepository,
    private val notificationManager: NotificationManagerImpl
): HiltBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        goAsync()
        val threadId = intent.getLongExtra("threadId", 0)

        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.markRead(threadId)
            notificationManager.update(threadId)
        }
    }
}