package org.ethereumhpone.domain.repository

import android.net.Uri

interface SyncRepository {

    fun syncMessages()
    fun syncMessage(uri: Uri)
    fun syncContacts()
}