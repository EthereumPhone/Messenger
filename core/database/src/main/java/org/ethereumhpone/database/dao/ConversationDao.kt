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
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.relation.ConversationWithLastMessage
import java.util.concurrent.TimeUnit

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation WHERE id = :id")
    fun getConversation(id: Long): Flow<Conversation?>

    @Query("SELECT * FROM conversation WHERE (:archived IS NULL or archived = :archived)")
    fun getConversations(archived: Boolean? = null): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE id IN (:threadIds) ")
    fun getConversations(threadIds: List<Long>): Flow<List<Conversation>>

    @Query("""
        SELECT * FROM conversation 
        WHERE json_array_length(members) = :memberCount 
        AND members = :membersJson
    """)
    suspend fun getConversationByExactMembers(memberCount: Int, membersJson: String): Conversation?

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
    suspend fun getAllConversationsWithLatestMessage(): List<ConversationWithLastMessage>

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
    fun getBlockedConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE " +
        "archived == true OR blocked == true OR pinned == true OR title != ''" +
        "OR blockingClient IS NOT NULL OR blockReason != ''")
    fun getPersistedData(): Flow<List<Conversation>>

    @Update
    fun updateConversation(conversation: Conversation)

    @Upsert
    fun upsertConversation(conversation: Conversation)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversation(conversation: Conversation)

    @Upsert
    fun upsertConversations(conversations: List<Conversation>)

    @Delete
    fun deleteConversation(conversations: List<Conversation>)

}