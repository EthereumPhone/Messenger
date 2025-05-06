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
        m.id AS message_id,
        m.threadId AS message_threadId,
        m.date AS message_date,
        m.body AS message_body,
        m.senderInboxId AS message_senderInboxId,
        COALESCE(m.read, 0) AS message_read,
        m.dateSent AS message_dateSent,
        COALESCE(m.seen, 0) AS message_seen,
        COALESCE(m.locked, 0) AS message_locked,
        m.replyReference AS message_replyReference,
        m.seenDate AS message_seenDate,
        m.deliveryStatus AS message_deliveryStatus,
        COALESCE(m.isMe, 0) AS message_isMe
    FROM conversation
    LEFT JOIN (
        SELECT message.* 
        FROM message
        WHERE (message.threadId, message.dateSent) IN (
            SELECT m.threadId, MAX(m.dateSent)
            FROM message m
            GROUP BY m.threadId
        )
    ) AS m ON conversation.id = m.threadId
    WHERE conversation.id = :id
    """)
    fun getConversation(id: String): Flow<CompositeConversation?>

    @Transaction
    @Query("""
    SELECT 
        conversation.*,
        m.id AS message_id,
        m.threadId AS message_threadId,
        m.date AS message_date,
        m.body AS message_body,
        m.senderInboxId AS message_senderInboxId,
        COALESCE(m.read, 0) AS message_read,
        m.dateSent AS message_dateSent,
        COALESCE(m.seen, 0) AS message_seen,
        COALESCE(m.locked, 0) AS message_locked,
        m.replyReference AS message_replyReference,
        m.seenDate AS message_seenDate,
        m.deliveryStatus AS message_deliveryStatus,
        COALESCE(m.isMe, 0) AS message_isMe
    FROM conversation
    LEFT JOIN (
        SELECT message.* 
        FROM message
        WHERE (message.threadId, message.dateSent) IN (
            SELECT m.threadId, MAX(m.dateSent)
            FROM message m
            GROUP BY m.threadId
        )
    ) AS m ON conversation.id = m.threadId
    ORDER BY CASE WHEN m.dateSent IS NULL THEN 0 ELSE 1 END DESC, COALESCE(m.dateSent, 0) DESC
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


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversationEntity: ConversationEntity)

    @Delete
    fun deleteConversation(conversationEntities: List<ConversationEntity>)

    @Query("DELETE FROM conversation WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationMemberCrossRefs(refs: List<ConversationRecipientCrossRef>)

}