package org.ethereumhpone.data.manager

import org.ethereumhpone.domain.manager.KeyManager


class KeyManagerImpl(): KeyManager {

    private var initialized = false
    private var maxValue: Long = 0

    override fun reset() {
        initialized = true
        maxValue = 0L
    }

    // Creates a valid ID to store messages in the Database

    override fun newId(): Long {
        if(!initialized) {

        }

        maxValue++
        return maxValue
    }
}