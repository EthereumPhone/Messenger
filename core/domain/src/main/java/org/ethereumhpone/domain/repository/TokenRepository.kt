package org.ethereumhpone.domain.repository

import org.ethereumphone.model.TokenAsset

interface TokenRepository {
    suspend fun getTokenAssets(address: String, chainId: Int): List<TokenAsset>
} 