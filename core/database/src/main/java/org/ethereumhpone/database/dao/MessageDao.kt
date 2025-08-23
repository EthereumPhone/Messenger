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
    @Query("SELECT * FROM message WHERE threadId = :threadId ORDER BY date ASC")
    fun getMessages(threadId: String): Flow<List<CompositeMessage>>

    @Transaction
    @Query("SELECT * FROM message where id == :id")
    fun getCompositeMessage(id: String): Flow<CompositeMessage?>

    @Query("SELECT * FROM message where id == :id")
    fun getMessage(id: String): Flow<MessageEntity?>

    @Query("SELECT * FROM message WHERE seen = 0 ORDER BY date")
    suspend fun getUnreadUnseenMessages(): List<MessageEntity>

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

}