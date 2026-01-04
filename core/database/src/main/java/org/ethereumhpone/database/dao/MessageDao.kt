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
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.relation.CompositeMessage

@Dao
interface MessageDao {

    @Transaction
    @Query("SELECT * FROM message WHERE threadId = :threadId AND dateSent >= (SELECT hideBefore FROM conversation WHERE id = :threadId) ORDER BY date ASC")
    fun getMessages(threadId: String): Flow<List<CompositeMessage>>

    @Transaction
    @Query("SELECT * FROM message where id == :id")
    fun getCompositeMessage(id: String): Flow<CompositeMessage?>

    @Query("SELECT * FROM message where id == :id")
    fun getMessage(id: String): Flow<MessageEntity?>

    @Query("SELECT * FROM message WHERE seen = 0 ORDER BY date")
    suspend fun getUnreadUnseenMessages(): List<MessageEntity>

    /**
     * Returns unread (unseen) messages for a specific thread.
     * This intentionally does NOT depend on the conversation table so it works
     * even if the conversation entity hasn't been created yet.
     */
    @Query("SELECT * FROM message WHERE threadId = :threadId AND seen = 0 ORDER BY date")
    suspend fun getUnreadUnseenMessagesForThread(threadId: String): List<MessageEntity>

    @Query("SELECT * FROM message WHERE seen = 0")
    fun getUnseenMessages(): Flow<List<MessageEntity>>

    @Query("UPDATE message SET seen = :seen WHERE id = :id")
    fun updateSeenMessage(id: String, seen: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messageEntity: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: MessageEntity)


    @Update
    suspend fun updateMessages(messageEntities: List<MessageEntity>)

    @Query("""
        UPDATE message
        SET seenDate = :seenDate, seen = 1, read = 1
        WHERE seenDate = 0 AND :seenDate >= dateSent
    """
    )
    suspend fun updateMessageSeenDate(seenDate: Long)

    @Query("""
        UPDATE message
        SET seen = 1, read = 1
        WHERE threadId = :threadId
    """)
    suspend fun markAllRead(threadId: String)


    @Upsert
    suspend fun upsertMessages(messageEntities: List<MessageEntity>)

    @Delete
    suspend fun deleteAllMessage(messageEntity: List<MessageEntity>)

    @Delete
    suspend fun deleteMessage(messageEntity: MessageEntity)

    /**
     * Gets the timestamp of the most recent message in the database.
     * Used by syncNow() to determine which messages are new.
     */
    @Query("SELECT MAX(date) FROM message")
    suspend fun getLatestMessageTime(): Long?
    
    /**
     * Updates the transaction status for a message.
     */
    @Query("""
        UPDATE message 
        SET transactionStatus = :status, transactionHash = :txHash
        WHERE id = :messageId
    """)
    suspend fun updateTransactionStatus(messageId: String, status: String, txHash: String?)

}