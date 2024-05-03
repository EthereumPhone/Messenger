package org.ethereumhpone.data.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class SmsDeliveredReceiver @Inject constructor(
    private val messageRepositoryImpl: MessageRepository
): HiltBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val id = intent.getLongExtra("id", 0L)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            id?.let {
                when(resultCode) {
                    // TODO notify about delivery
                    Activity.RESULT_OK -> {
                        messageRepositoryImpl.markDelivered(id)
                    }
                    else -> {
                        // TODO notify about delivery failure
                        messageRepositoryImpl.markFailed(id, resultCode)
                    }
                }
            }
            pendingResult.finish()
        }
    }
}