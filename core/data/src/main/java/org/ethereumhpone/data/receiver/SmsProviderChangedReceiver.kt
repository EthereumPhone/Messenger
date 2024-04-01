package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.usecase.SyncMessage
import javax.inject.Inject

class SmsProviderChangedReceiver @Inject constructor(
    private val syncMessage: SyncMessage
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val uri = intent.data ?: return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            syncMessage(uri)
            pendingResult.finish()
        }
    }
}