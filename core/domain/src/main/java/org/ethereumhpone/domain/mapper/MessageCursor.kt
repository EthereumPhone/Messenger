package org.ethereumhpone.domain.mapper

import android.database.Cursor
import android.provider.Telephony
import org.ethereumhpone.database.model.Message
import java.util.Arrays

interface MessageCursor : Mapper<Pair<Cursor, MessageCursor.MessageColumns>, Message> {

    fun getMessagesCursor(): Cursor?

    fun getMessageCursor(id: Long): Cursor?

    class MessageColumns(private val cursor: Cursor) {

        val msgType by lazy { getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN) }
        val msgId by lazy { getColumnIndex(Telephony.MmsSms._ID) }
        val date by lazy { getColumnIndex(Telephony.Mms.DATE) }
        val dateSent by lazy { getColumnIndex(Telephony.Mms.DATE_SENT) }
        val read by lazy { getColumnIndex(Telephony.Mms.READ) }
        val threadId by lazy { getColumnIndex(Telephony.Mms.THREAD_ID) }
        val locked by lazy { getColumnIndex(Telephony.Mms.LOCKED) }
        val subId by lazy { getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID) }

        val smsAddress by lazy { getColumnIndex(Telephony.Sms.ADDRESS) }
        val smsBody by lazy { getColumnIndex(Telephony.Sms.BODY) }
        val smsSeen by lazy { getColumnIndex(Telephony.Sms.SEEN) }
        val smsType by lazy { getColumnIndex(Telephony.Sms.TYPE) }
        val smsStatus by lazy { getColumnIndex(Telephony.Sms.STATUS) }
        val smsErrorCode by lazy { getColumnIndex(Telephony.Sms.ERROR_CODE) }

        val mmsSubject by lazy { getColumnIndex(Telephony.Mms.SUBJECT) }
        val mmsSubjectCharset by lazy { getColumnIndex(Telephony.Mms.SUBJECT_CHARSET) }
        val mmsSeen by lazy { getColumnIndex(Telephony.Mms.SEEN) }
        val mmsMessageType by lazy { getColumnIndex(Telephony.Mms.MESSAGE_TYPE) }
        val mmsMessageBox by lazy { getColumnIndex(Telephony.Mms.MESSAGE_BOX) }
        val mmsDeliveryReport by lazy { getColumnIndex(Telephony.Mms.DELIVERY_REPORT) }
        val mmsReadReport by lazy { getColumnIndex(Telephony.Mms.READ_REPORT) }
        val mmsErrorType by lazy { getColumnIndex(Telephony.MmsSms.PendingMessages.ERROR_TYPE) }
        val mmsStatus by lazy { getColumnIndex(Telephony.Mms.STATUS) }

        private fun getColumnIndex(columnsName: String) = try {
            cursor.getColumnIndexOrThrow(columnsName)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
}