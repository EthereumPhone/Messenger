package org.ethereumhpone.data.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Binder
import android.os.Process
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.domain.manager.NotificationManager
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumhpone.ipc.IMsgSyncService
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.datastore.MessengerPreferences

@AndroidEntryPoint
class MsgSyncService : Service() {

    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var messageDao: MessageDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val binder = object : IMsgSyncService.Stub() {
        override fun syncNow() {
            val callerUid = Binder.getCallingUid()
            if (callerUid != Process.SYSTEM_UID) {
                android.util.Log.w("MsgSyncService", "Denied syncNow from uid=$callerUid")
                return
            }
            scope.launch {
                try {
                    performSync()
                } catch (_: Throwable) {
                    // swallow to not crash system binder caller
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MsgSyncEntryPoint {
        fun messengerPreferences(): MessengerPreferences
    }

    private suspend fun performSync() {
        // Only proceed if XMTP is enabled by the user (resolve via EntryPoint to avoid service field injection issues)
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            MsgSyncEntryPoint::class.java
        )
        val prefs = entryPoint.messengerPreferences().prefs.first()
        if (!prefs.useXmtp) return

        // Create client if needed then wait until ready
        if (xmtpClientManager.clientState.value != org.ethereumhpone.data.manager.XmtpClientManager.ClientState.Ready) {
            xmtpClientManager.createClient(walletSDK, applicationContext)
            xmtpClientManager.clientState.first { it == org.ethereumhpone.data.manager.XmtpClientManager.ClientState.Ready }
        }

        // Diff unseen messages before/after to detect new items
        val before = messageDao.getUnreadUnseenMessages().associateBy { it.id }

        // Perform the actual sync
        syncRepository.syncXmtp()

        // Determine if any new unseen messages landed
        val after = messageDao.getUnreadUnseenMessages()
        val newMessages = after.filterNot { before.containsKey(it.id) }
        if (newMessages.isEmpty()) return

        // Notify - use the latest thread id; NotificationManagerImpl will render all unread
        val latestThreadId = newMessages.maxByOrNull { it.dateSent }?.threadId ?: "0"
        notificationManager.createNotificationChannel(latestThreadId)
        notificationManager.update(latestThreadId)
    }
}

