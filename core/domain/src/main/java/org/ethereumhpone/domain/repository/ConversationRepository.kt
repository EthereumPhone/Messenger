package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.common.util.Result
import org.ethereumphone.model.Conversation

interface ConversationRepository {

    fun getConversations(): Flow<List<Conversation>>
    fun getConversation(conversationId: String): Flow<Conversation?>
    fun getUnreadConversations(): Flow<List<Conversation>>
    
    /**
     * Gets conversations with unread messages for notification purposes.
     * Includes both ALLOWED and UNKNOWN consent state conversations.
     * Only excludes BLOCKED conversations.
     */
    fun getUnreadConversationsForNotifications(): Flow<List<Conversation>>
    
    /**
     * Creates a new conversation - either DM (single address) or Group (multiple addresses).
     * For groups, an optional groupName can be provided.
     */
    fun createConversation(
        addresses: List<String>,
        preResolvedAddresses: Map<String, String>? = null,
        groupName: String? = null,
        groupDescription: String? = null,
        groupImageUrl: String? = null
    ): Flow<Result<Conversation>>
    
    /**
     * Updates the name of a group conversation.
     */
    suspend fun updateGroupName(conversationId: String, name: String): Result<Unit>
    
    /**
     * Updates the description of a group conversation.
     */
    suspend fun updateGroupDescription(conversationId: String, description: String): Result<Unit>
    
    /**
     * Updates the image URL of a group conversation.
     */
    suspend fun updateGroupImageUrl(conversationId: String, imageUrl: String): Result<Unit>
    
    /**
     * Adds members to a group conversation.
     */
    suspend fun addGroupMembers(conversationId: String, addresses: List<String>): Result<Unit>
    
    /**
     * Removes members from a group conversation.
     */
    suspend fun removeGroupMembers(conversationId: String, inboxIds: List<String>): Result<Unit>
    
    /**
     * Leaves a group conversation.
     */
    suspend fun leaveGroup(conversationId: String): Result<Unit>
    
    /**
     * Checks if the current user is an admin of the group conversation.
     * Returns false for non-group conversations.
     */
    suspend fun isGroupAdmin(conversationId: String): Boolean
    
    suspend fun updatePinnedConversation(id: String, pinned: Boolean)
    suspend fun updateArchivedConversation(id: String, archived: Boolean)
    suspend fun updateBlockedConversation(id: String, blocked: Boolean)
    suspend fun updateSeenConversation(id: String, seen: Boolean)
    suspend fun deleteConversation(id: String)

}