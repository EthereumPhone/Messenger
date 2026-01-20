package org.ethereumphone.model

import kotlinx.serialization.Serializable

/**
 * Represents an XMTP transaction reference (transaction_reference).
 * Used to share a reference to a completed on-chain transaction.
 * Based on XIP-21 specification.
 * 
 * @property namespace The namespace for the transaction (e.g., "eip155" for EVM chains)
 * @property networkId The chain/network ID
 * @property reference The transaction hash
 * @property metadata Optional metadata about the transaction
 */
@Serializable
data class TransactionReference(
    val namespace: String = "eip155",
    val networkId: Long,
    val reference: String,
    val metadata: TransactionReferenceMetadata? = null
)

/**
 * Metadata about a completed transaction reference.
 * 
 * @property transactionType Type of transaction (transfer, swap, mint, lend, etc.)
 * @property currency Token/currency symbol
 * @property amount Amount in base units (integer)
 * @property decimals Token decimals for display
 * @property fromAddress Sender address
 * @property toAddress Recipient address
 * @property tokenContract Contract address for ERC20 tokens (null for native transfers)
 * @property blockExplorerUrl Optional URL to view on block explorer
 */
@Serializable
data class TransactionReferenceMetadata(
    val transactionType: String? = null,
    val currency: String? = null,
    val amount: Long? = null,
    val decimals: Int? = null,
    val fromAddress: String? = null,
    val toAddress: String? = null,
    val tokenContract: String? = null,
    val blockExplorerUrl: String? = null
)

/**
 * Transaction types for transaction references.
 */
object TransactionTypes {
    const val TRANSFER = "transfer"
    const val SWAP = "swap"
    const val MINT = "mint"
    const val LEND = "lend"
    const val BORROW = "borrow"
    const val STAKE = "stake"
    const val UNSTAKE = "unstake"
    const val APPROVE = "approve"
    const val CONTRACT_INTERACTION = "contract_interaction"
}
