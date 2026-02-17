package org.ethereumhpone.data.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
import android.os.Binder
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.ethereumhpone.data.codec.TransactionRequestCodec
import org.ethereumhpone.data.manager.GeneratedWallet
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.ipc.IIdentityMessageCallback
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
import org.xmtp.android.library.Conversation
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.PublicIdentity
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Bound service that allows third-party apps to generate an isolated XMTP
 * identity and send messages through it. Each calling app is identified by
 * its package name + signing certificate hash, so the identity persists
 * across app reinstalls (same developer key) while preventing a different
 * developer from hijacking an identity by reusing the same package name.
 * Requires GENERATE_XMTP_IDENTITY permission.
 */
@AndroidEntryPoint
class ThirdPartyIdentityService : Service() {

    @Inject lateinit var appContext: Context

    /** Cached XMTP clients for isolated identities, keyed by package + signing cert hash. */
    private val identityClients = ConcurrentHashMap<String, Client>()

    private val binder = object : IXmtpIdentityService.Stub() {

        override fun createIdentity(): String? {
            val key = callerKey()
            return runBlocking(Dispatchers.IO) {
                try {
                    // If already exists, return existing address
                    val existingAddress = loadAddress(key)
                    if (existingAddress != null) {
                        // Ensure client is initialized
                        getOrCreateClient(key)
                        return@runBlocking existingAddress
                    }

                    // Generate new keypair using SecureRandom + ECKeyPair.create()
                    // to avoid Android's stripped BouncyCastle missing "ECDSA"
                    val privateKeyBytes = ByteArray(32)
                    SecureRandom().nextBytes(privateKeyBytes)
                    val ecKeyPair = ECKeyPair.create(BigInteger(1, privateKeyBytes))
                    val wallet = GeneratedWallet(ecKeyPair)

                    // Store private key and address
                    storePrivateKey(key, ecKeyPair.privateKey.toString(16))
                    storeAddress(key, wallet.address)

                    // Create XMTP client
                    registerCodecs()
                    val options = XmtpClientManager.clientOptions(appContext, wallet.address)
                    val client = Client.create(account = wallet, options = options)
                    identityClients[key] = client

                    Log.d(TAG, "createIdentity: created ${wallet.address} for caller $key")
                    wallet.address
                } catch (e: Exception) {
                    Log.e(TAG, "createIdentity failed for caller $key", e)
                    null
                }
            }
        }

        override fun hasIdentity(): Boolean {
            return loadAddress(callerKey()) != null
        }

        override fun getIdentityAddress(): String? {
            return loadAddress(callerKey())
        }

        override fun getInboxId(): String? {
            val key = callerKey()
            return runBlocking(Dispatchers.IO) {
                try {
                    val client = getOrCreateClient(key)
                    client?.inboxId
                } catch (e: Exception) {
                    Log.e(TAG, "getInboxId failed for caller $key", e)
                    null
                }
            }
        }

        override fun sendMessage(recipientAddress: String, body: String): String? {
            val key = callerKey()
            return runBlocking(Dispatchers.IO) {
                try {
                    val client = getOrCreateClient(key)
                    if (client == null) {
                        Log.w(TAG, "sendMessage: no identity for caller $key")
                        return@runBlocking null
                    }

                    val identity = PublicIdentity(IdentityKind.ETHEREUM, recipientAddress)
                    val dm = client.conversations.findOrCreateDmWithIdentity(identity)
                    val messageId = dm.prepareMessage(body)
                    dm.publishMessages()

                    Log.d(TAG, "sendMessage: sent $messageId from isolated identity to $recipientAddress")
                    messageId
                } catch (e: Exception) {
                    Log.e(TAG, "sendMessage failed for caller $key", e)
                    null
                }
            }
        }

        override fun syncConversations() {
            val key = callerKey()
            runBlocking(Dispatchers.IO) {
                try {
                    val client = getOrCreateClient(key)
                    if (client == null) {
                        Log.w(TAG, "syncConversations: no identity for caller $key")
                        return@runBlocking
                    }
                    client.conversations.syncAllConversations()
                    Log.d(TAG, "syncConversations: synced for caller $key")
                } catch (e: Exception) {
                    Log.e(TAG, "syncConversations failed for caller $key", e)
                }
            }
        }

        override fun getConversations(): String? {
            val key = callerKey()
            return runBlocking(Dispatchers.IO) {
                try {
                    val client = getOrCreateClient(key)
                    if (client == null) {
                        Log.w(TAG, "getConversations: no identity for caller $key")
                        return@runBlocking null
                    }
                    val conversations = client.conversations.list()
                    val jsonArray = JSONArray()
                    for (conversation in conversations) {
                        val obj = JSONObject()
                        obj.put("id", conversation.id)
                        obj.put("createdAtMs", conversation.createdAt.time)
                        // Extract peer address from members (the one that isn't us)
                        val peerAddress = conversation.members()
                            .flatMap { it.identities }
                            .filter { it.kind == IdentityKind.ETHEREUM }
                            .map { it.identifier }
                            .firstOrNull { !it.equals(loadAddress(key), ignoreCase = true) }
                        obj.put("peerAddress", peerAddress ?: "")
                        jsonArray.put(obj)
                    }
                    jsonArray.toString()
                } catch (e: Exception) {
                    Log.e(TAG, "getConversations failed for caller $key", e)
                    null
                }
            }
        }

        override fun registerMessageCallback(callback: IIdentityMessageCallback) {
            IdentityCallbackRegistry.register(callerKey(), callback)
        }

        override fun unregisterMessageCallback(callback: IIdentityMessageCallback) {
            IdentityCallbackRegistry.unregister(callerKey(), callback)
        }

        override fun getMessages(conversationId: String, afterNs: Long): String? {
            val key = callerKey()
            return runBlocking(Dispatchers.IO) {
                try {
                    val client = getOrCreateClient(key)
                    if (client == null) {
                        Log.w(TAG, "getMessages: no identity for caller $key")
                        return@runBlocking null
                    }
                    val conversation = client.conversations.findConversation(conversationId)
                    if (conversation == null) {
                        Log.w(TAG, "getMessages: conversation $conversationId not found")
                        return@runBlocking null
                    }
                    val messages = conversation.messages(afterNs = afterNs)
                    val myAddress = loadAddress(key)
                    val jsonArray = JSONArray()
                    for (msg in messages) {
                        // Skip empty messages
                        if (msg.body.isNullOrBlank()) continue
                        val obj = JSONObject()
                        obj.put("id", msg.id)
                        obj.put("senderInboxId", msg.senderInboxId)
                        obj.put("body", msg.body)
                        obj.put("sentAtMs", msg.sentAtNs / 1_000_000)
                        obj.put("isMe", msg.senderInboxId == client.inboxId)
                        jsonArray.put(obj)
                    }
                    jsonArray.toString()
                } catch (e: Exception) {
                    Log.e(TAG, "getMessages failed for caller $key", e)
                    null
                }
            }
        }

    }

    /**
     * Derives a stable identity key from the caller's package name and signing
     * certificate hash. This survives app reinstalls (same signing key) but
     * prevents a different developer from hijacking the identity.
     */
    private fun callerKey(): String {
        val uid = Binder.getCallingUid()
        val pm = appContext.packageManager
        val packageName = pm.getNameForUid(uid)
            ?: throw SecurityException("Cannot resolve package for UID $uid")
        val packageInfo = pm.getPackageInfo(packageName, GET_SIGNING_CERTIFICATES)
        val cert = packageInfo.signingInfo.apkContentsSigners.firstOrNull()
            ?: throw SecurityException("No signing certificate for $packageName")
        return "${packageName}_${cert.hashCode()}"
    }

    /**
     * Returns an existing cached client or creates one from stored key material.
     * Returns null if no identity has been created yet.
     */
    private suspend fun getOrCreateClient(callerKey: String): Client? {
        identityClients[callerKey]?.let { return it }

        val privateKeyHex = loadPrivateKey(callerKey) ?: return null
        val wallet = GeneratedWallet.fromPrivateKeyHex(privateKeyHex)

        registerCodecs()
        val options = XmtpClientManager.clientOptions(appContext, wallet.address)
        val client = Client.create(account = wallet, options = options)
        identityClients[callerKey] = client
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

    private fun storePrivateKey(callerKey: String, hexKey: String) {
        prefs().edit().putString("${KEY_PREFIX}${callerKey}_key", hexKey).apply()
    }

    private fun loadPrivateKey(callerKey: String): String? =
        prefs().getString("${KEY_PREFIX}${callerKey}_key", null)

    private fun storeAddress(callerKey: String, address: String) {
        prefs().edit().putString("${KEY_PREFIX}${callerKey}_address", address).apply()
    }

    private fun loadAddress(callerKey: String): String? =
        prefs().getString("${KEY_PREFIX}${callerKey}_address", null)

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        private const val TAG = "ThirdPartyIdentity"
        private const val PREFS_NAME = "IsolatedIdentities"
        private const val KEY_PREFIX = "identity_"

        /**
         * Returns all stored caller keys that have a private key + address.
         * Used by MsgSyncService to sync all isolated identity clients.
         */
        fun getAllCallerKeys(context: Context): List<String> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.all.keys
                .filter { it.startsWith(KEY_PREFIX) && it.endsWith("_key") }
                .map { it.removePrefix(KEY_PREFIX).removeSuffix("_key") }
        }

        /**
         * Loads the private key hex for a given caller key.
         * Used by MsgSyncService to create clients for sync.
         */
        fun loadPrivateKeyForSync(context: Context, callerKey: String): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString("${KEY_PREFIX}${callerKey}_key", null)
        }

        /**
         * Loads the address for a given caller key.
         */
        fun loadAddressForSync(context: Context, callerKey: String): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString("${KEY_PREFIX}${callerKey}_address", null)
        }
    }
}
