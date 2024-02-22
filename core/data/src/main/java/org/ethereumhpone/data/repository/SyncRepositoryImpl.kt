package org.ethereumhpone.data.repository

import android.content.ContentResolver
import android.net.Uri
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.KeyManager
import org.ethereumhpone.domain.mapper.ContactCursor
import org.ethereumhpone.domain.mapper.ContactGroupCursor
import org.ethereumhpone.domain.mapper.ContactGroupMemberCursor
import org.ethereumhpone.domain.mapper.ConversationCursor
import org.ethereumhpone.domain.mapper.MessageCursor
import org.ethereumhpone.domain.mapper.PartCursor
import org.ethereumhpone.domain.mapper.RecipientCursor
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository


class SyncRepositoryImpl(
    private val contentResolver: ContentResolver,
    private val conversationRepository: ConversationRepository,
    private val conversationCursor: ConversationCursor,
    private val messageCursor: MessageCursor,
    private val partCursor: PartCursor,
    private val recipientCursor: RecipientCursor,
    private val contactCursor: ContactCursor,
    private val contactGroupCursor: ContactGroupCursor,
    private val contactGroupMemberCursor: ContactGroupMemberCursor,
    private val keys: KeyManager,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val messengerPreferences: MessengerPreferences
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