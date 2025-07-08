package org.ethereumhpone.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.ethereumphone.model.TokenAsset
import org.ethereumhpone.domain.repository.TokenRepository
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

class GetAllTokensUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val walletSDK: WalletSDK
) {
    operator fun invoke(): Flow<List<TokenAsset>> = flow {
        // Wait until a non-empty wallet address is available
        var address: String
        do {
            address = walletSDK.getAddress()
            if (address.isBlank()) {
                delay(100)
            }
        } while (address.isBlank())

        val chainId = walletSDK.getChainId()
        val assets = tokenRepository.getTokenAssets(address, chainId)
        emit(assets)
    }.flowOn(Dispatchers.IO)
} 