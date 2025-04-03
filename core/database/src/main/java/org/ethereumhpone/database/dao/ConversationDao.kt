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
import org.ethereumhpone.database.model.relation.ConversationWithLastMessage

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation WHERE id = :id")
    fun getConversation(id: Long): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversation WHERE (:archived IS NULL or archived = :archived)")
    fun getConversations(archived: Boolean? = null): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversation WHERE id IN (:threadIds) ")
    fun getConversations(threadIds: List<Long>): Flow<List<ConversationEntity>>

    @Query("""
        SELECT * FROM conversation 
        WHERE json_array_length(members) = :memberCount 
        AND members = :membersJson
    """)
    suspend fun getConversationByExactMembers(memberCount: Int, membersJson: String): ConversationEntity?

    @Transaction
    @Query("""
        SELECT c.*, m.* FROM conversation c
        LEFT JOIN message m ON c.id = m.threadId
        WHERE m.dateSent = (
            SELECT MAX(dateSent) 
            FROM message 
            WHERE threadId = c.id
        ) OR m.id IS NULL
    """)
    fun getAllConversationsWithLatestMessage(): Flow<List<ConversationWithLastMessage>>

    @Transaction
    @Query("""
        SELECT c.*, m.* FROM conversation c
        LEFT JOIN message m ON c.id = m.threadId
        WHERE c.id = :id
        ORDER BY m.dateSent DESC
        LIMIT 1
    """)
    fun getConversationWithLastMessage(id: String): ConversationWithLastMessage

    @Query("SELECT * FROM conversation WHERE blocked = true")
    fun getBlockedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversation WHERE " +
        "archived == true OR blocked == true OR pinned == true OR title != ''" +
        "OR blockingClient IS NOT NULL OR blockReason != ''")
    fun getPersistedData(): Flow<List<ConversationEntity>>

    @Update
    fun updateConversation(conversationEntity: ConversationEntity)

    @Upsert
    fun upsertConversation(conversationEntity: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversation(conversationEntity: ConversationEntity)

    @Upsert
    fun upsertConversations(conversationEntities: List<ConversationEntity>)

    @Delete
    fun deleteConversation(conversationEntities: List<ConversationEntity>)

}