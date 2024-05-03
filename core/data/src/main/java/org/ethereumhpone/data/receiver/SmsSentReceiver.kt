package org.ethereumhpone.data.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.usecase.MarkFailed
import javax.inject.Inject

@AndroidEntryPoint
class SmsSentReceiver : HiltBroadcastReceiver() {
    @Inject lateinit var messageRepositoryImpl: MessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val id = intent.getLongExtra("id", 0L)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    messageRepositoryImpl.markSent(id)
                }
                else -> {
                    messageRepositoryImpl.markFailed(id, resultCode)
                }
            }
            pendingResult.finish()
        }
    }
}

abstract class HiltBroadcastReceiver : BroadcastReceiver() {
    @CallSuper
    override fun onReceive(context: Context, intent: Intent) {}
}