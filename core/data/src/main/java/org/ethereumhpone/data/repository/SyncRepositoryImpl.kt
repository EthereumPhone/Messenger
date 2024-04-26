package org.ethereumhpone.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.common.extensions.forEach
import org.ethereumhpone.common.extensions.map
import org.ethereumhpone.common.util.tryOrNull
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.PhoneNumberDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.dao.SyncLogDao
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.SyncLog
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.mapper.ContactCursor
import org.ethereumhpone.domain.mapper.ContactGroupCursor
import org.ethereumhpone.domain.mapper.ContactGroupMemberCursor
import org.ethereumhpone.domain.mapper.ConversationCursor
import org.ethereumhpone.domain.mapper.MessageCursor
import org.ethereumhpone.domain.mapper.PartCursor
import org.ethereumhpone.domain.mapper.RecipientCursor
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject


class SyncRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val conversationRepository: ConversationRepository,
    private val conversationCursor: ConversationCursor,
    private val messageCursor: MessageCursor,
    private val partCursor: PartCursor,
    private val recipientCursor: RecipientCursor,
    private val contactCursor: ContactCursor,
    private val contactGroupCursor: ContactGroupCursor,
    private val contactGroupMemberCursor: ContactGroupMemberCursor,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val messengerPreferences: MessengerPreferences,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val contactDao: ContactDao,
    private val recipientDao: RecipientDao,
    private val phoneNumberDao: PhoneNumberDao,
    private val syncLogDao: SyncLogDao
): SyncRepository {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: Flow<Boolean> = _isSyncing.asStateFlow()


    override suspend fun syncMessages() {

        if(isSyncing.first()) return
        _isSyncing.value = true

        val persistedData = conversationDao.getPersistedData()
            .first()
            .associateBy { conversation -> conversation.id }
            .toMutableMap()


        val partsCursor = partCursor.getPartsCursor()
        val messagesCursor = messageCursor.getMessagesCursor()
        val conversationsCursor = conversationCursor.getConversationsCursor()
        val recipientsCursor = recipientCursor.getRecipientCursor()


        // sync messages parts
        partsCursor?.use { cursor ->
            cursor.forEach {
                val mmsPart = partCursor.map(cursor) // Assuming this method creates a new instance
                CoroutineScope(Dispatchers.IO).launch {
                    messageDao.upsertMessagePart(mmsPart)
                }
            }
        }

        messagesCursor?.use {
            val messageColumns = MessageCursor.MessageColumns(messagesCursor)
            messagesCursor.forEach { cursor ->
                CoroutineScope(Dispatchers.IO).launch {
                    val message = messageCursor.map(Pair(cursor, messageColumns))
                    messageDao.upsertMessage(message.copy(
                        parts = if (message.isSms()) {
                            messageDao.getPartsForConversation(message.contentId).first()
                        } else {
                            message.parts // If not SMS, keep the original parts
                        }
                    ))
                }
            }
        }
        conversationsCursor?.use {
            conversationsCursor.forEach { cursor ->

                val conversation = conversationCursor.map(cursor)

                CoroutineScope(Dispatchers.IO).launch {
                    val updatedConvo = persistedData[conversation.id]?.let {
                        conversation.copy(
                            archived = it.archived,
                            blocked = it.blocked,
                            pinned = it.pinned,
                            title = it.title,
                            blockingClient = it.blockingClient,
                            blockReason = it.blockReason,
                            lastMessage = messageDao.getLastConversationMessage(it.id).first()
                        )
                    }
                    conversationDao.upsertConversation(updatedConvo ?: conversation)
                }
            }
        }


        // sync recipients
        recipientsCursor?.use {
            val contacts = contactDao.getContacts().first()
            recipientsCursor.forEach { cursor ->
                val recipient = recipientCursor.map(cursor)
                val updatedRecipient = recipient.copy(
                    contact = contacts.firstOrNull { contact ->
                        contact.numbers.any { phoneNumberUtils.compare(recipient.address, it.address) }
                    }
                )
                recipientDao.upsertRecipient(updatedRecipient)
            }
        }

        syncLogDao.upsertSyncLog(SyncLog())
        _isSyncing.value = false
    }

    override suspend fun syncMessage(uri: Uri): Message? {
        val type = when {
            uri.toString().contains("mms") -> "mms"
            uri.toString().contains("sms") -> "sms"
            else -> return null
        }

        val id = tryOrNull(false) { ContentUris.parseId(uri) } ?: return null

        val existingId = messageDao.getMessageId(id, type)

        val stableUri = when (type) {
            "mms" -> ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id)
            else -> ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id)
        }

        return contentResolver.query(
            stableUri, null, null, null, null
        )?.use { cursor ->

            if(!cursor.moveToFirst()) return null

            val columnsMap = MessageCursor.MessageColumns(cursor)
            messageCursor.map(Pair(cursor, columnsMap)).apply {

                conversationRepository.getOrCreateConversation(threadId)
                messageDao.upsertMessage(
                    this.copy(
                        id = existingId.first() ?: this.id,
                        parts = if(isSms()) {
                            partCursor.getPartsCursor(contentId)?.map {
                                partCursor.map(it)
                            }.orEmpty()
                        } else this.parts
                    )
                )
            }
        }
    }

    override suspend fun syncContacts() {
        val contacts = getContacts()

        contactDao.deleteAllContacts()
        contactDao.deleteAllContactGroups()
        val recipients = recipientDao.getRecipients()

        recipients.collect { recipientList ->
            contactDao.upsertContactGroup(getContactGroups(contacts))
            contactDao.upsertContact(contacts)

            recipientList.forEach { recipient ->
                recipientDao.upsertRecipient(
                    recipient.copy(
                        contact = contacts.find { contact ->
                            contact.numbers.any { phoneNumberUtils.compare(recipient.address, it.address) }
                        }
                    )
                )
            }
        }
    }

    private suspend fun getContacts(): List<Contact> {
        val defaultNumberIds = phoneNumberDao.getDefaultNunmberIds().map { numbers ->
            numbers.map { it.id }
        }.first()

        return contactCursor.getContactsCursor()
            ?.map { cursor -> contactCursor.map(cursor) }
            ?.groupBy { contact -> contact.lookupKey }
            ?.map { contacts ->
                // Sometimes, contacts providers on the phone will create duplicate phone number entries. This
                // commonly happens with Whatsapp. Let's try to detect these duplicate entries and filter them out
                val uniqueNumbers = mutableListOf<PhoneNumber>()
                contacts.value
                    .flatMap { it.numbers }
                    .forEach { number ->
                        val isDefault = defaultNumberIds.any { id -> id == number.id }
                        val updatedNumber = number.copy(isDefault = isDefault)
                        var duplicate = uniqueNumbers.find { other ->
                            phoneNumberUtils.compare(number.address, other.address)
                        }

                        if (duplicate == null) {
                            uniqueNumbers += number
                        } else if (!duplicate.isDefault && updatedNumber.isDefault) {
                            duplicate = duplicate.copy(isDefault = true)
                        }
                    }
                contacts.value.first().copy(
                    numbers = uniqueNumbers
                )
            } ?: listOf()
    }

    private fun getContactGroups(contacts: List<Contact>): List<ContactGroup> {
        val groupMembers = contactGroupMemberCursor.getGroupMembersCursor()
            ?.map(contactGroupMemberCursor::map)
            .orEmpty()

        val groups = contactGroupCursor.getContactGroupsCursor()
            ?.map(contactGroupCursor::map)
            .orEmpty()

        return groups.map { group ->
            group.copy(
                contacts = groupMembers
                    .filter { member -> member.groupId == group.id }
                    .mapNotNull { member -> contacts.find { contact -> contact.lookupKey == member.lookupKey  } }
            )
        }
    }
}