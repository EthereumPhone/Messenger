package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.relation.CompositeConversation
import org.ethereumhpone.database.model.relation.ConversationRecipientCrossRef

@Dao
interface ConversationDao {

    @Transaction
    @Query("""
    SELECT 
        conversation.*,
        message.id AS message_id,
        message.threadId AS message_threadId,
        message.date AS message_date,
        message.body AS message_body
        -- Add all other MessageEntity fields here with 'message_' prefix
    FROM conversation
    LEFT JOIN message 
        ON message.id = (
            SELECT id FROM message 
            WHERE threadId = conversation.id 
            ORDER BY date DESC 
            LIMIT 1
        )
        WHERE conversation.id = :id
    """)
    fun getConversation(id: String): Flow<CompositeConversation?>

    @Transaction
    @Query("""
    SELECT 
        conversation.*,
        message.id AS message_id,
        message.threadId AS message_threadId,
        message.date AS message_date,
        message.body AS message_body
        -- Add all other MessageEntity fields here with 'message_' prefix
    FROM conversation
    LEFT JOIN message 
        ON message.id = (
            SELECT id FROM message 
            WHERE threadId = conversation.id 
            ORDER BY date DESC 
            LIMIT 1
        )
    """)
    fun getConversations(): Flow<List<CompositeConversation>>

    @Transaction
    @Query("""
        SELECT c.*, m.* FROM conversation c
        LEFT JOIN message m ON c.id = m.threadId
        WHERE members = :members
        ORDER BY m.dateSent DESC
        LIMIT 1
    """)
    fun getCompositeConversationByExactMembers(members: List<String>): Flow<CompositeConversation?>

    @Query("UPDATE conversation SET archived = :archived WHERE id = :id")
    suspend fun updateArchivedStatus(id: String, archived: Boolean)

    @Query("UPDATE conversation SET pinned = :pinned WHERE id = :id")
    suspend fun updatePinnedStatus(id: String, pinned: Boolean)

    @Query("UPDATE conversation SET blocked = :blocked WHERE id = :id")
    suspend fun updateBlockedStatus(id: String, blocked: Boolean)

    @Query("SELECT * FROM conversation WHERE blocked = true")
    fun getBlockedConversations(): Flow<List<ConversationEntity>>

    @Update
    fun updateConversation(conversationEntity: ConversationEntity)

    @Upsert
    fun upsertConversation(conversationEntity: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversationEntity: ConversationEntity)

    @Upsert
    fun upsertConversations(conversationEntities: List<ConversationEntity>)

    @Delete
    fun deleteConversation(conversationEntities: List<ConversationEntity>)

    @Query("DELETE FROM conversation WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationMemberCrossRefs(refs: List<ConversationRecipientCrossRef>)

}