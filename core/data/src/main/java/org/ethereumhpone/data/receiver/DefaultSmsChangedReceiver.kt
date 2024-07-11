package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject

@AndroidEntryPoint
class DefaultSmsChangedReceiver: HiltBroadcastReceiver() {
    @Inject lateinit var syncRepository: SyncRepository

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

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