package org.ethereumhpone.messenger
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class MyBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "org.ethereumhpone.messenger.RUN_RECEIVER") {
            println("MyBootReceiver: onReceive: RUN_RECEIVER")
            val serviceIntent = Intent(context, MyForegroundService::class.java)
            //context.startForegroundService(serviceIntent)
        }
    }
}
