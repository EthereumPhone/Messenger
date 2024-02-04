package org.ethereumhpone.domain.repository

import android.os.Message
import android.provider.Telephony
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.domain.model.Attachment

interface MessageRepository {

    fun getMessages(threadId: Long, query: String): Flow<List<Message>>
    fun getMessage(id: Long): Flow<Message?>
    fun getMessageForPart(id: Long): Flow<Message?>
    fun getLastIncomingMessage(
        threadId: Long,
        smsInboxTypes: IntArray = intArrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX, Telephony.Sms.MESSAGE_TYPE_ALL),
        mmsInboxTypes: IntArray = intArrayOf(Telephony.Mms.MESSAGE_BOX_INBOX, Telephony.Mms.MESSAGE_BOX_ALL)
    ): Flow<Message>
    fun getUnreadCount(): Flow<Long>
    fun getPart(id: Long): Flow<MmsPart?>
    fun getPartsForConversation(threadId: Long): Flow<List<MmsPart>>
    fun savePart()
    fun insertSms()
    fun markAllSeen()
    fun markSeen(threadId: Long)
    fun markRead(vararg threadIds: Long)
    fun markUnread(vararg threadIds: Long)
    suspend fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>
    )
    suspend fun sendSms(message: Message)
    suspend fun resendMms(message: Message)
    suspend fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    )
    suspend fun insertReceivedSms(
        subId: Int,
        address: String,
        body: String,
        sentTime: Long
    )
    suspend fun markSending(id: Long)
    suspend fun markSent(id: Long)
    suspend fun markFailed(id: Long, resultCode: Int)
    suspend fun markDelivered(id: Long)
    suspend fun markDeliveryFailed(id: Long, resultCode: Int)
    suspend fun deleteMessage(vararg messageIds: Long)

}