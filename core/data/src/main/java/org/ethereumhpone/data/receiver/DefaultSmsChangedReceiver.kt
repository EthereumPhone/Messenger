package org.ethereumhpone.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject

class DefaultSmsChangedReceiver @Inject constructor(
    private val syncRepository: SyncRepository
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {

        if (intent.getBooleanExtra(Telephony.Sms.Intents.EXTRA_IS_DEFAULT_SMS_APP, false)) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    syncRepository.syncMessages()
                } finally {
                    pendingResult.finish()
                }

            }
        }




    }
}