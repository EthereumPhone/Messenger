package org.ethereumhpone.domain.repository

import android.net.Uri
import android.provider.Telephony
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.domain.model.Attachment
import java.io.File

interface MessageRepository {

    fun getMessages(threadId: Long, query: String = ""): Flow<List<Message>>
    fun getMessage(id: String): Flow<Message?>
    fun getMessageForPart(id: String): Flow<Message?>
    fun getLastIncomingMessage(
        threadId: Long,
        smsInboxTypes: IntArray = intArrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX, Telephony.Sms.MESSAGE_TYPE_ALL),
        mmsInboxTypes: IntArray = intArrayOf(Telephony.Mms.MESSAGE_BOX_INBOX, Telephony.Mms.MESSAGE_BOX_ALL)
    ): Flow<Message>
    fun getUnreadCount(): Flow<Long>
    fun getPart(id: String): Flow<MmsPart?>
    fun getPartsForConversation(threadId: String): Flow<List<MmsPart>>
    suspend fun savePart(id: String): Uri?
    suspend fun getUnreadUnseenMessages(threadId: Long): List<Message>
    suspend fun markAllSeen()
    suspend fun markSeen(threadId: Long)
    suspend fun markRead(vararg threadIds: Long)
    suspend fun markUnread(vararg threadIds: Long)
    suspend fun sendMessage(subId: Int, threadId: Long, addresses: List<String>, body: String, attachments: List<Attachment>)
    suspend fun sendSms(message: Message)
    suspend fun resendMms(message: Message)
    suspend fun insertSentSms(subId: Int, threadId: Long, address: String, body: String, date: Long): Message
    suspend fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Message
    suspend fun insertSentXmtp(subId: Int, threadId: Long, address: String, body: String, date: Long): Message
    suspend fun insertReceivedXmtp(subId: Int, addresses: List<String>, body: String, sentTime: Long): Message
    suspend fun markSending(id: String)
    suspend fun markSent(id: String)
    suspend fun markFailed(id: String, resultCode: Int)
    suspend fun markDelivered(id: String)
    suspend fun markDeliveryFailed(id: String, resultCode: Int)
    suspend fun deleteMessage(vararg messageIds: String)

}