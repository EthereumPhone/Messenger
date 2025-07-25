package org.ethereumhpone.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject

class XmtpBootReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        println("xmtp boot receiver")
        Log.d("xmtp STARTED broadcast", "indeed")

        try {
            NotificationChannel(
                "xmtp_channel",
                "XMTP Message Stream",
                NotificationManager.IMPORTANCE_LOW
            ).also { channel ->
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            }

            // Service removed - no longer starting foreground service
        } catch (e: Exception) {
            println("xmtp crash"+ e.localizedMessage)
        }


    }
}