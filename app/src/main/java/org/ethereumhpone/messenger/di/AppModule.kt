package org.ethereumhpone.messenger.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import com.google.protobuf.ByteString
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ethereumhpone.domain.model.LogTimeHandler
import org.ethereumhpone.messenger.BuildConfig
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.xmtp.android.library.Client
import org.xmtp.android.library.ClientOptions
import org.xmtp.android.library.SigningKey
import org.xmtp.android.library.XMTPEnvironment
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

    @Provides
    @Singleton
    fun provideXmtpClient(walletSDK: WalletSDK, @ApplicationContext context: Context): Client {
        val options = ClientOptions(api = ClientOptions.Api(env = XMTPEnvironment.PRODUCTION, isSecure = true), enableV3 = true, appContext = context)

        return runBlocking {
            CoroutineScope(Dispatchers.IO).async {
                Client().create(account = EthOSSigningKey(walletSDK), options = options)
            }.await()
        }
    }

    private class EthOSSigningKey(private val walletSDK: WalletSDK) : SigningKey {
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



}