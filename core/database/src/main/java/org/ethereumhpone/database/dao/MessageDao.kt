package org.ethereumhpone.database.dao

import android.provider.Telephony
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MessageWithReactions
import org.ethereumhpone.database.model.MmsPart

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE threadId = :threadId ORDER BY date DESC")
    fun getMessages(threadId: Long): Flow<List<Message>>

    @Transaction
    @Query("SELECT * FROM message")
    fun getMessagesWithReactions(): Flow<List<MessageWithReactions>>

    @Query("SELECT * FROM message where id == :id")
    fun getMessage(id: String): Message?

    @Query("SELECT * FROM message where threadId = :threadId ORDER BY date DESC LIMIT 1")
    fun getLastConversationMessage(threadId: Long): Flow<Message?>

    @Query("SELECT * FROM message where threadId = :threadId")
    fun getAllConversationMessages(threadId: Long): Flow<List<Message>>

    @Query("SELECT * FROM message WHERE seen = 0 AND read = 0 ORDER BY date")
    suspend fun getUnreadUnseenMessages(): List<Message>

    @Query("SELECT * FROM message WHERE seen = 0")
    fun getUnseenMessages(): Flow<List<Message>>

    @Insert
    suspend fun insertMessages(message: List<Message>)

    @Update
    suspend fun updateMessages(messages: List<Message>)

    @Upsert
    suspend fun upsertMessage(message: Message)

    @Upsert
    suspend fun upsertMessages(messages: List<Message>)

    @Insert
    suspend fun insertMessage(message: Message): Long

    @Delete
    suspend fun deleteAllMessage(message: List<Message>)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Transaction
    suspend fun deleteAndInsert(oldMessage: Message, newMessage: Message) {
        deleteMessage(oldMessage)
        insertMessages(listOf(newMessage))
    }

}