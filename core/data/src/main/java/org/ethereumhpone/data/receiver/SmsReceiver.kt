package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Message
import android.provider.Telephony
import android.provider.Telephony.Sms


private const val TAG = "SmsReceiver"
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Sms.Intents.SMS_RECEIVED_ACTION) {
           Sms.Intents.getMessagesFromIntent(intent)?.let { messages ->
               val subId = intent.extras?.getInt("subscription", -1) ?: -1
           }
        }
    }
}