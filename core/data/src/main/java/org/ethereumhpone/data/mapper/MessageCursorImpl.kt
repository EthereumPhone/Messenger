package org.ethereumhpone.data.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import org.ethereumhpone.common.mms.pdu_alt.EncodedStringValue
import org.ethereumhpone.common.mms.pdu_alt.PduHeaders
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.mapper.MessageCursor
import org.ethereumhpone.domain.mapper.PartCursor
import javax.inject.Inject

class MessageCursorImpl @Inject constructor(
    private val context: Context,
    private val partCursor: PartCursor,
    private val permissionManager: PermissionManager,

) : MessageCursor {

    private val uri = Uri.parse("content://mms-sms/complete-conversations")
    private val projection = arrayOf(
        Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        Telephony.MmsSms._ID,
        Telephony.Mms.DATE,
        Telephony.Mms.DATE_SENT,
        Telephony.Mms.READ,
        Telephony.Mms.THREAD_ID,
        Telephony.Mms.LOCKED,

        Telephony.Sms.ADDRESS,
        Telephony.Sms.BODY,
        Telephony.Sms.SEEN,
        Telephony.Sms.TYPE,
        Telephony.Sms.STATUS,
        Telephony.Sms.ERROR_CODE,

        Telephony.Mms.SUBJECT,
        Telephony.Mms.SUBJECT_CHARSET,
        Telephony.Mms.SEEN,
        Telephony.Mms.MESSAGE_TYPE,
        Telephony.Mms.MESSAGE_BOX,
        Telephony.Mms.DELIVERY_REPORT,
        Telephony.Mms.READ_REPORT,
        Telephony.MmsSms.PendingMessages.ERROR_TYPE,
        Telephony.Mms.STATUS
    )

    override fun getMessagesCursor(): Cursor? {
        TODO("Not yet implemented")
    }

    override fun getMessageCursor(id: Long): Cursor? {
        TODO("Not yet implemented")
    }

    override fun map(from: Pair<Cursor, MessageCursor.MessageColumns>): Message {
        val cursor = from.first
        val columnsMap = from.second

        val type = when {
            cursor.getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN) != -1 -> cursor.getString(columnsMap.msgType)
            cursor.getColumnIndex(Telephony.Mms.SUBJECT) != -1 -> "mms"
            cursor.getColumnIndex(Telephony.Sms.ADDRESS) != -1 -> "sms"
            else -> "unknown"
        }


        val message = Message(
            type = type,
            threadId = cursor.getLong(columnsMap.threadId),
            contentId = cursor.getLong(columnsMap.msgId),
            date = cursor.getLong(columnsMap.date),
            dateSent = cursor.getLong(columnsMap.dateSent),
            read = cursor.getInt(columnsMap.read) != 0,
            locked = cursor.getInt(columnsMap.locked) != 0,
            subId = if (columnsMap.subId != -1) cursor.getInt(columnsMap.subId) else -1
        )

        return when (type) {
            "sms" -> message.copy(
                address = cursor.getString(columnsMap.smsAddress) ?: "",
                boxId = cursor.getInt(columnsMap.smsType),
                seen = cursor.getInt(columnsMap.smsSeen) != 0,
                body = columnsMap.smsBody
                    .takeIf { column -> column != -1 } // The column may not be set
                    ?.let { column -> cursor.getString(column) } ?: "", // cursor.getString() may return null
                errorCode = cursor.getInt(columnsMap.smsErrorCode),
                deliveryStatus = cursor.getInt(columnsMap.smsStatus),
            )

            "mms" -> message.copy(
                address = getMmsAddress(message.contentId),
                boxId = cursor.getInt(columnsMap.mmsMessageBox),
                date = message.date * 1000L,
                dateSent = message.dateSent * 1000L,
                seen = cursor.getInt(columnsMap.mmsSeen) != 0,
                mmsDeliveryStatusString = cursor.getString(columnsMap.mmsDeliveryReport) ?: "",
                errorType = if (columnsMap.mmsErrorType != -1) cursor.getInt(columnsMap.mmsErrorType) else 0,
                messageSize = 0,
                readReportString = cursor.getString(columnsMap.mmsReadReport) ?: "",
                messageType = cursor.getInt(columnsMap.mmsMessageType),
                mmsStatus = cursor.getInt(columnsMap.mmsStatus),
                subject = cursor.getString(columnsMap.mmsSubject)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { EncodedStringValue(cursor.getInt(columnsMap.mmsSubjectCharset), it.toByteArray()).string } ?: "",
                textContentType = "",
                attachmentType = Message.AttachmentType.NOT_LOADED,
                attachmentTypeString = Message.AttachmentType.NOT_LOADED.toString()
            )
            else -> message // when unknown
        }
    }

    private fun getMmsAddress(messageId: Long): String {
        val uri = Telephony.Mms.CONTENT_URI.buildUpon()
            .appendPath(messageId.toString())
            .appendPath("addr").build()

        //TODO: Use Charset to ensure address is decoded correctly
        val projection = arrayOf(Telephony.Mms.Addr.ADDRESS, Telephony.Mms.Addr.CHARSET)
        val selection = "${Telephony.Mms.Addr.TYPE} = ${PduHeaders.FROM}"

        val cursor = context.contentResolver.query(uri, projection, selection, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getString(0) ?: ""
            }
        }

        return ""
    }
}