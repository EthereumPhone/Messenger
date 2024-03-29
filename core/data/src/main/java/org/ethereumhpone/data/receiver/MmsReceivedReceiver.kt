package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.klinker.android.send_message.MmsReceivedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.NotificationManager
import org.ethereumhpone.domain.blocking.BlockingClient
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject

class MmsReceivedReceiver @Inject constructor(
    private val syncRepository: SyncRepository,
    private val activeConversationRepository: ConversationRepository,
    private val blockingClient: BlockingClient,
    private val messageRepository: MessageRepository,
    private val notificationManager: NotificationManager
) : MmsReceivedReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }

    override fun onMessageReceived(messageUri: Uri?) {
        messageUri?.let { uri ->
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                val message = syncRepository.syncMessage(uri)
                message?.let {
                    if (activeConversationRepository.getConversation().first() == message.threadId) {

                    }

                }
            }
        }

    }

}