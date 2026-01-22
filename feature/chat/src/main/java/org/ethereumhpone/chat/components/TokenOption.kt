package org.ethereumhpone.chat.components

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Data class representing a token option for transaction flows.
 */
data class TokenOption(
    val symbol: String,
    val name: String,
    val decimals: Int,
    val contractAddress: String? = null, // null for native token
    val chainId: Long,
    val logoUrl: String? = null,
    val balance: BigDecimal = BigDecimal.ZERO,
    val price: Double = 0.0
) {
    val fiatValue: Double
        get() = balance.toDouble() * price

    val formattedBalance: String
        get() = balance.setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString()

    val formattedFiatValue: String
        get() = String.format("%.2f", fiatValue)
}
