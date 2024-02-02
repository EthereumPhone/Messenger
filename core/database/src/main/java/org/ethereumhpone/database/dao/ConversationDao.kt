package org.ethereumhpone.database.dao

import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Conversation
import java.util.concurrent.TimeUnit

interface ConversationDao {

    @Query("SELECT * FROM conversation WHERE (:archived IS NULL or archived = :archived)")
    fun getConversations(archived: Boolean): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation " +
        "WHERE id != 0 AND archived = false AND blocked = false AND recipients IS NOT NULL AND " +
        "(lastMessage IS NOT NULL OR LENGTH(draft) > 0) " +
        "ORDER BY pinned DESC, draft DESC, (SELECT MAX(date) FROM message WHERE message.threadId = id) DESC"
    )
    fun getConversationsSnapshot(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE id != 0 AND lastMessage IS NOT NULL AND " +
        "(pinned = true OR (SELECT MAX(date) FROM message WHERE message.id = id) > :timeFrame) " +
        "AND archived = false AND blocked = false AND recipients IS NOT NULL " +
        "ORDER BY pinned DESC, (SELECT MAX(date) FROM message WHERE message.id = id) DESC"
    )
    fun getTopConversations(
        timeFrame: Long =  System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
    ): Flow<List<Conversation>>

     @Query("SELECT * FROM conversation WHERE id != 0 AND lastMessage IS NOT NULL AND " +
        "lastMessage IS NOT NULL AND blocked = false AND recipients IS NOT NULL " +
         "ORDER BY pinned DESC, (SELECT MAX(date) FROM message WHERE message.id = id) DESC"
     )
    fun getActiveConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE blocked = true")
    fun getBlockedConversations(): Flow<List<Conversation>>
}