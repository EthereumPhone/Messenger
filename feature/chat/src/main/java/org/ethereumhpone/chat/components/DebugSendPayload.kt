package org.ethereumhpone.chat.components

/**
 * Minimal payload used to build a TransactionReference for sending.
 */
data class DebugSendPayload(
    val amount: String,
    val tokenSymbol: String,
    val tokenDecimals: Int,
    val chainId: Long,
    val description: String
)
