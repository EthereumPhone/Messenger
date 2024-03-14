package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Message
import android.provider.Telephony
import android.provider.Telephony.Sms
import org.ethereumhpone.domain.usecase.MarkFailed
import org.ethereumhpone.domain.usecase.MarkSent
import javax.inject.Inject


private const val TAG = "SmsReceiver"
class SmsReceiver @Inject constructor(
    private val markSent: MarkSent,
    private val markFailed: MarkFailed
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Sms.Intents.SMS_RECEIVED_ACTION) {
           Sms.Intents.getMessagesFromIntent(intent)?.let { messages ->
               val subId = intent.extras?.getInt("subscription", -1) ?: -1
               val pendingResult = goAsync()


           }
        }
    }
}