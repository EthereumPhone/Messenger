package org.ethereumhpone.database.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import org.xmtp.android.library.libxmtp.DecodedMessage

@Entity("message",

    /*
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
     */

)
@Serializable
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(index = true) val threadId: String,
    val senderInboxId: String,
    val date: Long = 0,
    val dateSent: Long = 0,
    val seen: Boolean = false,
    val read: Boolean = false,
    val locked: Boolean = false,
    val body: String,
    val replyReference: String?,
    val seenDate: Long = 0,
    val deliveryStatus: DecodedMessage.MessageDeliveryStatus = DecodedMessage.MessageDeliveryStatus.PUBLISHED,
    val isMe: Boolean = false,
    // Transaction request data (JSON serialized TransactionRequest)
    val transactionRequest: String? = null,
    // Transaction execution status: PENDING, EXECUTING, SUCCESS, FAILED, REJECTED
    val transactionStatus: String? = null,
    // Transaction hash after successful execution
    val transactionHash: String? = null,
    // Transaction reference data (JSON serialized TransactionReference)
    val transactionReference: String? = null,
) {
    fun getSummary(): String = when {
        transactionRequest != null -> "Transaction Request"
        transactionReference != null -> "Transaction"
        else -> body
    }

    fun isFailedMessage(): Boolean = deliveryStatus == DecodedMessage.MessageDeliveryStatus.FAILED

    fun isDelivered(): Boolean = deliveryStatus == DecodedMessage.MessageDeliveryStatus.PUBLISHED
    
    fun isTransactionRequest(): Boolean = transactionRequest != null
    
    fun isTransactionReference(): Boolean = transactionReference != null

}

fun MessageEntity.toExternalModel(recipient: Recipient): Message {
    // Parse transaction request if present
    val txRequest: org.ethereumphone.model.TransactionRequest? = transactionRequest?.let { json ->
        try {
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString(org.ethereumphone.model.TransactionRequest.serializer(), json)
        } catch (e: Exception) {
            null
        }
    }
    
    val txStatus = transactionStatus?.let { status ->
        try {
            org.ethereumphone.model.TransactionRequestStatus.valueOf(status)
        } catch (e: Exception) {
            null
        }
    }
    
    // Parse transaction reference if present
    val txReference: org.ethereumphone.model.TransactionReference? = transactionReference?.let { json ->
        try {
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString(org.ethereumphone.model.TransactionReference.serializer(), json)
        } catch (e: Exception) {
            null
        }
    }
    
    return Message(
        id = id,
        threadId = threadId,
        recipient = recipient,
        date = Instant.fromEpochMilliseconds(date),
        dateSent = Instant.fromEpochMilliseconds(dateSent),
        seen = seen,
        deliveryStatus = DeliveryStatus.valueOf(deliveryStatus.name),
        replyReference = replyReference,
        isMe = isMe,
        attachments = emptyList(),
        reactions = emptyList(),
        body = body,
        transactionRequest = txRequest,
        transactionStatus = txStatus,
        transactionHash = transactionHash,
        transactionReference = txReference
    )
}

