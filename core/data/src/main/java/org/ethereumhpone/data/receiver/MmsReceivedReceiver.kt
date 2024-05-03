package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.CallSuper
import com.klinker.android.send_message.MmsReceivedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.NotificationManagerImpl
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
    private val notificationManager: NotificationManagerImpl
) : HiltMmsReceivedReceiver() {

    override fun onMessageReceived(messageUri: Uri?) {
        super.onMessageReceived(messageUri)
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

abstract class HiltMmsReceivedReceiver : MmsReceivedReceiver() {
    @CallSuper
    override fun onReceive(context: Context, intent: Intent) {}

    @CallSuper
    override fun onMessageReceived(messageUri: Uri?) {}
}
