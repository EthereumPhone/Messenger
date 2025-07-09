package org.ethereumhpone.messenger.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Nullable
import com.google.protobuf.ByteString
import com.messenger.terminalsdk.TerminalSDK
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.domain.model.ClientWrapper
import org.ethereumhpone.domain.model.LogTimeHandler
import org.ethereumhpone.domain.model.XMTPConversationHandler
import org.ethereumhpone.domain.model.XMTPPrivateKeyHandler
import org.ethereumhpone.messenger.BuildConfig
import org.ethereumphone.walletsdk.WalletSDK
import org.kethereum.ens.ENS
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    fun chainToApiKey(): String = BuildConfig.ALCHEMY_API

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
        val apiKey = chainToApiKey()
        return if (apiKey.isNullOrBlank()) {
            // Fallback to a public RPC if the Alchemy key is missing to avoid runtime failures.
            when (chainId) {
                1 -> "https://cloudflare-eth.com"            // Mainnet
                11155111 -> "https://ethereum-sepolia.publicnode.com" // Sepolia
                else -> "https://cloudflare-eth.com" // Default to mainnet public RPC
            }
        } else {
            "https://${chainIdToName(chainId)}.g.alchemy.com/v2/$apiKey"
        }
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
        val walletSDK = WalletSDK(
            context,
            bundlerRPCUrl= "https://api.pimlico.io/v2/1/rpc?apikey=${BuildConfig.BUNDLER_API}",
            web3jInstance = Web3j.build(HttpService(chainIdToRPC(1)))
        )

        return walletSDK
    }

    @Provides
    @Singleton
    fun provideENSResolver(): ENS {
        return ENS(HttpEthereumRPC(chainIdToRPC(1)))
    }

    @Singleton
    @Provides
    fun provideXmtpClientManger(): XmtpClientManager = XmtpClientManager

    @Singleton
    @Provides
    fun provideXmtpConversationHandler(
    ): XMTPConversationHandler {
        return XMTPConversationHandler()
    }


    @Singleton
    @Provides
    fun provideCoroutineScope(
    ): XMTPConversationHandler {
        return XMTPConversationHandler()
    }

    @Provides
    @Singleton
    fun provideTerminalSDK(
        @ApplicationContext context: Context
    ): TerminalSDK? {
        return try {
            TerminalSDK(context)
        } catch (e: Exception) {
            // If the underlying proxy class is not available on the device, gracefully fall back
            null
        }
    }


}

