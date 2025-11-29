package org.ethereumhpone.data.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.NotificationManager
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumhpone.ipc.IMsgSyncService
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

/**
 * MsgSyncService - Bound service for OS-level XMTP message synchronization.
 *
 * This service is bound by the OS-level XMTPNotificationsService which calls
 * syncNow() every 5 minutes to fetch new messages from the XMTP network.
 *
 * Key design decisions:
 * 1. Uses AIDL for IPC with the OS service
 * 2. Acquires a partial wake lock during sync to prevent CPU sleep
 * 3. Uses a 55-second timeout to prevent ANR (OS has 60s wake lock)
 * 4. Handles all edge cases: no network, client not ready, etc.
 */
@AndroidEntryPoint
class MsgSyncService : Service() {

    companion object {
        private const val TAG = "MsgSyncService"
        private const val SYNC_TIMEOUT_MS = 55_000L // 55 seconds (OS has 60s wake lock)
        private const val WAKE_LOCK_TAG = "MsgSyncService:sync"
    }

    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var messengerPreferences: MessengerPreferences
    @Inject lateinit var notificationManager: NotificationManager

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Uncaught exception in service scope", throwable)
        }
    )

    private val binder = object : IMsgSyncService.Stub() {
        override fun syncNow() {
            Log.i(TAG, "syncNow() called by OS service")
            performSync()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "MsgSyncService created")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "onBind() - returning AIDL binder")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "onUnbind()")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.i(TAG, "MsgSyncService destroyed")
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Performs the actual XMTP sync operation.
     * This is called from the AIDL binder when the OS triggers syncNow().
     */
    private fun performSync() {
        serviceScope.launch {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                setReferenceCounted(false)
            }

            try {
                wakeLock.acquire(SYNC_TIMEOUT_MS + 5000) // Extra 5s buffer
                Log.i(TAG, "Wake lock acquired, starting sync...")

                val result = withTimeoutOrNull(SYNC_TIMEOUT_MS) {
                    doSync()
                }

                if (result == null) {
                    Log.w(TAG, "Sync timed out after ${SYNC_TIMEOUT_MS}ms")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync", e)
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Log.i(TAG, "Wake lock released")
                }
            }
        }
    }

    /**
     * The core sync logic that:
     * 1. Checks if XMTP is enabled
     * 2. Ensures the XMTP client is ready
     * 3. Calls syncNow() on the repository to fetch new messages
     * 4. Triggers notifications for any new unread messages
     */
    private suspend fun doSync(): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if XMTP is enabled in preferences
                val prefs = messengerPreferences.prefs.first()
                if (!prefs.useXmtp) {
                    Log.i(TAG, "XMTP is disabled in preferences, skipping sync")
                    return@withContext SyncResult.Skipped("XMTP disabled")
                }

                // Ensure the XMTP client is initialized
                val clientState = xmtpClientManager.clientState.value
                if (clientState != XmtpClientManager.ClientState.Ready) {
                    Log.i(TAG, "XMTP client not ready (state: $clientState), attempting to initialize...")
                    
                    // Try to create the client
                    xmtpClientManager.createClient(walletSDK, this@MsgSyncService)
                    
                    // Wait for client to become ready (with timeout)
                    val readyState = withTimeoutOrNull(30_000L) {
                        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
                    }
                    
                    if (readyState == null) {
                        Log.w(TAG, "XMTP client failed to initialize within timeout")
                        return@withContext SyncResult.Error("Client initialization timeout")
                    }
                }

                Log.i(TAG, "XMTP client ready, starting network sync...")

                // Call the repository's syncNow method which does the actual work
                val newMessageCount = syncRepository.syncNow()

                Log.i(TAG, "Sync completed: $newMessageCount new messages")

                if (newMessageCount > 0) {
                    return@withContext SyncResult.Success(newMessageCount)
                } else {
                    return@withContext SyncResult.NoNewMessages
                }

            } catch (e: Exception) {
                Log.e(TAG, "Sync failed with exception", e)
                return@withContext SyncResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    private sealed class SyncResult {
        data class Success(val newMessageCount: Int) : SyncResult()
        object NoNewMessages : SyncResult()
        data class Skipped(val reason: String) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }
}

