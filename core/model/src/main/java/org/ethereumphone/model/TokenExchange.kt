package org.ethereumphone.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenExchange(
    val symbol: String,
    val currency: String,
    val value: Double,
    val timestamp: Long
) 