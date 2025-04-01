package org.ethereumhpone.domain.repository

import android.net.Uri
import android.provider.Telephony
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.domain.model.Attachment
import java.io.File

interface MessageRepository {

    fun getMessages(threadId: Long): Flow<List<Message>>
    fun getMessage(id: String): Flow<Message?>
    fun getUnreadCount(): Flow<Long>
    suspend fun savePart(id: String): Uri?
    suspend fun getUnreadUnseenMessages(threadId: Long): List<Message>
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