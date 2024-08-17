package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import org.ethereumhpone.database.model.Reaction

@Dao
interface ReactionDao {


    @Upsert
    suspend fun upsertReaction(reaction: Reaction)

    @Delete
    suspend fun deleteReaction(reaction: Reaction)



}