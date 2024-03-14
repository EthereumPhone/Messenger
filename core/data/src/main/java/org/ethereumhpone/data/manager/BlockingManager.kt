package org.ethereumhpone.data.manager

import org.ethereumhpone.domain.blocking.BlockingClient
import javax.inject.Inject

/**
 * This manager can be extended to support 3rd party blocking clients
 */
class BlockingManager @Inject constructor(
    private val blockingClient: BlockingClient
): BlockingClient {
    override fun isAvailable(): Boolean = blockingClient.isAvailable()

    override fun getClientCapability(): BlockingClient.Capability =
        blockingClient.getClientCapability()

    override suspend fun shouldBlock(address: String): BlockingClient.Action =
        blockingClient.shouldBlock(address)

    override suspend fun isBlacklisted(address: String): BlockingClient.Action =
        blockingClient.isBlacklisted(address)

    override suspend fun block(addresses: List<String>) = blockingClient.block(addresses)

    override suspend fun unblock(addresses: List<String>) = blockingClient.block(addresses)

    override fun openSettings() = blockingClient.openSettings()

}