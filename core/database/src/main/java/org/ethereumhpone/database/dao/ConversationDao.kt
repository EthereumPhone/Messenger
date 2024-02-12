package org.ethereumhpone.database.dao

import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Conversation
import java.util.concurrent.TimeUnit

interface ConversationDao {

    @Query("SELECT * FROM conversation WHERE id = :id")
    fun getConversation(id: Long): Flow<Conversation?>

    @Query("SELECT * FROM conversation WHERE (:archived IS NULL or archived = :archived)")
    fun getConversations(archived: Boolean? = null): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE id IN (:threadIds) ")
    fun getConversations(threadIds: List<Long>): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation " +
        "WHERE id != 0 AND archived = false AND blocked = false AND recipients IS NOT NULL AND " +
        "(lastMessage IS NOT NULL OR LENGTH(draft) > 0) " +
        "ORDER BY pinned DESC, draft DESC, (SELECT MAX(date) FROM message WHERE message.threadId = id) DESC"
    )
    fun getConversationsSnapshot(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE id != 0 AND lastMessage IS NOT NULL AND " +
        "(pinned = true OR (SELECT MAX(date) FROM message WHERE message.id = id) > :timeFrame) " +
        "AND archived = false AND blocked = false AND recipients IS NOT NULL " +
        "ORDER BY pinned DESC, (SELECT MAX(date) FROM message WHERE message.id = threadId) DESC"
    )
    fun getTopConversations(
        timeFrame: Long =  System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
    ): Flow<List<Conversation>>

     @Query("SELECT * FROM conversation WHERE  " +
             "id != 0 AND lastMessage IS NOT NULL AND blocked = false AND recipients IS NOT NULL " +
             "ORDER BY pinned DESC, (SELECT MAX(date) FROM message WHERE message.id = id) DESC"
     )
    fun getActiveConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE " +
            "id != 0 AND lastMessage IS NOT NULL AND archived = 0 AND blocked = 0 AND recipients IS NOT NULL " +
            "ORDER BY (SELECT MAX(date) FROM message WHERE threadId = message.id) DESC LIMIT 5")
    fun getUnmanagedConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE blocked = true")
    fun getBlockedConversations(): Flow<List<Conversation>>

    @Update
    fun updateConversation(conversation: Conversation)

    @Upsert
    fun upsertConversation(conversation: Conversation)

    @Delete
    fun deleteConversation(conversations: List<Conversation>)
}