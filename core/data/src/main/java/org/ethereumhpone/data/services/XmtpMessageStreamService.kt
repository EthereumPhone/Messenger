package org.ethereumhpone.data.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ethereumhpone.data.R
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

@AndroidEntryPoint
class XmtpMessageStreamService : Service() {

    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var context: Context
    @Inject lateinit var messengerPreferences: MessengerPreferences

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // nothing else needed if you use Hilt's @AndroidEntryPoint
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {

            println("xmtp started forground service")


            // 1) build notification (must exist before you call startForeground)
            val notification = NotificationCompat.Builder(this, "xmtp_channel")
                .setContentTitle("XMTP Stream")
                .setContentText("Listening for messages…")
                .setSmallIcon(R.drawable.ic_sms_light)
                .setOngoing(true)
                .build()


            // 2) start in foreground
            //startForeground(42, notification)

            // 3) kick off your coroutine
            /*
            coroutineScope.launch {
                try {
                    println("xmtp starting stream service")
                    syncRepository.startStream()
                } catch (e: Exception) {
                    println("xmtp stream error: ${e.message}")
                    e.printStackTrace()
                }
            }
             */

            // if process dies, Android will recreate service and redeliver the intent
            return START_STICKY
        } catch (e: Exception) {
            e.printStackTrace()
            return START_NOT_STICKY
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the coroutine scope to clean up resources
        coroutineScope.launch { }
    }
}