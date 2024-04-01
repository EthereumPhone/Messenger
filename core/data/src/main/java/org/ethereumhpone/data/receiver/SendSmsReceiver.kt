package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.usecase.retrySending
import javax.inject.Inject

class SendSmsReceiver @Inject constructor(
    private val retrySending: retrySending
): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val messageId = intent.getLongExtra("id", -1L).takeIf { it >= 0 } ?: return

        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            retrySending(messageId)
            result.finish()
        }
    }
}