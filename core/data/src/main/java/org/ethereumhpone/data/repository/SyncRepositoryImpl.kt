package org.ethereumhpone.data.repository

import android.content.ContentResolver
import android.net.Uri
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository


class SyncRepositoryImpl(
    private val contentResolver: ContentResolver,
    private val conversationRepository: ConversationRepository
): SyncRepository {
    override suspend fun syncMessages() {
        TODO("Not yet implemented")
    }

    override suspend fun syncMessage(uri: Uri) {
        TODO("Not yet implemented")
    }

    override suspend fun syncContacts() {
        TODO("Not yet implemented")
    }

}