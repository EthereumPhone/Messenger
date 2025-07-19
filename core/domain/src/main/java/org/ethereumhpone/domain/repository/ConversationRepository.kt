package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.common.util.Result
import org.ethereumphone.model.Conversation

interface ConversationRepository {

    fun getConversations(): Flow<List<Conversation>>
    fun getConversation(conversationId: String): Flow<Conversation?>
    fun getUnreadConversations(): Flow<List<Conversation>>
    fun createConversation(addresses: List<String>): Flow<Result<Conversation>>
    suspend fun updatePinnedConversation(id: String, pinned: Boolean)
    suspend fun updateArchivedConversation(id: String, archived: Boolean)
    suspend fun updateBlockedConversation(id: String, blocked: Boolean)
    suspend fun updateUnknownConversation(id: String, unknown: Boolean)
    suspend fun updateSeenConversation(id: String, seen: Boolean)
    suspend fun deleteConversation(id: String)

}