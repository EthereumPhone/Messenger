package org.ethereumhpone.data.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.ethereumhpone.data.codec.TransactionRequestCodec
import org.ethereumhpone.data.manager.GeneratedWallet
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.ipc.IXmtpIdentityService
import org.web3j.crypto.ECKeyPair
import org.xmtp.android.library.Client
import java.math.BigInteger
import java.security.SecureRandom
import org.xmtp.android.library.codecs.AttachmentCodec
import org.xmtp.android.library.codecs.GroupUpdatedCodec
import org.xmtp.android.library.codecs.ReactionCodec
import org.xmtp.android.library.codecs.ReadReceiptCodec
import org.xmtp.android.library.codecs.RemoteAttachmentCodec
import org.xmtp.android.library.codecs.ReplyCodec
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.PublicIdentity
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Bound service that allows third-party apps to generate an isolated XMTP
 * identity and send messages through it. Each calling app (identified by UID)
 * gets its own unique identity. Requires GENERATE_XMTP_IDENTITY permission.
 */
@AndroidEntryPoint
class ThirdPartyIdentityService : Service() {

    @Inject lateinit var appContext: Context

    /** Cached XMTP clients for isolated identities, keyed by calling UID. */
    private val identityClients = ConcurrentHashMap<Int, Client>()

    private val binder = object : IXmtpIdentityService.Stub() {

        override fun createIdentity(): String? {
            enforceIdentityPermission()
            val callingUid = Binder.getCallingUid()
            return runBlocking(Dispatchers.IO) {
                try {
                    // If already exists, return existing address
                    val existingAddress = loadAddress(callingUid)
                    if (existingAddress != null) {
                        // Ensure client is initialized
                        getOrCreateClient(callingUid)
                        return@runBlocking existingAddress
                    }

                    // Generate new keypair using SecureRandom + ECKeyPair.create()
                    // to avoid Android's stripped BouncyCastle missing "ECDSA"
                    val privateKeyBytes = ByteArray(32)
                    SecureRandom().nextBytes(privateKeyBytes)
                    val ecKeyPair = ECKeyPair.create(BigInteger(1, privateKeyBytes))
                    val wallet = GeneratedWallet(ecKeyPair)

                    // Store private key and address
                    storePrivateKey(callingUid, ecKeyPair.privateKey.toString(16))
                    storeAddress(callingUid, wallet.address)

                    // Create XMTP client
                    registerCodecs()
                    val options = XmtpClientManager.clientOptions(appContext, wallet.address)
                    val client = Client.create(account = wallet, options = options)
                    identityClients[callingUid] = client

                    Log.d(TAG, "createIdentity: created ${wallet.address} for UID $callingUid")
                    wallet.address
                } catch (e: Exception) {
                    Log.e(TAG, "createIdentity failed for UID $callingUid", e)
                    null
                }
            }
        }

        override fun hasIdentity(): Boolean {
            enforceIdentityPermission()
            val callingUid = Binder.getCallingUid()
            return loadAddress(callingUid) != null
        }

        override fun getIdentityAddress(): String? {
            enforceIdentityPermission()
            val callingUid = Binder.getCallingUid()
            return loadAddress(callingUid)
        }

        override fun getInboxId(): String? {
            enforceIdentityPermission()
            val callingUid = Binder.getCallingUid()
            return runBlocking(Dispatchers.IO) {
                try {
                    val client = getOrCreateClient(callingUid)
                    client?.inboxId
                } catch (e: Exception) {
                    Log.e(TAG, "getInboxId failed for UID $callingUid", e)
                    null
                }
            }
        }

        override fun sendMessage(recipientAddress: String, body: String): String? {
            enforceIdentityPermission()
            val callingUid = Binder.getCallingUid()
            return runBlocking(Dispatchers.IO) {
                try {
                    val client = getOrCreateClient(callingUid)
                    if (client == null) {
                        Log.w(TAG, "sendMessage: no identity for UID $callingUid")
                        return@runBlocking null
                    }

                    val identity = PublicIdentity(IdentityKind.ETHEREUM, recipientAddress)
                    val dm = client.conversations.findOrCreateDmWithIdentity(identity)
                    val messageId = dm.prepareMessage(body)
                    dm.publishMessages()

                    Log.d(TAG, "sendMessage: sent $messageId from isolated identity to $recipientAddress")
                    messageId
                } catch (e: Exception) {
                    Log.e(TAG, "sendMessage failed for UID $callingUid", e)
                    null
                }
            }
        }

        private fun enforceIdentityPermission() {
            val result = checkCallingPermission(PERMISSION_GENERATE_IDENTITY)
            if (result != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Caller does not hold $PERMISSION_GENERATE_IDENTITY")
            }
        }
    }

    /**
     * Returns an existing cached client or creates one from stored key material.
     * Returns null if no identity has been created yet.
     */
    private suspend fun getOrCreateClient(uid: Int): Client? {
        identityClients[uid]?.let { return it }

        val privateKeyHex = loadPrivateKey(uid) ?: return null
        val wallet = GeneratedWallet.fromPrivateKeyHex(privateKeyHex)

        registerCodecs()
        val options = XmtpClientManager.clientOptions(appContext, wallet.address)
        val client = Client.create(account = wallet, options = options)
        identityClients[uid] = client
        return client
    }

    private fun registerCodecs() {
        Client.register(codec = GroupUpdatedCodec())
        Client.register(codec = ReadReceiptCodec())
        Client.register(codec = ReactionCodec())
        Client.register(codec = ReplyCodec())
        Client.register(codec = AttachmentCodec())
        Client.register(codec = RemoteAttachmentCodec())
        Client.register(codec = TransactionRequestCodec())
    }

    // --- Key storage (SharedPreferences, private to this app) ---

    private fun prefs() =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun storePrivateKey(uid: Int, hexKey: String) {
        prefs().edit().putString("${KEY_PREFIX}${uid}_key", hexKey).apply()
    }

    private fun loadPrivateKey(uid: Int): String? =
        prefs().getString("${KEY_PREFIX}${uid}_key", null)

    private fun storeAddress(uid: Int, address: String) {
        prefs().edit().putString("${KEY_PREFIX}${uid}_address", address).apply()
    }

    private fun loadAddress(uid: Int): String? =
        prefs().getString("${KEY_PREFIX}${uid}_address", null)

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        private const val TAG = "ThirdPartyIdentity"
        private const val PREFS_NAME = "IsolatedIdentities"
        private const val KEY_PREFIX = "identity_"
        const val PERMISSION_GENERATE_IDENTITY =
            "org.ethereumhpone.messenger.permission.GENERATE_XMTP_IDENTITY"
    }
}
