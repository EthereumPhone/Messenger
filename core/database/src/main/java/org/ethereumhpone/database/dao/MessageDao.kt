package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.MessageWithReactions

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE threadId = :threadId ORDER BY date DESC")
    fun getMessages(threadId: Long): Flow<List<MessageEntity>>

    @Transaction
    @Query("SELECT * FROM message")
    fun getMessagesWithReactions(): Flow<List<MessageWithReactions>>

    @Query("SELECT * FROM message where id == :id")
    fun getMessage(id: String): MessageEntity?

    @Query("SELECT * FROM message where threadId = :threadId ORDER BY date DESC LIMIT 1")
    fun getLastConversationMessage(threadId: Long): Flow<MessageEntity?>

    @Query("SELECT * FROM message where threadId = :threadId")
    fun getAllConversationMessages(threadId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM message WHERE seen = 0 AND read = 0 ORDER BY date")
    suspend fun getUnreadUnseenMessages(): List<MessageEntity>

    @Query("SELECT * FROM message WHERE seen = 0")
    fun getUnseenMessages(): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessages(messageEntity: List<MessageEntity>)

    @Update
    suspend fun updateMessages(messageEntities: List<MessageEntity>)

    @Upsert
    suspend fun upsertMessage(messageEntity: MessageEntity)

    @Upsert
    suspend fun upsertMessages(messageEntities: List<MessageEntity>)

    @Insert
    suspend fun insertMessage(messageEntity: MessageEntity): Long

    @Delete
    suspend fun deleteAllMessage(messageEntity: List<MessageEntity>)

    @Delete
    suspend fun deleteMessage(messageEntity: MessageEntity)

    @Transaction
    suspend fun deleteAndInsert(oldMessageEntity: MessageEntity, newMessageEntity: MessageEntity) {
        deleteMessage(oldMessageEntity)
        insertMessages(listOf(newMessageEntity))
    }

}