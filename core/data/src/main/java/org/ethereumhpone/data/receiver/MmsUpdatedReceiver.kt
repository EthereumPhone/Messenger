package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject


private const val URI = "uri"

@AndroidEntryPoint
class MmsUpdatedReceiver : HiltBroadcastReceiver() {

    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var conversationRepository: ConversationRepository

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        intent.getStringExtra(URI)?.let { uriString ->

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                val message = syncRepository.syncMessage(Uri.parse(uriString))
                message?.let {
                    conversationRepository.updateConversations(message.threadId)
                }
                pendingResult.finish()
            }
        }
    }
}