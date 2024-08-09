package org.ethereumhpone.messenger
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.internal.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.model.ClientWrapper
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumhpone.messenger.MainActivity
import org.ethereumhpone.messenger.R
import org.ethereumphone.walletsdk.WalletSDK
import org.xmtp.android.library.Client
import org.xmtp.android.library.DecodedMessage
import javax.inject.Inject

@AndroidEntryPoint
class MyForegroundService : HiltService() {

    @Inject lateinit var clientWrapperProvider: Provider<ClientWrapper>

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

        val clientWrapper = clientWrapperProvider.get()

        if (clientWrapper.client == null) {
            println("RUN_RECEIVER: foreground onStartCommand: client is null")
        }

        val notification: Notification = NotificationCompat.Builder(this, "MyForegroundServiceChannel")
            .setContentTitle("My Service")
            .setContentText("Service is running")
            .setSmallIcon(org.ethereumhpone.chat.R.drawable.wallet)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)


        CoroutineScope(Dispatchers.IO).launch {
            clientWrapper.client?.conversations?.streamAllMessages()?.collect { message ->
                handleMessage(message)
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
}


abstract class HiltService : Service() {
    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}