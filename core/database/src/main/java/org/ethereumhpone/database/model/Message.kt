package org.ethereumhpone.database.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.xmtp.android.library.libxmtp.DecodedMessage

@Entity("message",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
@Serializable
data class Message(
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
    val xmtpDeliveryStatus: DecodedMessage.MessageDeliveryStatus = DecodedMessage.MessageDeliveryStatus.PUBLISHED,
    val isMe: Boolean = false,
) {
    enum class AttachmentType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        SLIDESHOW,
        NOT_LOADED
    }

    fun getSummary(): String = body //TODO: Change this

    fun isFailedMessage(): Boolean = xmtpDeliveryStatus == DecodedMessage.MessageDeliveryStatus.FAILED

    fun isDelivered(): Boolean = xmtpDeliveryStatus == DecodedMessage.MessageDeliveryStatus.PUBLISHED

}
