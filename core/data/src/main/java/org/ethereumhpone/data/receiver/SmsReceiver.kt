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

        if(intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()




        }
    }
}