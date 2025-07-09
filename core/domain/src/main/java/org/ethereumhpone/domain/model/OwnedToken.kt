package org.ethereumhpone.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OwnedToken(
    val contractAddress: String,
    val decimals: Int,
    val name: String,
    val symbol: String,
    val logo: String? = null,
    val chainId: Int,
    val swappable: Boolean,
    val balance: String,
) 