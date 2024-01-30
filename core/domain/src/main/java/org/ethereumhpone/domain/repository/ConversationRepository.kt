package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.SearchResult

interface ConversationRepository {

    fun getConversations(archived: Boolean = false): Flow<List<Conversation>>
    fun getConversationsSnapShot(): List<Conversation>
    fun getTopConversations(): List<Conversation>

    // for group chats
    fun setConversationName(id: Long, name: String)
    fun searchConversations(query: CharSequence): List<SearchResult>
    fun getBlockedConversations(): Flow<List<Conversation>>
    fun getConversation(threadId: Long): Conversation?
    fun getConversations(vararg threadIds: Long): Flow<List<Conversation>>
    fun getUnmanagedConversations(): Flow<List<Conversation>>
    fun getRecipients(): Flow<List<Recipient>>
    fun getUnmanagedRecipients(): Flow<List<Recipient>>
    fun getRecipient(recipientId: Long): Recipient?
    fun getThreadId(recipient: String): Long?
    fun getThreadId(recipients: Collection<String>): Long?
    fun getOrCreateConversation(threadId: Long): Conversation?
    fun getOrCreateConversation(address: String): Conversation?
    fun getOrCreateConversation(addresses: List<String>): Conversation?
    fun saveDraft(threadId: Long, draft: String)
    fun updateConversations(vararg threadIds: Long)
    fun markArchived(vararg threadIds: Long)
    fun markUnarchived(vararg threadIds: Long)
    fun markPinned(vararg threadIds: Long)
    fun markUnpinned(vararg threadIds: Long)
    fun markBlocked(threadIds: List<Long>, blockingClient: Int, blockReason: String?)
    fun markUnblocked(vararg threadIds: Long)
    fun deleteConversations(vararg threadIds: Long)
}