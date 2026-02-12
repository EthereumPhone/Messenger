package org.ethereumhpone.data.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.ipc.IMessagingService
import org.ethereumphone.walletsdk.WalletSDK
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.PublicIdentity
import javax.inject.Inject

/**
 * Bound service that allows third-party apps to send XMTP messages
 * as the currently logged-in user. Requires SEND_MESSAGE_AS_USER permission.
 */
@AndroidEntryPoint
class ThirdPartyMessagingService : Service() {

    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var walletSDK: WalletSDK

    private val binder = object : IMessagingService.Stub() {

        override fun isClientReady(): Boolean {
            enforceMessagingPermission()
            return xmtpClientManager.clientState.value == XmtpClientManager.ClientState.Ready
        }

        override fun getUserAddress(): String? {
            enforceMessagingPermission()
            return runBlocking(Dispatchers.IO) {
                val ready = withTimeoutOrNull(10_000L) {
                    xmtpClientManager.clientState.first {
                        it == XmtpClientManager.ClientState.Ready
                    }
                }
                if (ready == null) return@runBlocking null
                walletSDK.getAddress()
            }
        }

        override fun getInboxId(): String? {
            enforceMessagingPermission()
            return runBlocking(Dispatchers.IO) {
                val ready = withTimeoutOrNull(10_000L) {
                    xmtpClientManager.clientState.first {
                        it == XmtpClientManager.ClientState.Ready
                    }
                }
                if (ready == null) return@runBlocking null
                xmtpClientManager.client.inboxId
            }
        }

        override fun sendMessage(recipientAddress: String, body: String): String? {
            enforceMessagingPermission()
            return runBlocking(Dispatchers.IO) {
                try {
                    val ready = withTimeoutOrNull(15_000L) {
                        xmtpClientManager.clientState.first {
                            it == XmtpClientManager.ClientState.Ready
                        }
                    }
                    if (ready == null) {
                        Log.w(TAG, "sendMessage: XMTP client not ready")
                        return@runBlocking null
                    }

                    val client = xmtpClientManager.client
                    val identity = PublicIdentity(IdentityKind.ETHEREUM, recipientAddress)
                    val dm = client.conversations.findOrCreateDmWithIdentity(identity)
                    val messageId = dm.prepareMessage(body)
                    dm.publishMessages()

                    Log.d(TAG, "sendMessage: sent $messageId to $recipientAddress")
                    messageId
                } catch (e: Exception) {
                    Log.e(TAG, "sendMessage failed", e)
                    null
                }
            }
        }

        override fun sendGroupMessage(conversationId: String, body: String): String? {
            enforceMessagingPermission()
            return runBlocking(Dispatchers.IO) {
                try {
                    val ready = withTimeoutOrNull(15_000L) {
                        xmtpClientManager.clientState.first {
                            it == XmtpClientManager.ClientState.Ready
                        }
                    }
                    if (ready == null) {
                        Log.w(TAG, "sendGroupMessage: XMTP client not ready")
                        return@runBlocking null
                    }

                    val client = xmtpClientManager.client
                    val conversation = client.conversations.findConversation(conversationId)
                    if (conversation == null) {
                        Log.w(TAG, "sendGroupMessage: conversation $conversationId not found")
                        return@runBlocking null
                    }

                    val messageId = conversation.prepareMessage(body)
                    conversation.publishMessages()

                    Log.d(TAG, "sendGroupMessage: sent $messageId to $conversationId")
                    messageId
                } catch (e: Exception) {
                    Log.e(TAG, "sendGroupMessage failed", e)
                    null
                }
            }
        }

        private fun enforceMessagingPermission() {
            val result = checkCallingPermission(PERMISSION_SEND_MESSAGE)
            if (result != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Caller does not hold $PERMISSION_SEND_MESSAGE")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        private const val TAG = "ThirdPartyMessaging"
        const val PERMISSION_SEND_MESSAGE =
            "org.ethereumhpone.messenger.permission.SEND_MESSAGE_AS_USER"
    }
}
