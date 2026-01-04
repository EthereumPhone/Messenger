package org.ethereumphone.model

import kotlinx.serialization.Serializable

/**
 * Represents an XMTP transaction request (wallet_sendCalls).
 * Based on EIP-5792 batch transaction format.
 * 
 * @property chainId The chain ID for the transaction (e.g., 1 for Ethereum mainnet, 8453 for Base)
 * @property calls List of transaction calls to execute
 * @property metadata Optional metadata about the transaction
 */
@Serializable
data class TransactionRequest(
    val chainId: Long,
    val calls: List<TransactionCall>,
    val metadata: TransactionMetadata? = null
)

/**
 * A single transaction call within a batch.
 * 
 * @property to Target contract/wallet address
 * @property value Amount of native token in wei (as hex string)
 * @property data Encoded call data (as hex string)
 */
@Serializable
data class TransactionCall(
    val to: String,
    val value: String = "0x0",
    val data: String = "0x"
)

/**
 * Optional metadata about the transaction request.
 * 
 * @property description Human-readable description of what the transaction does
 * @property tokenSymbol Symbol of the token being transferred (e.g., "ETH", "USDC")
 * @property tokenAmount Human-readable amount (e.g., "0.1")
 * @property tokenDecimals Token decimals for display purposes
 */
@Serializable
data class TransactionMetadata(
    val description: String? = null,
    val tokenSymbol: String? = null,
    val tokenAmount: String? = null,
    val tokenDecimals: Int? = null
)

/**
 * Status of a transaction request.
 */
enum class TransactionRequestStatus {
    PENDING,      // Not yet executed
    EXECUTING,    // Currently being executed
    SUCCESS,      // Successfully executed
    FAILED,       // Execution failed
    REJECTED      // User rejected the transaction
}

