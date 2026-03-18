package org.ethereumhpone.data.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.ipc.IXmtpSetupCallback
import org.ethereumhpone.ipc.IXmtpSetupService
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

/**
 * Bound service that allows external apps (SetupWizard) to trigger XMTP
 * client creation.  Mirrors the MsgSyncService pattern: the caller binds,
 * calls [setupNow], and receives a callback when the operation finishes.
 */
@AndroidEntryPoint
class XmtpSetupService : Service() {

    companion object {
        private const val TAG = "XmtpSetupService"
    }

    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var prefs: MessengerPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val binder = object : IXmtpSetupService.Stub() {
        override fun setupNow(callback: IXmtpSetupCallback?) {
            doSetup("", callback)
        }

        override fun setupNowWithAddress(walletAddress: String?, callback: IXmtpSetupCallback?) {
            doSetup(walletAddress ?: "", callback)
        }

        private fun doSetup(walletAddress: String, callback: IXmtpSetupCallback?) {
            Log.i(TAG, "doSetup() called, address=${if (walletAddress.isNotEmpty()) walletAddress else "(from WalletSDK)"}")
            serviceScope.launch {
                try {
                    xmtpClientManager.createClient(walletSDK, applicationContext, walletAddress)

                    // Wait until the client reports Ready or Error
                    val state = xmtpClientManager.clientState.first {
                        it is XmtpClientManager.ClientState.Ready ||
                        it is XmtpClientManager.ClientState.Error
                    }

                    val success = state is XmtpClientManager.ClientState.Ready
                    if (success) {
                        prefs.setUseXmtp(true)
                        prefs.setShouldHideOnboarding(true)
                        Log.i(TAG, "XMTP setup completed successfully")
                    } else {
                        Log.w(TAG, "XMTP client entered error state: $state")
                    }

                    callback?.onSetupComplete(success)
                } catch (e: Exception) {
                    Log.e(TAG, "XMTP setup failed", e)
                    try { callback?.onSetupComplete(false) } catch (_: Exception) {}
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "onBind()")
        return binder
    }

    override fun onDestroy() {
        Log.i(TAG, "XmtpSetupService destroyed")
        serviceScope.cancel()
        super.onDestroy()
    }
}
