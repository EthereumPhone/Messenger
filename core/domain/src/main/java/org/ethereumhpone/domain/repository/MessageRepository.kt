package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.domain.model.Attachment
import org.ethereumphone.model.Message
import org.ethereumphone.model.Reaction
import org.xmtp.android.library.Conversation

interface MessageRepository {

    fun getMessages(threadId: String): Flow<List<Message>>
    fun getMessage(id: String): Flow<Message?>
    fun getUnreadCount(): Flow<Long>
    suspend fun canMessage(addresses: List<String>)
    suspend fun getUnreadUnseenMessages(threadId: String): List<Message>
    suspend fun markAllSeen()
    suspend fun markSeen(threadId: String)
    suspend fun markRead(vararg threadIds: String)
    suspend fun markUnread(vararg threadIds: String)
    suspend fun sendMessage(
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment>,
        reaction: Reaction?
    ): String?
    suspend fun sendMessageWithConversation(
        xmtpConversation: Conversation,
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment>,
        reaction: Reaction?
    ): String?
    suspend fun sendReadReceipt(timestamp: Long)
    suspend fun markSending(id: String)
    suspend fun markSent(id: String)
    suspend fun markFailed(id: String, resultCode: Int)
    suspend fun markDelivered(id: String)
    suspend fun deleteMessage(vararg messageIds: String)

}