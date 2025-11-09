package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.NotificationManager
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.walletsdk.WalletSDK

@AndroidEntryPoint
class XmtpRefreshReceiver : HiltBroadcastReceiver() {

    @Inject lateinit var messengerPreferences: MessengerPreferences
    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var messageDao: MessageDao

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != ACTION_REFRESH_XMTP) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = messengerPreferences.prefs.first()
                if (!prefs.useXmtp) {
                    Log.d(TAG, "XMTP disabled; skipping refresh")
                    return@launch
                }

                val unseenBefore = messageDao.getUnreadUnseenMessages().associateBy { it.id }

                if (xmtpClientManager.clientState.value != XmtpClientManager.ClientState.Ready) {
                    Log.d(TAG, "XMTP client not ready; attempting creation")
                    xmtpClientManager.createClient(walletSDK, context.applicationContext)
                    xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
                }

                syncRepository.syncXmtp()

                val unseenAfter = messageDao.getUnreadUnseenMessages()
                val newMessages = unseenAfter.filterNot { unseenBefore.containsKey(it.id) }

                if (newMessages.isEmpty()) {
                    Log.d(TAG, "No new XMTP messages found")
                    return@launch
                }

                val latestThreadId = newMessages.maxByOrNull { it.dateSent }?.threadId ?: "0"
                notificationManager.createNotificationChannel(latestThreadId)
                notificationManager.update(latestThreadId)
                Log.d(TAG, "Issued notification for XMTP thread $latestThreadId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh XMTP messages", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "XmtpRefreshReceiver"
        const val ACTION_REFRESH_XMTP = "org.ethereumhpone.messenger.action.REFRESH_XMTP"
    }
}


