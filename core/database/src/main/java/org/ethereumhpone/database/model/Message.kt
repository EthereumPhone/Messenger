package org.ethereumhpone.database.model

import android.provider.Telephony
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity("message")
data class Message(

    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val threadId: Long = 0,
    // MMS-SMS are stored in separate tables in Android and thus can return the same id.
    // this contentId should be used if fetching from the content provider is needed.
    val contentId: Long = 0,
    val address: String = "",
    val boxId: Int = 0,
    val type: String = "",
    val date: Long = 0,
    val dateSent: Long = 0,
    val seen: Boolean = false,
    val read: Boolean = false,
    val locked: Boolean = false,
    val subId: Int = -1,

    // SMS only
    val body: String = "",
    val errorCode: Int = 0,
    val deliveryStatus: Int = Telephony.Sms.STATUS_NONE,

    //MMS ONLY
    val attachmentTypeString: String = AttachmentType.NOT_LOADED.toString(),
    val attachmentType: AttachmentType = AttachmentType.NOT_LOADED,

    val mmsDeliveryStatusString: String = "",
    val readReportString: String = "",
    val errorType: Int = 0,
    val messageSize: Int = 0,
    val messageType: Int = 0,
    val mmsStatus: Int = 0,
    val subject: String = "",
    val textContentType: String = "",
    val parts: List<MmsPart> = emptyList()

) {
    enum class AttachmentType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        SLIDESHOW,
        NOT_LOADED
    }

    fun isMms(): Boolean = type == "mms"

    fun isSms(): Boolean = type == "sms"

    fun isMe(): Boolean {
        val isIncomingMms = isMms() && (boxId == Telephony.Mms.MESSAGE_BOX_INBOX || boxId == Telephony.Mms.MESSAGE_BOX_ALL)
        val isIncomingSms = isSms() && (boxId == Telephony.Sms.MESSAGE_TYPE_INBOX || boxId == Telephony.Sms.MESSAGE_TYPE_ALL)

        return !(isIncomingMms || isIncomingSms)
    }

    fun isOutgoingMessage(): Boolean {
        val isOutgoingMms = isMms() && boxId == Telephony.Mms.MESSAGE_BOX_OUTBOX
        val isOutgoingSms = isSms() && (boxId == Telephony.Sms.MESSAGE_TYPE_FAILED
                || boxId == Telephony.Sms.MESSAGE_TYPE_OUTBOX
                || boxId == Telephony.Sms.MESSAGE_TYPE_QUEUED)

        return isOutgoingMms || isOutgoingSms
    }

    fun getText(): String {
        return when {
            isSms() -> body

            else -> parts
                .filter { it.type == "text/plain" }
                .mapNotNull { it.text }
                .joinToString("\n") { text -> text }
        }
    }

    fun getSummary(): String = when {
        isSms() -> body

        else -> {
            val sb = StringBuilder()

            getCleansedSubject().takeIf { it.isNotEmpty() }?.run(sb::appendLine)
            parts.mapNotNull { it.getSummary() }.forEach { summary -> sb.appendLine(summary) }

            sb.toString().trim()
        }
    }
    fun getCleansedSubject(): String {
        val uselessSubjects = listOf("no subject", "NoSubject", "<not present>")

        return if (uselessSubjects.contains(subject)) "" else subject
    }

    fun isFailedMessage(): Boolean {
        val isFailedMms = isMms() && (errorType >= Telephony.MmsSms.ERR_TYPE_GENERIC_PERMANENT || boxId == Telephony.Mms.MESSAGE_BOX_FAILED)
        val isFailedSms = isSms() && boxId == Telephony.Sms.MESSAGE_TYPE_FAILED
        return isFailedMms || isFailedSms
    }

    fun isSending(): Boolean {
        return !isFailedMessage() && isOutgoingMessage()
    }

    fun compareSender(other: Message): Boolean = when {
        isMe() && other.isMe() -> subId == other.subId
        !isMe() && !other.isMe() -> subId == other.subId && address == other.address
        else -> false
    }

    fun isDelivered(): Boolean {
        val isDeliveredMms = false // TODO
        val isDeliveredSms = deliveryStatus == Telephony.Sms.STATUS_COMPLETE
        return isDeliveredMms || isDeliveredSms
    }

}
