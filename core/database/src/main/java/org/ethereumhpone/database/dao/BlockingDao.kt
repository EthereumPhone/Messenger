package org.ethereumhpone.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.BlockedNumber

interface BlockingDao {

    @Query("SELECT * FROM blocked_number")
    fun getBlockedNumbers(): Flow<List<BlockedNumber>>

    @Query("SELECT * FROM blocked_number WHERE id = :id")
    fun getBlockedNumber(id: Long): Flow<BlockedNumber?>

    @Insert
    fun insertBlockedNumber(blockedNumber: BlockedNumber)

    @Insert
    fun insertBlockedNumbers(blockedNumbers: List<BlockedNumber>)

    @Delete
    fun deleteBlockedNumber(blockedNumber: BlockedNumber)

    @Delete
    fun deleteBlockedNumbers(blockedNumbers: List<BlockedNumber>)

}