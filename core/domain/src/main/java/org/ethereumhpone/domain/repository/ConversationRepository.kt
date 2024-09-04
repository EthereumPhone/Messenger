package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.SearchResult

interface ConversationRepository {

    fun getConversations(archived: Boolean = false): Flow<List<Conversation>>
    fun getConversationsSnapShot(): Flow<List<Conversation>>
    fun getTopConversations(): Flow<List<Conversation>>

    // for group chats
    suspend fun setConversationName(id: Long, name: String)
    fun searchConversations(query: CharSequence): Flow<List<SearchResult>>
    fun getBlockedConversations(): Flow<List<Conversation>>
    fun getConversation(threadId: Long): Flow<Conversation?>
    fun getConversations(vararg threadIds: Long): Flow<List<Conversation>>
    fun getUnmanagedConversations(): Flow<List<Conversation>>
    fun getRecipients(): Flow<List<Recipient>>
    fun getUnmanagedRecipients(): Flow<List<Recipient>>
    fun getRecipient(recipientId: Long): Flow<Recipient?>
    fun getThreadId(recipient: String): Flow<Long?>
    fun getThreadId(recipients: Collection<String>): Flow<Long?>
    fun getOrCreateConversation(threadId: Long): Flow<Conversation?>
    fun getOrCreateConversation(address: String): Flow<Conversation?>
    fun getOrCreateConversation(addresses: List<String>): Flow<Conversation?>
    suspend fun saveDraft(threadId: Long, draft: String)
    suspend fun updateConversations(vararg threadIds: Long)
    suspend fun markArchived(vararg threadIds: Long)
    suspend fun markRead(threadId: Long)
    suspend fun markUnarchived(vararg threadIds: Long)
    suspend fun markPinned(vararg threadIds: Long)
    suspend fun markUnpinned(vararg threadIds: Long)
    suspend fun markBlocked(threadIds: List<Long>, blockingClient: Int, blockReason: String?)
    suspend fun markUnblocked(vararg threadIds: Long)
    suspend fun deleteConversations(vararg threadIds: Long)
    suspend fun markAccepted(threadId: Long)

}