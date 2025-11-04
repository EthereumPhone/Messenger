package org.ethereumhpone.data.manager

import android.content.Context
import android.util.Log
import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.toByteString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumphone.walletsdk.WalletSDK
import org.xmtp.android.library.Client
import org.xmtp.android.library.ClientOptions
import org.xmtp.android.library.SignedData
import org.xmtp.android.library.SignerType
import org.xmtp.android.library.SigningKey
import org.xmtp.android.library.XMTPEnvironment
import org.xmtp.android.library.codecs.AttachmentCodec
import org.xmtp.android.library.codecs.GroupUpdatedCodec
import org.xmtp.android.library.codecs.ReactionCodec
import org.xmtp.android.library.codecs.ReadReceiptCodec
import org.xmtp.android.library.codecs.RemoteAttachmentCodec
import org.xmtp.android.library.codecs.ReplyCodec
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.PublicIdentity
import org.xmtp.android.library.messages.walletAddress
import org.xmtp.proto.message.contents.SignatureOuterClass
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
object XmtpClientManager {
    private val creating = AtomicBoolean(false)

    fun clientOptions(appContext: Context, address: String): ClientOptions {
        val keyUtil = KeyUtil(appContext)
        var encryptionKey = keyUtil.retrieveKey("${address}_encryption")

        if (encryptionKey.isNullOrEmpty()) {
            encryptionKey = Base64.getEncoder().encodeToString(SecureRandom().generateSeed(32))
            keyUtil.storeKey("${address}_encryption", encryptionKey)
        }

        return ClientOptions(
            api = ClientOptions.Api(
                XMTPEnvironment.PRODUCTION,
                //appVersion = "XMTPAndroidExample/v1.0.0",
                isSecure = true
            ),
            appContext = appContext,
            dbEncryptionKey = Base64.getDecoder().decode(encryptionKey)
        )
    }

    private val _clientState = MutableStateFlow<ClientState>(ClientState.Unknown)
    val clientState: StateFlow<ClientState> = _clientState

    private var _client: Client? = null

    val client: Client
        get() = if (clientState.value == ClientState.Ready) {
            _client!!
        } else {
            throw IllegalStateException("Client called before Ready state")
        }


    suspend fun awaitClient(): Client {
        clientState.first { it == ClientState.Ready }
        return _client!!
    }

    val clientOrNull: Client?
        get() = _client



    @OptIn(DelicateCoroutinesApi::class)
    fun createClient(
        walletSDK: WalletSDK,
        appContext: Context
    ) {
        if (clientState.value is ClientState.Ready) return
        if (!creating.compareAndSet(false, true)) return

        GlobalScope.launch(Dispatchers.IO) {
            val address = walletSDK.getAddress()
            Log.d("my address", address)
            try {
                _client = Client.create(
                    account = EOAWallet(walletSDK, address),
                    options = clientOptions(appContext, address),
                )

                Client.register(codec = GroupUpdatedCodec())
                Client.register(codec = ReadReceiptCodec())
                Client.register(codec = ReactionCodec())
                Client.register(codec = ReplyCodec())
                Client.register(codec = AttachmentCodec())
                Client.register(codec = RemoteAttachmentCodec())

                _clientState.value = ClientState.Ready

            } catch (e: Exception) {
                _clientState.value = ClientState.Error(e.localizedMessage.orEmpty())
            } finally {
                creating.set(false)
            }
        }
    }

    sealed class ClientState {
        object Unknown : ClientState()
        object Ready : ClientState()
        data class Error(val message: String) : ClientState()
    }

}

class KeyUtil(val context: Context) {
    private val PREFS_NAME = "EncryptionPref"



    fun storeKey(address: String, key: String) {
        val alias = "xmtp-${address.lowercase()}"

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(alias, key)
        editor.apply()
    }

    fun retrieveKey(address: String): String? {
        val alias = "xmtp-${address.lowercase()}"

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(alias, null)
    }
}

class EOAWallet(val walletSDK: WalletSDK, val address: String) : SigningKey {
    override val publicIdentity: PublicIdentity
        get() = PublicIdentity(
            IdentityKind.ETHEREUM,
            address
        )


    override val type: SignerType
        get() = SignerType.SCW

    override var chainId: Long? = 8453 // https://chainlist.org/


    // Explicitly implement `blockNumber` to prevent Kotlin's stub generator from
    // creating a setter with the illegal Java identifier "_" (underscore) which
    // breaks compilation on JDK 9+. Declaring the property ourselves ensures the
    // generated setter parameter uses the standard name `value` instead.
    override var blockNumber: Long? = null

    private val signMutex = Mutex()

    override suspend fun sign(message: String): SignedData {
        return signMutex.withLock {
            // Ensure signing is performed on the Main thread so that any UI-driven wallet prompts are shown properly
            val signatureString = try {
                withTimeout(30_000) {
                    withContext(Dispatchers.Main) {
                        walletSDK.signMessage(message, 8453)
                    }
                }
            } catch (e: IllegalStateException) {
                if (e.message?.contains("Already resumed", ignoreCase = true) == true) {
                    Log.w("EOAWallet", "Duplicate callback from WalletSDK.signMessage ignored; not retrying", e)
                    throw CancellationException("WalletSDK duplicate callback; operation cancelled")
                } else {
                    throw e
                }
            }

            if (signatureString.isBlank() || signatureString.equals("decline", ignoreCase = true)) {
                throw CancellationException("User declined signing")
            }

            val normalized = signatureString.trim()
            val hex = if (normalized.startsWith("0x")) normalized.substring(2) else normalized
            if (hex.isEmpty() || hex.length % 2 != 0 || !hex.all { it in '0'..'9' || it.lowercaseChar() in 'a'..'f' }) {
                throw IllegalArgumentException("Invalid signature format")
            }

            val signatureBytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            SignedData(signatureBytes)
        }
    }
}