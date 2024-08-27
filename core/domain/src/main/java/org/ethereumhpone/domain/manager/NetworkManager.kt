package org.ethereumhpone.domain.manager

import kotlinx.coroutines.flow.Flow

interface NetworkManager {
    val isOnline: Flow<Boolean>
}