package org.ethereumhpone.data.repository

import android.app.Application
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.model.SearchResult
import org.ethereumhpone.domain.repository.ConversationRepository

class ConversationRepositoryImpl(
    private val context: Application

): ConversationRepository {
    override fun getConversations(archived: Boolean): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override fun getConversations(vararg threadIds: Long): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override fun getConversationsSnapShot(): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override fun getTopConversations(): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun setConversationName(id: Long, name: String) {
        TODO("Not yet implemented")
    }

    override fun searchConversations(query: CharSequence): Flow<List<SearchResult>> {
        TODO("Not yet implemented")
    }

    override fun getBlockedConversations(): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override fun getConversation(threadId: Long): Flow<Conversation?> {
        TODO("Not yet implemented")
    }

    override fun getUnmanagedConversations(): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override fun getRecipients(): Flow<List<Recipient>> {
        TODO("Not yet implemented")
    }

    override fun getUnmanagedRecipients(): Flow<List<Recipient>> {
        TODO("Not yet implemented")
    }

    override fun getRecipient(recipientId: Long): Flow<Recipient?> {
        TODO("Not yet implemented")
    }

    override fun getThreadId(recipient: String): Flow<Long?> {
        TODO("Not yet implemented")
    }

    override fun getThreadId(recipients: Collection<String>): Flow<Long?> {
        TODO("Not yet implemented")
    }

    override fun getOrCreateConversation(threadId: Long): Flow<Conversation?> {
        TODO("Not yet implemented")
    }

    override fun getOrCreateConversation(address: String): Flow<Conversation?> {
        TODO("Not yet implemented")
    }

    override fun getOrCreateConversation(addresses: List<String>): Flow<Conversation?> {
        TODO("Not yet implemented")
    }

    override suspend fun saveDraft(threadId: Long, draft: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateConversations(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markArchived(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markUnarchived(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markPinned(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markUnpinned(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markBlocked(
        threadIds: List<Long>,
        blockingClient: Int,
        blockReason: String?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun markUnblocked(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteConversations(vararg threadIds: Long) {
        TODO("Not yet implemented")
    }


}