package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

@AndroidEntryPoint
class XmtpSetupReceiver : HiltBroadcastReceiver() {

    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var xmtpClientManager: XmtpClientManager

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != ACTION_SETUP_XMTP) return

        // Use goAsync to allow asynchronous work if needed
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                xmtpClientManager.createClient(walletSDK, context.applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SETUP_XMTP = "org.ethereumhpone.messenger.action.SETUP_XMTP"
    }
}