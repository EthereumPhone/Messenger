package org.ethereumhpone.data.codec

import com.google.protobuf.kotlin.toByteString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ethereumphone.model.TransactionCall
import org.ethereumphone.model.TransactionMetadata
import org.ethereumphone.model.TransactionRequest
import org.xmtp.android.library.codecs.ContentCodec
import org.xmtp.android.library.codecs.ContentTypeId
import org.xmtp.android.library.codecs.ContentTypeIdBuilder
import org.xmtp.proto.message.contents.Content

/**
 * Content type ID for transaction requests.
 * Based on XMTP content type conventions.
 */
val ContentTypeTransactionRequest = ContentTypeIdBuilder.builderFromAuthorityId(
    authorityId = "xmtp.org",
    typeId = "transactionRequest",
    versionMajor = 1,
    versionMinor = 0
)

/**
 * XMTP codec for encoding and decoding transaction requests.
 * Allows sending and receiving wallet_sendCalls style transaction requests via XMTP messages.
 */
class TransactionRequestCodec : ContentCodec<TransactionRequest> {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    override val contentType: ContentTypeId = ContentTypeTransactionRequest

    override fun encode(content: TransactionRequest): Content.EncodedContent {
        val jsonString = json.encodeToString(content)
        // Generate user-friendly fallback message for clients that don't support this content type
        val fallbackText = buildFallbackMessage(content)
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
            .setFallback(fallbackText)
            .build()
    }
    
    /**
     * Build a user-friendly fallback message for clients that don't support TransactionRequest.
     * This is included in the encoded content so older clients display this text.
     */
    private fun buildFallbackMessage(content: TransactionRequest): String {
        val chainName = chainIdToName(content.chainId)
        val metadata = content.metadata
        
        return if (metadata?.tokenAmount != null && metadata.tokenSymbol != null) {
            "I'm requesting ${metadata.tokenAmount} ${metadata.tokenSymbol} from you on $chainName"
        } else {
            "I'm requesting a transaction from you on $chainName"
        }
    }

    override fun decode(content: Content.EncodedContent): TransactionRequest {
        val jsonString = content.content.toStringUtf8()
        return json.decodeFromString(TransactionRequest.serializer(), jsonString)
    }

    override fun fallback(content: TransactionRequest): String {
        val chainName = chainIdToName(content.chainId)
        val callCount = content.calls.size
        
        val metadata = content.metadata
        return if (metadata?.tokenAmount != null && metadata.tokenSymbol != null) {
            "Transaction Request: Send ${metadata.tokenAmount} ${metadata.tokenSymbol} on $chainName ($callCount call${if (callCount > 1) "s" else ""})"
        } else {
            "Transaction Request: $callCount call${if (callCount > 1) "s" else ""} on $chainName"
        }
    }

    override fun shouldPush(content: TransactionRequest): Boolean = true
    
    private fun chainIdToName(chainId: Long): String = when (chainId) {
        1L -> "Ethereum"
        10L -> "Optimism"
        137L -> "Polygon"
        42161L -> "Arbitrum"
        8453L -> "Base"
        11155111L -> "Sepolia"
        else -> "Chain $chainId"
    }
}

/**
 * Builder helper for creating TransactionRequest objects.
 */
object TransactionRequestBuilder {
    
    /**
     * Create a simple native token transfer request.
     */
    fun nativeTransfer(
        chainId: Long,
        to: String,
        valueWei: String,
        tokenSymbol: String = "ETH",
        humanReadableAmount: String? = null
    ): TransactionRequest {
        return TransactionRequest(
            chainId = chainId,
            calls = listOf(
                TransactionCall(
                    to = to,
                    value = valueWei,
                    data = "0x"
                )
            ),
            metadata = TransactionMetadata(
                description = "Native token transfer",
                tokenSymbol = tokenSymbol,
                tokenAmount = humanReadableAmount,
                tokenDecimals = 18
            )
        )
    }
    
    /**
     * Create an ERC20 token transfer request.
     */
    fun erc20Transfer(
        chainId: Long,
        tokenContract: String,
        to: String,
        amount: String,
        tokenSymbol: String,
        humanReadableAmount: String,
        tokenDecimals: Int
    ): TransactionRequest {
        // Encode ERC20 transfer function: transfer(address,uint256)
        val functionSelector = "a9059cbb" // keccak256("transfer(address,uint256)")[:8]
        val paddedTo = to.removePrefix("0x").lowercase().padStart(64, '0')
        val paddedAmount = amount.removePrefix("0x").padStart(64, '0')
        val data = "0x$functionSelector$paddedTo$paddedAmount"
        
        return TransactionRequest(
            chainId = chainId,
            calls = listOf(
                TransactionCall(
                    to = tokenContract,
                    value = "0x0",
                    data = data
                )
            ),
            metadata = TransactionMetadata(
                description = "ERC20 token transfer",
                tokenSymbol = tokenSymbol,
                tokenAmount = humanReadableAmount,
                tokenDecimals = tokenDecimals
            )
        )
    }
    
    /**
     * Create a custom contract interaction request.
     */
    fun contractCall(
        chainId: Long,
        to: String,
        data: String,
        value: String = "0x0",
        description: String? = null
    ): TransactionRequest {
        return TransactionRequest(
            chainId = chainId,
            calls = listOf(
                TransactionCall(
                    to = to,
                    value = value,
                    data = data
                )
            ),
            metadata = TransactionMetadata(
                description = description
            )
        )
    }
    
    /**
     * Create a batched transaction request with multiple calls.
     */
    fun batchedCalls(
        chainId: Long,
        calls: List<TransactionCall>,
        description: String? = null
    ): TransactionRequest {
        return TransactionRequest(
            chainId = chainId,
            calls = calls,
            metadata = TransactionMetadata(
                description = description
            )
        )
    }
}

