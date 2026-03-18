package org.ethereumhpone.data.receiver

import android.content.Context
import android.content.Intent
import android.os.ResultReceiver
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

@AndroidEntryPoint
class XmtpSetupReceiver : HiltBroadcastReceiver() {

    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var prefs: MessengerPreferences

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != ACTION_SETUP_XMTP) return

        val walletAddress = intent.getStringExtra("wallet_address") ?: ""
        // Use the framework classloader to deserialize the ResultReceiver — the
        // default app classloader can't find SetupWizard's anonymous subclass.
        val resultReceiver: ResultReceiver? = try {
            intent.extras?.let { extras ->
                extras.classLoader = ResultReceiver::class.java.classLoader
                extras.getParcelable("result_receiver")
            }
        } catch (e: Exception) {
            android.util.Log.w("XmtpSetupReceiver", "Failed to extract ResultReceiver", e)
            null
        }

        // Use goAsync to allow asynchronous work if needed
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Kick off client creation — pass the wallet address from SetupWizard
                // so Messenger doesn't need to resolve it via WalletSDK (which returns
                // empty during device setup).
                xmtpClientManager.createClient(walletSDK, context.applicationContext, walletAddress)

                // Wait until the client is fully ready (signature completed)
                xmtpClientManager.clientState.first { it is XmtpClientManager.ClientState.Ready }

                // Mark setup as completed so the app can skip onboarding in the future
                context.getSharedPreferences("org.ethereumhpone.messenger.prefs", Context.MODE_PRIVATE)
                    .edit {
                        putBoolean("SETUP_XMTP", true)
                    }

                prefs.setUseXmtp(true)
                prefs.setShouldHideOnboarding(true)
                android.util.Log.d("XmtpSetupReceiver", "Set both useXmtp and shouldHideOnboarding to true")

                // Notify SetupWizard that XMTP setup is complete via ResultReceiver IPC
                resultReceiver?.send(0, null)
                android.util.Log.d("XmtpSetupReceiver", "Sent setup done callback to SetupWizard")
            } catch (e: Exception) {
                e.printStackTrace()
                resultReceiver?.send(1, null)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SETUP_XMTP = "org.ethereumhpone.messenger.action.SETUP_XMTP"
    }
}