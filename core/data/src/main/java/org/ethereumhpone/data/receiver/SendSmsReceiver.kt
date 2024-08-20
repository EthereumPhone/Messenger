package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.usecase.RetrySending
import javax.inject.Inject

class SendSmsReceiver @Inject constructor(
    private val retrySending: RetrySending
): HiltBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val messageId = intent.getLongExtra("id", -1L).takeIf { it >= 0 } ?: return

        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                retrySending(messageId.toString())
            } catch(e: Exception) {
                e.printStackTrace()
            } finally {
                result.finish()
            }
        }
    }
}