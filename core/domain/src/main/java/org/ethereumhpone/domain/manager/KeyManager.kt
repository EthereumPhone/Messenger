package org.ethereumhpone.domain.manager

interface KeyManager {

    fun reset()

    fun newId(): Long
}