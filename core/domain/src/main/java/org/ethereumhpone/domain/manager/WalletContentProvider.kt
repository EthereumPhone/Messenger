package org.ethereumhpone.domain.manager

import org.ethereumhpone.domain.model.OwnedToken
import org.ethereumhpone.domain.model.TokenMetadata

interface WalletContentProvider {
    fun getTokenMetadata(chainId: String, contractAddress: String): TokenMetadata?

    fun getTokensMetadataForChain(chainId: String): List<TokenMetadata>

    fun getOwnedToken(chainId: String, contractAddress: String): OwnedToken?

    fun getOwnedTokensForChain(chainId: String): List<OwnedToken>

    fun getAllOwnedTokens(): List<OwnedToken>
} 