package org.ethereumhpone.domain.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Message
import org.xmtp.android.library.Client

interface SyncRepository {

    val isSyncing: Flow<Boolean>

    suspend fun syncMessages()
    suspend fun syncMessage(uri: Uri): Message?
    suspend fun syncContacts()
    suspend fun syncXmtp()
    suspend fun startStreamAllMessages()
}