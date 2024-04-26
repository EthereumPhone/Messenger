package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.ethereumhpone.database.model.SyncLog

@Dao
interface SyncLogDao {

    @Query("SELECT date FROM SyncLog ORDER by date DESC LIMIT 1")
    suspend fun getLastLogDate(): Long?

    @Upsert
    suspend fun upsertSyncLog(syncLog: SyncLog)
}