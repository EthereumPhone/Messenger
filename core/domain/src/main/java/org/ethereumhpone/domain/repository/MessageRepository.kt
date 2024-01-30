package org.ethereumhpone.domain.repository

import android.os.Message
import android.provider.Telephony
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.domain.model.Attachment

interface MessageRepository {

    fun getMessages(threadId: Long, query: String): Flow<List<Message>>
    fun getMessage(id: Long): Message?
    fun getMessageForPart(id: Long): Message?
    fun getLastIncomingMessage(
        threadId: Long,
        smsInboxTypes: IntArray = intArrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX, Telephony.Sms.MESSAGE_TYPE_ALL),
        mmsInboxTypes: IntArray = intArrayOf(Telephony.Mms.MESSAGE_BOX_INBOX, Telephony.Mms.MESSAGE_BOX_ALL)
    ): Flow<Message>
    fun getUnreadCount(): Long
    fun getPart(id: Long): MmsPart?
    fun getPartsForConversation(threadId: Long): Flow<List<MmsPart>>
    fun savePart()
    fun insertSms()
    fun markAllSeen()
    fun markSeen(threadId: Long)
    fun markRead(vararg threadIds: Long)
    fun markUnread(vararg threadIds: Long)
    fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>
    )
    fun sendSms(message: Message)
    fun resendMms(message: Message)
    fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ) : Message
    fun insertReceivedSms(
        subId: Int,
        address: String,
        body: String,
        sentTime: Long
    ) : Message
    fun markSending(id: Long)
    fun markSent(id: Long)
    fun markFailed(id: Long, resultCode: Int)
    fun markDelivered(id: Long)
    fun markDeliveryFailed(id: Long, resultCode: Int)
    fun deleteMessage(vararg messageIds: Long)

}