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
        COALESCE(latest_msg.id, '') AS message_id,
        COALESCE(latest_msg.threadId, '') AS message_threadId,
        COALESCE(latest_msg.date, 0) AS message_date,
        COALESCE(latest_msg.body, '') AS message_body,
        COALESCE(latest_msg.senderInboxId, '') AS message_senderInboxId,
        COALESCE(latest_msg.read, 0) AS message_read,
        COALESCE(latest_msg.dateSent, 0) AS message_dateSent,
        COALESCE(latest_msg.seen, 0) AS message_seen,
        COALESCE(latest_msg.locked, 0) AS message_locked,
        COALESCE(latest_msg.replyReference, '') AS message_replyReference,
        COALESCE(latest_msg.seenDate, 0) AS message_seenDate,
        COALESCE(latest_msg.deliveryStatus, 0) AS message_deliveryStatus,
        COALESCE(latest_msg.isMe, 0) AS message_isMe
    FROM conversation
    LEFT JOIN message AS latest_msg
      ON latest_msg.threadId = conversation.id
     AND latest_msg.dateSent = (
          SELECT MAX(m2.dateSent) FROM message m2
           WHERE m2.threadId = conversation.id
             AND m2.dateSent >= conversation.hideBefore
        )
    WHERE conversation.id = :id AND conversation.deleted = 0
    """)
    fun getConversation(id: String): Flow<CompositeConversation?>

    @Transaction
    @Query("""
    SELECT 
        conversation.*,
        COALESCE(latest_msg.id, '') AS message_id,
        COALESCE(latest_msg.threadId, '') AS message_threadId,
        COALESCE(latest_msg.date, 0) AS message_date,
        COALESCE(latest_msg.body, '') AS message_body,
        COALESCE(latest_msg.senderInboxId, '') AS message_senderInboxId,
        COALESCE(latest_msg.read, 0) AS message_read,
        COALESCE(latest_msg.dateSent, 0) AS message_dateSent,
        COALESCE(latest_msg.seen, 0) AS message_seen,
        COALESCE(latest_msg.locked, 0) AS message_locked,
        COALESCE(latest_msg.replyReference, '') AS message_replyReference,
        COALESCE(latest_msg.seenDate, 0) AS message_seenDate,
        COALESCE(latest_msg.deliveryStatus, 0) AS message_deliveryStatus,
        COALESCE(latest_msg.isMe, 0) AS message_isMe
    FROM conversation
    LEFT JOIN message AS latest_msg
      ON latest_msg.threadId = conversation.id
     AND latest_msg.dateSent = (
          SELECT MAX(m2.dateSent) FROM message m2
           WHERE m2.threadId = conversation.id
             AND m2.dateSent >= conversation.hideBefore
        )
    WHERE conversation.deleted = 0
    ORDER BY CASE WHEN latest_msg.dateSent IS NULL THEN 0 ELSE 1 END DESC, latest_msg.dateSent DESC
    """)
    fun getConversations(): Flow<List<CompositeConversation>>

    @Transaction
    @Query("""
    SELECT 
        conversation.*,
        COALESCE(latest_msg.id, '') AS message_id,
        COALESCE(latest_msg.threadId, '') AS message_threadId,
        COALESCE(latest_msg.date, 0) AS message_date,
        COALESCE(latest_msg.body, '') AS message_body,
        COALESCE(latest_msg.senderInboxId, '') AS message_senderInboxId,
        COALESCE(latest_msg.read, 0) AS message_read,
        COALESCE(latest_msg.dateSent, 0) AS message_dateSent,
        COALESCE(latest_msg.seen, 0) AS message_seen,
        COALESCE(latest_msg.locked, 0) AS message_locked,
        COALESCE(latest_msg.replyReference, '') AS message_replyReference,
        COALESCE(latest_msg.seenDate, 0) AS message_seenDate,
        COALESCE(latest_msg.deliveryStatus, 0) AS message_deliveryStatus,
        COALESCE(latest_msg.isMe, 0) AS message_isMe
    FROM conversation
    INNER JOIN message ON conversation.id = message.threadId AND message.seen = 0 AND message.dateSent >= conversation.hideBefore
    LEFT JOIN message AS latest_msg
      ON latest_msg.threadId = conversation.id
     AND latest_msg.dateSent = (
          SELECT MAX(m2.dateSent) FROM message m2
           WHERE m2.threadId = conversation.id
             AND m2.dateSent >= conversation.hideBefore
        )
    WHERE conversation.deleted = 0
    GROUP BY conversation.id
    ORDER BY CASE WHEN latest_msg.dateSent IS NULL THEN 0 ELSE 1 END DESC, latest_msg.dateSent DESC
    """)
    fun getConversationsWithUnseenMessages(): Flow<List<CompositeConversation>>

    @Transaction
    @Query("""
        SELECT 
            c.*,
            COALESCE(m.id, '') AS message_id,
            COALESCE(m.threadId, '') AS message_threadId,
            COALESCE(m.date, 0) AS message_date,
            COALESCE(m.body, '') AS message_body,
            COALESCE(m.senderInboxId, '') AS message_senderInboxId,
            COALESCE(m.read, 0) AS message_read,
            COALESCE(m.dateSent, 0) AS message_dateSent,
            COALESCE(m.seen, 0) AS message_seen,
            COALESCE(m.locked, 0) AS message_locked,
            COALESCE(m.replyReference, '') AS message_replyReference,
            COALESCE(m.seenDate, 0) AS message_seenDate,
            COALESCE(m.deliveryStatus, 0) AS message_deliveryStatus,
            COALESCE(m.isMe, 0) AS message_isMe
        FROM conversation c
        LEFT JOIN message m ON c.id = m.threadId AND m.dateSent >= c.hideBefore
        WHERE members = :members AND c.deleted = 0
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

    @Query("UPDATE conversation SET deleted = 1, hideBefore = :cutoff WHERE id = :id")
    suspend fun softDeleteConversation(id: String, cutoff: Long)

    @Query("SELECT * FROM conversation WHERE id = :id LIMIT 1")
    suspend fun getConversationEntityById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationMemberCrossRefs(refs: List<ConversationRecipientCrossRef>)

    @Query("DELETE FROM ConversationRecipientCrossRef WHERE conversationId = :conversationId AND inboxId NOT IN (:keepInboxIds)")
    suspend fun deleteRemovedMemberCrossRefs(conversationId: String, keepInboxIds: List<String>)

    @Query("DELETE FROM ConversationRecipientCrossRef WHERE conversationId = :conversationId AND inboxId IN (:inboxIds)")
    suspend fun deleteMemberCrossRefs(conversationId: String, inboxIds: List<String>)

    @Query("DELETE FROM ConversationRecipientCrossRef WHERE conversationId = :conversationId")
    suspend fun deleteAllMemberCrossRefs(conversationId: String)

    /**
     * Gets all conversations with unseen messages for notification purposes.
     * Includes both ALLOWED and UNKNOWN consent state conversations.
     * Excludes only BLOCKED conversations.
     */
    @Transaction
    @Query("""
    SELECT 
        conversation.*,
        COALESCE(latest_msg.id, '') AS message_id,
        COALESCE(latest_msg.threadId, '') AS message_threadId,
        COALESCE(latest_msg.date, 0) AS message_date,
        COALESCE(latest_msg.body, '') AS message_body,
        COALESCE(latest_msg.senderInboxId, '') AS message_senderInboxId,
        COALESCE(latest_msg.read, 0) AS message_read,
        COALESCE(latest_msg.dateSent, 0) AS message_dateSent,
        COALESCE(latest_msg.seen, 0) AS message_seen,
        COALESCE(latest_msg.locked, 0) AS message_locked,
        COALESCE(latest_msg.replyReference, '') AS message_replyReference,
        COALESCE(latest_msg.seenDate, 0) AS message_seenDate,
        COALESCE(latest_msg.deliveryStatus, 0) AS message_deliveryStatus,
        COALESCE(latest_msg.isMe, 0) AS message_isMe
    FROM conversation
    INNER JOIN message ON conversation.id = message.threadId 
        AND message.seen = 0 
        AND message.isMe = 0
        AND message.dateSent >= conversation.hideBefore
    LEFT JOIN message AS latest_msg
      ON latest_msg.threadId = conversation.id
     AND latest_msg.dateSent = (
          SELECT MAX(m2.dateSent) FROM message m2
           WHERE m2.threadId = conversation.id
             AND m2.dateSent >= conversation.hideBefore
        )
    WHERE conversation.deleted = 0 
      AND conversation.blocked = 0
    GROUP BY conversation.id
    ORDER BY CASE WHEN latest_msg.dateSent IS NULL THEN 0 ELSE 1 END DESC, latest_msg.dateSent DESC
    """)
    fun getConversationsWithUnseenMessagesForNotifications(): Flow<List<CompositeConversation>>

}