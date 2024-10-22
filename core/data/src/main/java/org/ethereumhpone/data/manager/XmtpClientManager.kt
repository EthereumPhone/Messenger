package org.ethereumhpone.data.manager

import android.content.Context
import com.google.protobuf.ByteString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumphone.walletsdk.WalletSDK
import org.xmtp.android.library.Client
import org.xmtp.android.library.ClientOptions
import org.xmtp.android.library.SigningKey
import org.xmtp.android.library.XMTPEnvironment
import org.xmtp.android.library.codecs.AttachmentCodec
import org.xmtp.android.library.codecs.GroupUpdatedCodec
import org.xmtp.android.library.codecs.ReactionCodec
import org.xmtp.android.library.codecs.ReadReceiptCodec
import org.xmtp.android.library.codecs.RemoteAttachmentCodec
import org.xmtp.android.library.codecs.ReplyCodec
import org.xmtp.android.library.messages.PrivateKeyBundleV1Builder
import org.xmtp.android.library.messages.walletAddress
import org.xmtp.proto.message.contents.SignatureOuterClass
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
object XmtpClientManager {

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
            enableV3 = true,
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



    @OptIn(DelicateCoroutinesApi::class)
    fun createClient(
        encodedPrivateKeyData: String,
        appContext: Context
    ) {
        if (clientState.value is ClientState.Ready) return

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val v1Bundle = PrivateKeyBundleV1Builder.fromEncodedData(data = encodedPrivateKeyData)
                _client = Client().buildFrom(v1Bundle, clientOptions(appContext, v1Bundle.walletAddress))

                Client.register(codec = GroupUpdatedCodec())
                Client.register(codec = ReadReceiptCodec())
                Client.register(codec = ReactionCodec())
                Client.register(codec = ReplyCodec())
                Client.register(codec = AttachmentCodec())
                Client.register(codec = RemoteAttachmentCodec())

                _clientState.value = ClientState.Ready

            } catch (e: Exception) {
                _clientState.value = ClientState.Error(e.localizedMessage.orEmpty())
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

class EthOSSigningKey(private val walletSDK: WalletSDK) : SigningKey {
    override val address: String
        get() = walletSDK.getAddress()

    override suspend fun sign(data: ByteArray): SignatureOuterClass.Signature? {
        return sign(String(data))
    }

    override suspend fun sign(message: String): SignatureOuterClass.Signature? {
        val signatureString = walletSDK.signMessage(message)
        val signatureBytes = signatureString.removePrefix("0x").chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        val r = signatureBytes.sliceArray(0 until 32)
        val s = signatureBytes.sliceArray(32 until 64)
        val v = signatureBytes[64].toInt() - 27  // Adjust v value as required

        val ecdsaCompact = SignatureOuterClass.Signature.ECDSACompact.newBuilder()
            .setBytes((r + s).toByteString())
            .setRecovery(v)
            .build()

        return SignatureOuterClass.Signature.newBuilder()
            .setEcdsaCompact(ecdsaCompact)
            .build()
    }

    // Utility extension function to convert ByteArray to ByteString
    fun ByteArray.toByteString(): ByteString {
        return ByteString.copyFrom(this)
    }
}