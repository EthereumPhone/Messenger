package org.ethereumhpone.messenger
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.CallSuper
import androidx.compose.runtime.collectAsState
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.domain.manager.NetworkManager
import org.web3j.abi.datatypes.Bool
import org.xmtp.android.library.DecodedMessage
import org.xmtp.android.library.Util.Companion.envelopeFromFFi
import org.xmtp.android.library.messages.Topic
import uniffi.xmtpv3.FfiEnvelope
import uniffi.xmtpv3.FfiV2SubscribeRequest
import uniffi.xmtpv3.FfiV2Subscription
import uniffi.xmtpv3.FfiV2SubscriptionCallback
import uniffi.xmtpv3.NoPointer
import javax.inject.Inject

@AndroidEntryPoint
class MyForegroundService : HiltService() {

    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var networkManager: NetworkManager

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val client = xmtpClientManager.client

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "MyForegroundServiceChannel",
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        println("RUN_RECEIVER: foreground onCreate")
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)
        println("RUN_RECEIVER: foreground onStartCommand")


        val notification: Notification = NotificationCompat.Builder(this, "MyForegroundServiceChannel")
            .setContentTitle("My Service")
            .setContentText("Service is running")
            .setSmallIcon(org.ethereumhpone.chat.R.drawable.wallet)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        // service only started once the client is ready




        coroutineScope.launch {
            var collectionJob: Job? = null


            networkManager.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d("STARTED AGAIN", "LETS go")
                    try {
                        collectionJob = launch {
                            xmtpClientManager.client.conversations.streamAllMessages()
                                .cancellable()
                                .takeWhile { isOnline }
                                .collect {
                                    Log.d("Collected", "Collected")
                                    handleMessage(it)
                                }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                } else {
                    collectionJob?.cancel()
                    collectionJob = null
                }
            }
        }

        // Keep the service running
        return START_STICKY
    }

    private fun handleMessage(message: DecodedMessage) {
        println("RUN_RECEIVER: handleMessage: $message")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun streamMessageAfterEpoch(timestamp: Long) {

    }

}


abstract class HiltService : Service() {
    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
