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

class SmsSentReceiver @Inject constructor(
    private val messageRepositoryImpl: MessageRepository
) : BroadcastReceiver() {



    override fun onReceive(context: Context?, intent: Intent?) {

        val id = intent?.getLongExtra("id", 0L)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                id?.let {
                    when(resultCode) {
                        Activity.RESULT_OK -> {
                            messageRepositoryImpl.markSent(id)
                        }
                        else -> {
                            messageRepositoryImpl.markFailed(id, resultCode)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}