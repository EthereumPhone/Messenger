package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.klinker.android.send_message.MmsReceivedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.NotificationManager
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.domain.blocking.BlockingClient
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject

class MmsReceivedReceiver @Inject constructor(
    private val syncRepository: SyncRepository,
    private val conversationRepository: ConversationRepository,
    private val activeConversationManager: ActiveConversationManager,
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
                val message = syncRepository.syncMessage(uri) ?: return@launch

                if (activeConversationManager.getActiveConversation() == message.threadId) {
                    messageRepository.markRead(message.threadId)
                }

                val action = blockingClient.shouldBlock(message.address)

                if (action is BlockingClient.Action.Block) {
                    messageRepository.deleteMessage(message.id)
                }

                when (action) {
                    is BlockingClient.Action.Block -> {
                        messageRepository.markRead(message.threadId)
                        conversationRepository.markBlocked(listOf(message.threadId), 0 , action.reason)
                    }
                    is BlockingClient.Action.Unblock -> conversationRepository
                        .markUnblocked(message.threadId)
                    else -> Unit
                }

                conversationRepository.updateConversations(message.threadId)
                conversationRepository.getOrCreateConversation(message.threadId)
                    .filterNotNull()
                    .filter { !it.blocked }
                    .map {
                        if (it.archived) conversationRepository.markUnarchived(it.id)
                        notificationManager.update(it.id)
                    }
                pendingResult.finish()
            }
        }
    }
}