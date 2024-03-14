package org.ethereumhpone.data.blocking

import kotlinx.coroutines.flow.first
import org.ethereumhpone.domain.blocking.BlockingClient
import org.ethereumhpone.domain.repository.BlockingRepository
import javax.inject.Inject

class MessengerBlockingClient @Inject constructor(
    private val blockingRepository: BlockingRepository
): BlockingClient {
    override fun isAvailable(): Boolean = true

    override fun getClientCapability() = BlockingClient.Capability.BLOCK_WITHOUT_PERMISSION

    override suspend fun shouldBlock(address: String): BlockingClient.Action =
        isBlacklisted(address)

    override suspend fun isBlacklisted(address: String): BlockingClient.Action {
        return when(blockingRepository.isBlocked(address).first()) {
            true -> BlockingClient.Action.Block()
            false -> BlockingClient.Action.Unblock
        }
    }

    override suspend fun block(addresses: List<String>) {
        blockingRepository.blockNumber(*addresses.toTypedArray())
    }

    override suspend fun unblock(addresses: List<String>) {
        blockingRepository.blockNumber(*addresses.toTypedArray())
    }

    override fun openSettings() {
        TODO("Not yet implemented")
    }
}