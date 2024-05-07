package org.ethereumhpone.data.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class MarkSeenReceiver @Inject constructor(
    private val messageRepository: MessageRepository,
) : HiltBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val threadId = intent.getLongExtra("threadId", 0)
                messageRepository.markSeen(threadId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}