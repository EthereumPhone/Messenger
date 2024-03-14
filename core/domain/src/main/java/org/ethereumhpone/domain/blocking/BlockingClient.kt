package org.ethereumhpone.domain.blocking

interface BlockingClient {

    enum class Capability {
        BLOCK_WITHOUT_PERMISSION,
        BLOCK_WITH_PERMISSION ,
        CANT_BLOCK
    }

    sealed class Action {
        class Block(val reason: String? = null) : Action()
        object Unblock : Action()

        // We only need these for Should I Answer, because they don't allow us to block numbers in their app directly.
        // This means there's a good chance that if a number is blocked in QK, it won't be blocked there, so we
        // shouldn't unblock the conversation in that case
        object DoNothing : Action()

        override fun toString(): String {
            return when (this) {
                is Block -> "Block"
                is Unblock -> "Unblock"
                is DoNothing -> "DoNothing"
            }
        }
    }

    /**
     * Returns true if the target blocking client is available for use, ie. it is installed
     */
    fun isAvailable(): Boolean

    /**
     * Returns the level of access that the given blocking client provides to QKSMS
     */
    fun getClientCapability(): Capability

    /**
     * Returns the recommendation action to perform given a message from the [address]
     */
    suspend fun shouldBlock(address: String): Action

    /**
     * Returns whether or not the [address] is in the blocking manager's blacklist
     * In most cases this will return the same result as [shouldBlock], but it's possible for an app's blacklist
     * to be temporarily deactivated, in which case the results will differ
     */
    suspend fun isBlacklisted(address: String): Action

    /**
     * Blocks the numbers or opens the manager
     */
    suspend fun block(addresses: List<String>)

    /**
     * Unblocks the numbers or opens the manager
     */
    suspend fun unblock(addresses: List<String>)

    /**
     * Opens the settings page for the blocking manager
     */
    fun openSettings()

}