package org.ethereumhpone.messenger.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Nullable
import com.google.protobuf.ByteString
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.internal.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.domain.model.ClientWrapper
import org.ethereumhpone.domain.model.LogTimeHandler
import org.ethereumhpone.domain.model.XMTPPrivateKeyHandler
import org.ethereumhpone.messenger.BuildConfig
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.xmtp.android.library.Client
import org.xmtp.android.library.ClientOptions
import org.xmtp.android.library.SigningKey
import org.xmtp.android.library.XMTPEnvironment
import org.xmtp.android.library.codecs.AttachmentCodec
import org.xmtp.android.library.codecs.ReactionCodec
import org.xmtp.android.library.codecs.ReadReceiptCodec
import org.xmtp.android.library.codecs.RemoteAttachmentCodec
import org.xmtp.android.library.codecs.ReplyCodec
import org.xmtp.android.library.messages.PrivateKeyBundleV1Builder
import org.xmtp.proto.message.contents.SignatureOuterClass
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    fun chainToApiKey(networkName: String): String = when(networkName) {
        "eth-mainnet" -> BuildConfig.ETHEREUM_API
        "eth-sepolia" -> BuildConfig.SEPOLIA_API
        "opt-mainnet" -> BuildConfig.OPTIMISM_API
        "arb-mainnet" -> BuildConfig.ARBITRUM_API
        "polygon-mainnet" -> BuildConfig.POLYGON_API
        "base-mainnet" -> BuildConfig.BASE_API
        "eth-goerli" -> BuildConfig.BASE_API
        else -> ""
    }

    fun chainIdToName(chainId: Int): String = when(chainId) {
        1 -> "eth-mainnet"
        11155111 -> "eth-sepolia"
        10 -> "opt-mainnet"
        42161 -> "arb-mainnet"
        137 -> "polygon-mainnet"
        8453 -> "base-mainnet"
        5 -> "eth-goerli"
        else -> ""
    }

    fun chainIdToRPC(chainId: Int): String {
        return "https://${chainIdToName(chainId)}.g.alchemy.com/v2/${chainToApiKey(chainIdToName(chainId))}"
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideLogTimeHandler(@ApplicationContext context: Context) = LogTimeHandler(context.getSharedPreferences("app", Context.MODE_PRIVATE))

    @Provides
    @Singleton
    fun providePrivateKeyHandler(@ApplicationContext context: Context) = XMTPPrivateKeyHandler(context.getSharedPreferences("app", Context.MODE_PRIVATE))

    @Provides
    @Singleton
    fun provideWeb3j(): Web3j {
        return Web3j.build(HttpService(chainIdToRPC(1)))
    }

    @Provides
    @Singleton
    fun bindWalletSDK(
        @ApplicationContext context: Context,
    ): WalletSDK {
        var walletSDK = WalletSDK(context)

        runBlocking {
            val currentChainId = withContext(Dispatchers.IO) {
                walletSDK.getChainId()
            }
            walletSDK = WalletSDK(context, Web3j.build(HttpService(chainIdToRPC(currentChainId))))
        }

        return walletSDK
    }


    @Singleton
    @Provides
    fun provideXmtpClientManger(): XmtpClientManager = XmtpClientManager


}


/*
@Singleton
    @Provides
    fun provideXmtpClientProvider(
        walletSDK: WalletSDK,
        @ApplicationContext context: Context,
    ): Client {

        val options = ClientOptions(api = ClientOptions.Api(env = XMTPEnvironment.PRODUCTION, isSecure = true), appContext = context)
        Client.register(codec = ReadReceiptCodec())
        Client.register(codec = ReactionCodec())
        Client.register(codec = ReplyCodec())
        Client.register(codec = AttachmentCodec())
        Client.register(codec = RemoteAttachmentCodec())


        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val key = prefs.getString("xmtpKey", null)
        return if (key.isNullOrEmpty()) {
             runBlocking {
                Client().create(account = EthOSSigningKey(walletSDK), options = options).apply {
                    prefs.edit().putString("xmtpKey", PrivateKeyBundleV1Builder.encodeData(privateKeyBundleV1)).apply()
                }
            }
        } else {
            runBlocking { Client().buildFrom(bundle = PrivateKeyBundleV1Builder.fromEncodedData(key), options = options) }
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








@Singleton
class XmtpClientFactory @Inject constructor(
    private val walletSDK: WalletSDK,
    @ApplicationContext private val context: Context,
) {
    suspend fun createClient(): Client {
        val options = ClientOptions(
            api = ClientOptions.Api(env = XMTPEnvironment.PRODUCTION, isSecure = true),
            appContext = context
        )
        Client.register(codec = ReadReceiptCodec())
        Client.register(codec = ReactionCodec())
        Client.register(codec = ReplyCodec())
        Client.register(codec = AttachmentCodec())
        Client.register(codec = RemoteAttachmentCodec())

        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val key = prefs.getString("xmtpKey", null)

        return if (key.isNullOrEmpty()) {
            Client().create(account = AppModule.EthOSSigningKey(walletSDK), options = options).also {
                prefs.edit().putString("xmtpKey", PrivateKeyBundleV1Builder.encodeData(it.privateKeyBundleV1)).apply()
            }
        } else {
            Client().buildFrom(bundle = PrivateKeyBundleV1Builder.fromEncodedData(key), options = options)
        }
    }
}
 */
