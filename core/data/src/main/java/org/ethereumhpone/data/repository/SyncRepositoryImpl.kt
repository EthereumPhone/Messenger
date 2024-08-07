package org.ethereumhpone.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
import org.ethereumhpone.domain.model.LogTimeHandler
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
    private val syncLogDao: SyncLogDao,
    private val logTimeHandler: LogTimeHandler
): SyncRepository {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: Flow<Boolean> = _isSyncing.asStateFlow()


    override suspend fun syncMessages() {

        if(_isSyncing.value) return
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


        // sync recipients
        recipientsCursor?.use {
            val contacts = getContacts()
            contactDao.upsertContact(contacts)
            recipientsCursor.forEach { cursor ->
                val recipient = recipientCursor.map(cursor)
                val updatedRecipient = recipient.copy(
                    contact = contacts.firstOrNull { contact ->
                        contact.numbers.any { phoneNumberUtils.compare(recipient.address, it.address) }
                    }
                )
                CoroutineScope(Dispatchers.IO).launch {
                    recipientDao.upsertRecipient(updatedRecipient)
                }
            }
        }

        // sync messages
        messagesCursor?.use {
            val messageColumns = MessageCursor.MessageColumns(messagesCursor)
            messagesCursor.forEach { cursor ->
                val message = messageCursor.map(Pair(cursor, messageColumns))
                CoroutineScope(Dispatchers.IO).launch {
                    if (message.isMms()) {
                        messageDao.getPartsForConversation(message.contentId).collectLatest { mmsParts ->
                            val updatedMessage = message.copy(parts = mmsParts) // copy needs to be done here or it will not work correctly
                            messageDao.upsertMessage(updatedMessage)
                        }
                    } else {
                        messageDao.upsertMessage(message)
                    }
                }
            }
        }



        // sync conversations
        conversationsCursor?.use {
            conversationsCursor.forEach { cursor ->
                val conversation = conversationCursor.map(cursor)

                CoroutineScope(Dispatchers.IO).launch {
                    combine(
                        messageDao.getLastConversationMessage(conversation.id),
                        recipientDao.getRecipientsByIds(conversation.recipients.map { it.id })
                    ) { lastMessage, recipients ->
                        conversation.copy(lastMessage = lastMessage, recipients = recipients)
                    }.collectLatest {
                        conversationDao.upsertConversation(it)
                    }
                }
            }
        }
        logTimeHandler.setLastLog(SyncLog().date)
        _isSyncing.value = false
    }

    override suspend fun syncMessage(uri: Uri): Message? {

        val type = when {
            uri.toString().contains("mms") -> "mms"
            uri.toString().contains("sms") -> "sms"
            else -> return null
        }

        val id = tryOrNull { ContentUris.parseId(uri) } ?: return null

        val existingId = messageDao.getMessageId(id, type).first()

        val stableUri = when (type) {
            "mms" -> ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id)
            else -> ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id)
        }


        return contentResolver.query(stableUri, null, null, null, null)?.use { cursor ->

            // If there are no rows, return null. Otherwise, we've moved to the first row
            if (!cursor.moveToFirst()) return null

            val columnsMap = MessageCursor.MessageColumns(cursor)

            messageCursor.map(Pair(cursor, columnsMap)).apply {
                val message = this.copy(
                    id = existingId ?: this.id,
                    parts = if (isMms()) {
                        partCursor.getPartsCursor(contentId)?.map { partCursor.map(it) }.orEmpty()
                    } else { this.parts }
                )

                messageDao.upsertMessage(message)
                conversationRepository.getOrCreateConversation(threadId)
            }
        }
    }

    override suspend fun syncContacts() {
        val contacts = getContacts()

        contactDao.deleteAllContacts()
        contactDao.deleteAllContactGroups()


        recipientDao.getRecipients().collect { recipientList ->
            contactDao.upsertContactGroup(getContactGroups(contacts))
            contactDao.upsertContact(contacts)

            val updatedRecipients = recipientList.map { recipient ->
                recipient.copy(
                    contact = contacts.find { contact ->
                        contact.numbers.any {
                            phoneNumberUtils.compare(
                                recipient.address,
                                it.address
                            )
                        }
                    }
                )
            }
            updatedRecipients.forEach { recipient ->
                recipientDao.upsertRecipient(recipient)
            }
        }


        conversationDao.getConversations().collect { conversations ->
            conversations.forEach { conversation ->
                recipientDao.getRecipientsByIds(conversation.recipients.map { it.id })
                    .collect {
                        conversationDao.updateConversation(
                            conversation.copy(recipients = it)
                        )
                }
            }
        }
    }

    private suspend fun getContacts(): List<Contact> {
        val defaultNumberIds = phoneNumberDao.getDefaultNunmberIds().map { numbers ->
            numbers.map { it.id }
        }.first()

        return contactCursor.getContactsCursor()?.use { cursor ->
            cursor.map { contactCursor.map(it) }
                .groupBy { contact -> contact.lookupKey }
                .map { (_, contacts) ->
                    // Sometimes, contacts providers on the phone will create duplicate phone number entries. This
                    // commonly happens with Whatsapp. Let's try to detect these duplicate entries and filter them out
                    val uniqueNumbers = mutableListOf<PhoneNumber>()
                    contacts.flatMap { it.numbers }
                        .forEach { number ->
                            val isDefault = defaultNumberIds.any { id -> id == number.id }
                            val updatedNumber = number.copy(isDefault = isDefault)
                            var duplicate = uniqueNumbers.find { other ->
                                phoneNumberUtils.compare(number.address, other.address)
                            }

                            if (duplicate == null) {
                                uniqueNumbers += updatedNumber
                            } else if (!duplicate.isDefault && updatedNumber.isDefault) {
                                uniqueNumbers[uniqueNumbers.indexOf(duplicate)] = duplicate.copy(isDefault = true)
                            }
                        }
                    contacts.first().copy(
                        numbers = uniqueNumbers
                    )
                }
        } ?: listOf()
    }


    private fun getContactGroups(contacts: List<Contact>): List<ContactGroup> {
        val groupMembers = contactGroupMemberCursor.getGroupMembersCursor()?.use { cursor ->
            cursor.map(contactGroupMemberCursor::map).toList()
        }.orEmpty()

        val groups = contactGroupCursor.getContactGroupsCursor()?.use { cursor ->
            cursor.map(contactGroupCursor::map).toList()
        }.orEmpty()

        return groups.map { group ->
            group.copy(
                contacts = groupMembers
                    .filter { member -> member.groupId == group.id }
                    .mapNotNull { member -> contacts.find { contact -> contact.lookupKey == member.lookupKey } }
            )
        }
    }

}