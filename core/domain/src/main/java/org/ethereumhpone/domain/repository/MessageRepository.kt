package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.domain.model.Attachment

interface MessageRepository {

    fun getMessages(threadId: Long): Flow<List<MessageEntity>>
    fun getMessage(id: String): Flow<MessageEntity?>
    fun getUnreadCount(): Flow<Long>
    suspend fun canMessage(addresses: List<String>)
    suspend fun getUnreadUnseenMessages(threadId: Long): List<MessageEntity>
    suspend fun markAllSeen()
    suspend fun markSeen(threadId: Long)
    suspend fun markRead(vararg threadIds: Long)
    suspend fun markUnread(vararg threadIds: Long)
    suspend fun sendMessage(subId: Int, threadId: Long, addresses: List<String>, body: String, attachments: List<Attachment>)
    suspend fun markSending(id: String)
    suspend fun markSent(id: String)
    suspend fun markFailed(id: String, resultCode: Int)
    suspend fun markDelivered(id: String)
    suspend fun deleteMessage(vararg messageIds: String)

}