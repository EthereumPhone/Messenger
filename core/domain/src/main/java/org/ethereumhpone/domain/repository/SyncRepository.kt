package org.ethereumhpone.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MessageEntity

interface SyncRepository {

    val isSyncing: Flow<Boolean>

    suspend fun syncMessages()
    suspend fun syncMessage(uri: Uri): MessageEntity?
    suspend fun syncContacts()
    suspend fun syncXmtp()
    suspend fun startStream()
    
    /**
     * Performs an immediate sync of all XMTP conversations and messages from the network.
     * This is called by the OS-level XMTPNotificationsService every 5 minutes.
     * 
     * Unlike syncXmtp() which does a full sync, this method is optimized for quick
     * incremental syncs:
     * 1. Syncs all conversations from the network
     * 2. Fetches only new messages since the last sync
     * 3. Stores new messages in the local database
     * 4. Triggers notifications for new unread messages
     * 
     * @return The number of new messages received
     */
    suspend fun syncNow(): Int
}