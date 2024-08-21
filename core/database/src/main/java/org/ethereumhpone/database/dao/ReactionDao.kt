package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MessageReaction

@Dao
interface ReactionDao {

    @Query("SELECT * FROM reaction WHERE messageId = :messageId")
    fun getReactions(messageId: Long): Flow<List<MessageReaction>>

    @Query("SELECT * FROM reaction WHERE " +
            "senderAddress = :senderAddress AND messageId = :messageId AND unicode = :content"
    )
    suspend fun getReactionByContent(
        messageId: String,
        senderAddress: String,
        content: String
    ): MessageReaction?


    @Upsert
    suspend fun upsertReaction(messageReaction: MessageReaction)

    @Delete
    suspend fun deleteReaction(messageReaction: MessageReaction)

    @Query("DELETE FROM reaction WHERE id = :id")
    suspend fun deleteReaction(id: String)



}