package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MessageReaction
import org.xmtp.android.library.codecs.Reaction

@Dao
interface ReactionDao {

    @Query("SELECT * FROM reaction WHERE messageId = :messageId")
    fun getReactions(messageId: Long): Flow<List<MessageReaction>>

    @Upsert
    suspend fun upsertReaction(messageReaction: MessageReaction)

    @Delete
    suspend fun deleteReaction(messageReaction: MessageReaction)

    @Query("DELETE FROM reaction WHERE id = :id")
    suspend fun deleteReaction(id: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReaction(reaction: MessageReaction)



}