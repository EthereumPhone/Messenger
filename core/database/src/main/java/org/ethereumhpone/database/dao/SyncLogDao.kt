package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SyncLogDao {

    @Query("SELECT date FROM SyncLog ORDER by date DESC LIMIT 1")
    suspend fun getLastQuery(): Long?
}