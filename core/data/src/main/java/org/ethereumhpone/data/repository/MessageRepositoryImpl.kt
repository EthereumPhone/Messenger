package org.ethereumhpone.data.repository

import android.os.Message
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.domain.manager.KeyManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.MessageRepository

class MessageRepositoryImpl(
    private val keyManager: KeyManager,
): MessageRepository {
    override fun getMessages(threadId: Long, query: String): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getMessage(id: Long): Flow<Message?> {
        TODO("Not yet implemented")
    }

    override fun getMessageForPart(id: Long): Flow<Message?> {
        TODO("Not yet implemented")
    }

    override fun getLastIncomingMessage(
        threadId: Long,
        smsInboxTypes: IntArray,
        mmsInboxTypes: IntArray
    ): Flow<Message> {
        TODO("Not yet implemented")
    }

    override fun getUnreadCount(): Flow<Long> {
        TODO("Not yet implemented")
    }

    override fun getPart(id: Long): Flow<MmsPart?> {
        TODO("Not yet implemented")
    }

    override fun getPartsForConversation(threadId: Long): Flow<List<MmsPart>> {
        TODO("Not yet implemented")
    }

    override fun savePart() {
        TODO("Not yet implemented")
    }

    override fun insertSms() {
        TODO("Not yet implemented")
    }

    override fun markAllSeen() {
        TODO("Not yet implemented")
    }

    override fun markSeen(threadId: Long) {
        TODO("Not yet implemented")
    }

    override fun markRead(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }

    override fun markUnread(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun sendSms(message: Message) {
        TODO("Not yet implemented")
    }

    override suspend fun resendMms(message: Message) {
        TODO("Not yet implemented")
    }

    override suspend fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun insertReceivedSms(
        subId: Int,
        address: String,
        body: String,
        sentTime: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun markSending(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markSent(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markFailed(id: Long, resultCode: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun markDelivered(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markDeliveryFailed(id: Long, resultCode: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(vararg messageIds: Long) {
        TODO("Not yet implemented")
    }

}