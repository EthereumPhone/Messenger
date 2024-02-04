package org.ethereumhpone.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.BlockingDao
import org.ethereumhpone.database.model.BlockedNumber
import org.ethereumhpone.domain.repository.BlockingRepository

class BlockingRepositoryImpl(
    private val phoneNumberUtils: PhoneNumberUtils,
    private val blockingDao: BlockingDao
): BlockingRepository {
    override fun blockNumber(vararg addresses: String) {
        blockingDao.getBlockedNumbers().map { blockedNumbers ->
            val newAddresses = addresses.filter { address ->
                blockedNumbers.none { number -> phoneNumberUtils.compare(number.address, address) }
            }

            blockingDao.insertBlockedNumbers(
                newAddresses.map {
                    BlockedNumber(
                        address = it
                    )
                }
            )
        }
    }

    override fun getBlockedNumbers(): Flow<List<BlockedNumber>> = blockingDao.getBlockedNumbers()

    override fun getBlockedNumber(id: Long): Flow<BlockedNumber?> = blockingDao.getBlockedNumber(id)

    override fun isBlocked(address: String): Flow<Boolean> =
        blockingDao.getBlockedNumbers().map { blockedNumbers ->
            blockedNumbers.any { blockedNumber ->
                phoneNumberUtils.compare(blockedNumber.address, address)
            }
        }


    override suspend fun unblockNumber(id: Long) {
        blockingDao.getBlockedNumber(id).map { blockedNumber ->
            blockedNumber?.let {
                blockingDao.deleteBlockedNumber(blockedNumber)
            }
        }
    }

    override suspend fun unblockNumbers(vararg addresses: String) {
        blockingDao.getBlockedNumbers().map { blockedNumbers ->
            val toRemove = blockedNumbers.filter { blockedNumber ->
                addresses.any { number -> phoneNumberUtils.compare(blockedNumber.address, number) }
            }

            blockingDao.deleteBlockedNumbers(toRemove)
        }
    }
}