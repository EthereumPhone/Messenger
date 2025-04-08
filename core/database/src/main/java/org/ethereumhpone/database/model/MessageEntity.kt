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
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
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
) {
    fun getSummary(): String = body //TODO: Change this

    fun isFailedMessage(): Boolean = deliveryStatus == DecodedMessage.MessageDeliveryStatus.FAILED

    fun isDelivered(): Boolean = deliveryStatus == DecodedMessage.MessageDeliveryStatus.PUBLISHED

}

fun MessageEntity.toExternalModel(recipient: Recipient): Message = 
    Message(
        id = id,
        threadId = threadId,
        recipient = recipient,
        date = Instant.fromEpochSeconds(date),
        dateSent = Instant.fromEpochSeconds(dateSent),
        seen = seen,
        deliveryStatus = DeliveryStatus.valueOf(deliveryStatus.name),
        replyReference = replyReference,
        isMe = isMe,
        attachments = emptyList(),
        reactions = emptyList(),
        body = body
    )

