package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Message
import android.provider.Telephony
import android.provider.Telephony.Sms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.usecase.MarkFailed
import org.ethereumhpone.domain.usecase.MarkSent
import org.ethereumhpone.domain.usecase.ReceiveSms
import javax.inject.Inject


private const val TAG = "SmsReceiver"
class SmsReceiver @Inject constructor(
    private val recieveMessage: ReceiveSms
) : HiltBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if(intent.action == Sms.Intents.SMS_RECEIVED_ACTION) {
           Sms.Intents.getMessagesFromIntent(intent)?.let { messages ->
               val subId = intent.extras?.getInt("subscription", -1) ?: -1
               val pendingResult = goAsync()

               CoroutineScope(Dispatchers.IO).launch {
                   recieveMessage(subId, messages)
                   pendingResult.finish()
               }
           }
        }
    }
}