package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.common.util.Result
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.database.model.relation.ConversationWithLastMessage
import org.ethereumhpone.domain.model.SearchResult

interface ConversationRepository {

    fun getConversations(archived: Boolean = false): Flow<List<ConversationEntity>>
    fun getConversationsSnapShot(): Flow<List<ConversationEntity>>
    fun getTopConversations(): Flow<List<ConversationEntity>>
    fun getCompleteConversations(): Flow<List<ConversationWithLastMessage>>

    // for group chats
    suspend fun setConversationName(id: Long, name: String)
    fun searchConversations(query: CharSequence): Flow<List<SearchResult>>
    fun getBlockedConversations(): Flow<List<ConversationEntity>>
    fun getConversation(threadId: Long): Flow<ConversationEntity?>
    fun getConversations(vararg threadIds: Long): Flow<List<ConversationEntity>>
    fun getUnmanagedConversations(): Flow<List<ConversationEntity>>
    fun getRecipients(): Flow<List<RecipientEntity>>
    fun getUnmanagedRecipients(): Flow<List<RecipientEntity>>
    fun getRecipient(recipientId: Long): Flow<RecipientEntity?>
    fun getThreadId(recipient: String): Flow<Long?>
    fun getThreadId(recipients: Collection<String>): Flow<Long?>
    fun getOrCreateConversation(addresses: List<String>): Flow<Result<ConversationEntity>>
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