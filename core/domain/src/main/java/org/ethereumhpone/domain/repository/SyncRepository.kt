package org.ethereumhpone.domain.repository

import android.net.Uri

interface SyncRepository {

    sealed class SyncProgress {
        object Idle: SyncProgress()
        data class Running(val max: Int, val progress: Int, val indeterminate: Boolean): SyncProgress()
    }

    fun syncMessages()
    fun syncMessage(uri: Uri)
    fun syncContacts()
}