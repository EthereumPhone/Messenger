package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.BlockedNumber

interface BlockingRepository {

    fun blockNumber(vararg addresses: String)
    fun getBlockedNumbers(): Flow<List<BlockedNumber>>
    fun getBlockedNumber(id: Long): BlockedNumber?
    fun isBlocked(address: String): Boolean
    fun unblockNumber(id: Long)
    fun unblockNumbers(vararg addresses: String)


}