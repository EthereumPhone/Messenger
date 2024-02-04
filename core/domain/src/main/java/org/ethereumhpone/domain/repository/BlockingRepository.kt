package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.BlockedNumber

interface BlockingRepository {

    fun blockNumber(vararg addresses: String)
    fun getBlockedNumbers(): Flow<List<BlockedNumber>>
    fun getBlockedNumber(id: Long): Flow<BlockedNumber?>
    fun isBlocked(address: String): Flow<Boolean>
    suspend fun unblockNumber(id: Long)
    suspend fun unblockNumbers(vararg addresses: String)


}