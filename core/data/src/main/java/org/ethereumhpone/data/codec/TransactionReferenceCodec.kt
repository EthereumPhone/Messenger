package org.ethereumhpone.data.codec

import com.google.protobuf.kotlin.toByteString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ethereumphone.model.TransactionReference
import org.ethereumphone.model.TransactionReferenceMetadata
import org.ethereumphone.model.TransactionTypes
import org.xmtp.android.library.codecs.ContentCodec
import org.xmtp.android.library.codecs.ContentTypeId
import org.xmtp.android.library.codecs.ContentTypeIdBuilder
import org.xmtp.proto.message.contents.Content

/**
 * Content type ID for transaction references.
 * Based on XIP-21 XMTP content type conventions.
 */
val ContentTypeTransactionReference = ContentTypeIdBuilder.builderFromAuthorityId(
    authorityId = "xmtp.org",
    typeId = "transactionReference",
    versionMajor = 1,
    versionMinor = 0
)

/**
 * XMTP codec for encoding and decoding transaction references.
 * Allows sharing completed on-chain transaction details via XMTP messages.
 */
class TransactionReferenceCodec : ContentCodec<TransactionReference> {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    override val contentType: ContentTypeId = ContentTypeTransactionReference

    override fun encode(content: TransactionReference): Content.EncodedContent {
        val jsonString = json.encodeToString(content)
        return Content.EncodedContent.newBuilder()
            .setType(
                Content.ContentTypeId.newBuilder()
                    .setAuthorityId(contentType.authorityId)
                    .setTypeId(contentType.typeId)
                    .setVersionMajor(contentType.versionMajor)
                    .setVersionMinor(contentType.versionMinor)
                    .build()
            )
            .setContent(jsonString.toByteArray().toByteString())
            .build()
    }

    override fun decode(content: Content.EncodedContent): TransactionReference {
        val jsonString = content.content.toStringUtf8()
        return json.decodeFromString(TransactionReference.serializer(), jsonString)
    }

    override fun fallback(content: TransactionReference): String {
        val chainName = chainIdToName(content.networkId)
        val metadata = content.metadata
        
        // Capture nullable values to enable smart casting
        val amount = metadata?.amount
        val decimals = metadata?.decimals
        val currency = metadata?.currency
        val transactionType = metadata?.transactionType
        
        return when {
            amount != null && currency != null && decimals != null -> {
                val humanAmount = formatAmount(amount, decimals)
                val txType = transactionType?.capitalize() ?: "Transaction"
                "$txType: $humanAmount $currency on $chainName"
            }
            transactionType != null -> {
                "${transactionType.capitalize()} on $chainName"
            }
            else -> {
                "Transaction on $chainName: ${truncateHash(content.reference)}"
            }
        }
    }

    override fun shouldPush(content: TransactionReference): Boolean = true
    
    private fun chainIdToName(chainId: Long): String = when (chainId) {
        1L -> "Ethereum"
        10L -> "Optimism"
        137L -> "Polygon"
        42161L -> "Arbitrum"
        8453L -> "Base"
        11155111L -> "Sepolia"
        else -> "Chain $chainId"
    }
    
    private fun formatAmount(amount: Long, decimals: Int): String {
        val divisor = Math.pow(10.0, decimals.toDouble())
        val result = amount.toDouble() / divisor
        return if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            String.format("%.6f", result).trimEnd('0').trimEnd('.')
        }
    }
    
    private fun truncateHash(hash: String): String {
        return if (hash.length > 12) {
            "${hash.take(6)}...${hash.takeLast(4)}"
        } else {
            hash
        }
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

/**
 * Builder helper for creating TransactionReference objects.
 */
object TransactionReferenceBuilder {
    
    /**
     * Create a native token transfer reference.
     */
    fun nativeTransfer(
        networkId: Long,
        txHash: String,
        fromAddress: String,
        toAddress: String,
        amountWei: Long,
        tokenSymbol: String = "ETH"
    ): TransactionReference {
        return TransactionReference(
            namespace = "eip155",
            networkId = networkId,
            reference = txHash,
            metadata = TransactionReferenceMetadata(
                transactionType = TransactionTypes.TRANSFER,
                currency = tokenSymbol,
                amount = amountWei,
                decimals = 18,
                fromAddress = fromAddress,
                toAddress = toAddress,
                blockExplorerUrl = getBlockExplorerUrl(networkId, txHash)
            )
        )
    }
    
    /**
     * Create an ERC20 token transfer reference.
     */
    fun erc20Transfer(
        networkId: Long,
        txHash: String,
        tokenContract: String,
        fromAddress: String,
        toAddress: String,
        amount: Long,
        tokenSymbol: String,
        tokenDecimals: Int
    ): TransactionReference {
        return TransactionReference(
            namespace = "eip155",
            networkId = networkId,
            reference = txHash,
            metadata = TransactionReferenceMetadata(
                transactionType = TransactionTypes.TRANSFER,
                currency = tokenSymbol,
                amount = amount,
                decimals = tokenDecimals,
                fromAddress = fromAddress,
                toAddress = toAddress,
                tokenContract = tokenContract,
                blockExplorerUrl = getBlockExplorerUrl(networkId, txHash)
            )
        )
    }
    
    /**
     * Create a swap transaction reference.
     */
    fun swap(
        networkId: Long,
        txHash: String,
        fromAddress: String,
        tokenInSymbol: String,
        tokenOutSymbol: String,
        amountIn: Long,
        decimalsIn: Int
    ): TransactionReference {
        return TransactionReference(
            namespace = "eip155",
            networkId = networkId,
            reference = txHash,
            metadata = TransactionReferenceMetadata(
                transactionType = TransactionTypes.SWAP,
                currency = "$tokenInSymbol → $tokenOutSymbol",
                amount = amountIn,
                decimals = decimalsIn,
                fromAddress = fromAddress,
                blockExplorerUrl = getBlockExplorerUrl(networkId, txHash)
            )
        )
    }
    
    /**
     * Create a generic transaction reference.
     */
    fun generic(
        networkId: Long,
        txHash: String,
        transactionType: String? = null,
        fromAddress: String? = null,
        toAddress: String? = null
    ): TransactionReference {
        return TransactionReference(
            namespace = "eip155",
            networkId = networkId,
            reference = txHash,
            metadata = TransactionReferenceMetadata(
                transactionType = transactionType,
                fromAddress = fromAddress,
                toAddress = toAddress,
                blockExplorerUrl = getBlockExplorerUrl(networkId, txHash)
            )
        )
    }
    
    private fun getBlockExplorerUrl(networkId: Long, txHash: String): String = when (networkId) {
        1L -> "https://etherscan.io/tx/$txHash"
        10L -> "https://optimistic.etherscan.io/tx/$txHash"
        137L -> "https://polygonscan.com/tx/$txHash"
        42161L -> "https://arbiscan.io/tx/$txHash"
        8453L -> "https://basescan.org/tx/$txHash"
        11155111L -> "https://sepolia.etherscan.io/tx/$txHash"
        else -> ""
    }
}
