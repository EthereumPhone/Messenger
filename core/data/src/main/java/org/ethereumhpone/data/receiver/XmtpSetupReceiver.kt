package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject
import androidx.core.content.edit
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.model.UserData

@AndroidEntryPoint
class XmtpSetupReceiver : HiltBroadcastReceiver() {

    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var prefs: MessengerPreferences

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != ACTION_SETUP_XMTP) return

        // Use goAsync to allow asynchronous work if needed
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Kick off client creation
                xmtpClientManager.createClient(walletSDK, context.applicationContext)

                // Wait until the client is fully ready (signature completed)
                xmtpClientManager.clientState.first { it is XmtpClientManager.ClientState.Ready }

                // Mark setup as completed so the app can skip onboarding in the future
                context.getSharedPreferences("org.ethereumhpone.messenger.prefs", Context.MODE_PRIVATE)
                    .edit {
                        putBoolean("SETUP_XMTP", true)
                    }

                prefs.setUseXmtp(true)

                // Now notify SetupWizard that XMTP setup is complete
                val doneIntent = Intent("app.grapheneos.setupwizard.action.XMTP_SETUP_DONE").apply {
                    `package` = "app.grapheneos.setupwizard" // restrict broadcast to SetupWizard app
                }


                context.sendBroadcast(doneIntent)
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