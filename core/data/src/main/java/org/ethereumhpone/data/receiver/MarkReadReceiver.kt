package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.NotificationManager
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class MarkReadReceiver @Inject constructor(
    private val messageRepository: MessageRepository,
    private val notificationManager: NotificationManager
): HiltBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)

        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.markRead(threadId)
            notificationManager.update(threadId)
        }
    }
}