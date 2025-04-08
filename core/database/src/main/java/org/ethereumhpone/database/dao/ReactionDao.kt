package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {

    @Query("SELECT * FROM reaction WHERE messageId = :messageId")
    fun getReactions(messageId: Long): Flow<List<org.ethereumhpone.database.model.ReactionEntity>>

    @Upsert
    suspend fun upsertReaction(reactionEntity: org.ethereumhpone.database.model.ReactionEntity)

    @Delete
    suspend fun deleteReaction(reactionEntity: org.ethereumhpone.database.model.ReactionEntity)

    @Query("DELETE FROM reaction WHERE id = :id")
    suspend fun deleteReaction(id: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReaction(reactionEntity: org.ethereumhpone.database.model.ReactionEntity)



}