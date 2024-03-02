package org.ethereumhpone.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Message

interface SyncRepository {

    val isSyncing: Flow<Boolean>

    suspend fun syncMessages()
    suspend fun syncMessage(uri: Uri): Message?
    suspend fun syncContacts()
}