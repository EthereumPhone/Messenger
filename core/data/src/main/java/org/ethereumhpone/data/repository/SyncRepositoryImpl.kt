package org.ethereumhpone.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.google.android.mms.ContentType
import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.common.extensions.forEach
import org.ethereumhpone.common.extensions.map
import org.ethereumhpone.common.util.tryOrNull
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.PhoneNumberDao
import org.ethereumhpone.database.dao.ReactionDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.dao.SyncLogDao
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.MessageReaction
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.Recipient
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
import org.xmtp.android.library.Client
import org.xmtp.android.library.ConsentState
import org.xmtp.android.library.DecodedMessage
import org.xmtp.android.library.XMTPException
import org.xmtp.android.library.codecs.Attachment
import org.xmtp.android.library.codecs.ContentCodec
import org.xmtp.android.library.codecs.ContentTypeAttachment
import org.xmtp.android.library.codecs.ContentTypeReaction
import org.xmtp.android.library.codecs.ContentTypeReadReceipt
import org.xmtp.android.library.codecs.ContentTypeRemoteAttachment
import org.xmtp.android.library.codecs.ContentTypeReply
import org.xmtp.android.library.codecs.ContentTypeText
import org.xmtp.android.library.codecs.EncodedContent
import org.xmtp.android.library.codecs.Reaction
import org.xmtp.android.library.codecs.ReactionAction
import org.xmtp.android.library.codecs.ReactionSchema
import org.xmtp.android.library.codecs.RemoteAttachment
import org.xmtp.android.library.codecs.Reply
import org.xmtp.proto.message.contents.Content
import javax.inject.Inject


class SyncRepositoryImpl @Inject constructor(
    private val context: Context,
    private val xmtpClientManager: XmtpClientManager,
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
    private val reactionDao: ReactionDao,
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
        // once sync at the time
        if(_isSyncing.value) return
        _isSyncing.value = true




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
                        messageDao.getPartsForConversation(message.contentId.toString()).collectLatest { mmsParts ->
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
                        val result = xmtpClientManager.clientState.first {
                            it == XmtpClientManager.ClientState.Ready
                        }
                        val isUnknown = if (result == XmtpClientManager.ClientState.Ready) {
                             try {
                                 xmtpClientManager.client.contacts.refreshConsentList()
                                 xmtpClientManager.client.contacts.consentList.state(recipients.map { it.address }.firstOrNull() ?: "") == ConsentState.UNKNOWN
                            } catch (e: Exception) {
                                false
                            }
                        } else {
                            false
                        }
                        conversation.copy(lastMessage = lastMessage, recipients = recipients, isUnknown = isUnknown)
                    }.collectLatest {
                        conversationDao.upsertConversation(it)
                    }
                }
            }
        }


        // syncXMTP

        xmtpClientManager.clientState.collectLatest {
            if (it == XmtpClientManager.ClientState.Ready) {
                syncXmtp(context = context, xmtpClientManager.client)
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

        val existingId = messageDao.getMessageId(id, type)

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

    override suspend fun syncXmtp(context: Context, client: Client) = coroutineScope {

        client.conversations.list().forEach { convo ->

            launch {
                // handle messages
                val threadId = TelephonyCompat.getOrCreateThreadId(context, convo.peerAddresses)
                convo.messages().forEach { message ->
                    manageXmtpMessage(
                        threadId = threadId,
                        msg = message,
                        client = client,
                        context = context
                    )
                }

                // update recipients
                val contacts = getContacts()
                val recipients = convo.peerAddresses.map { address ->
                    Recipient(
                        address = address,
                        contact = contacts.firstOrNull { it.ethAddress?.lowercase() == address.lowercase() },
                        inboxId = client.inboxIdFromAddress(address) ?: "0" // assume xmtp V3
                    )
                }.also { recipientDao.upsertRecipients(it) }

                // update convo
                Conversation(
                    id = threadId,
                    recipients = recipients,
                    lastMessage = messageDao.getLastConversationMessage(threadId).first(),
                    blocked = convo.consentState() == ConsentState.DENIED,
                    isUnknown = convo.consentState() == ConsentState.UNKNOWN
                ).also { conversationDao.upsertConversation(it) }
            }
        }
    }

    private suspend fun manageXmtpMessage(
        threadId: Long,
        msg: DecodedMessage,
        client: Client,
        replyReference: String = "", // empty if not a reply
        context: Context
    ) {
        val template = Message(
            id = msg.id,
            threadId = threadId,
            address = msg.senderAddress,
            type = "xmtp", // DO NOT CHANGE
            date = msg.sent.time, // for historical messages, new ones use System time
            dateSent = msg.sent.time,
            clientAddress = client.address,
            xmtpDeliveryStatus = msg.deliveryStatus,
            replyReference = replyReference // only for reply
        )

        //handle content types
        when (msg.encodedContent.type) {
            ContentTypeReadReceipt -> messageDao.getXmtpMessages(threadId)
                .map { it.copy(seenDate = msg.sent.time) }
                .also { messageDao.updateMessages(it) }

            ContentTypeReaction -> msg.content<Reaction>()?.let { reaction ->
                val unicode = if (reaction.schema == ReactionSchema.Shortcode) {
                    EmojiParser.parseToUnicode(reaction.content)
                } else reaction.content

                when (reaction.action) {
                    ReactionAction.Added -> reactionDao.upsertReaction(
                        MessageReaction(
                            id = msg.id,
                            senderAddress = msg.id,
                            unicode = unicode
                        ))

                    ReactionAction.Removed -> reactionDao.deleteReaction(msg.id)

                    //TODO: add fallback
                    ReactionAction.Unknown -> {}
                }
            }

            ContentTypeAttachment, ContentTypeRemoteAttachment -> {
                //TODO: This needs to be updated after MmsPart Message decoupling

                val content = msg.content() as? RemoteAttachment
                val attachment = if (content != null ) content.load<Attachment>() else msg.content<Attachment>()

                attachment?.let {
                    val name = attachment.filename

                    // don't save if plain text
                    val text = if (attachment.mimeType == ContentType.TEXT_PLAIN) {
                        attachment.data.toByteArray().toString(Charsets.UTF_8)
                    } else {
                        context.openFileOutput(name, Context.MODE_PRIVATE).use {
                            it.write(attachment.data.toByteArray())
                        }
                        ""
                    }

                    val mmsPart = MmsPart(
                        id = attachment.filename,
                        type = attachment.mimeType,
                        text = text
                    ).also { messageDao.upsertMessagePart(it) }

                    template.copy(parts = listOf(mmsPart)).also { messageDao.upsertMessage(it) }

                }
            }

            ContentTypeReply -> msg.content<Reply>()?.let { reply ->
                // recursive reply handling
                val newMessage = msg.copy(encodedContent = encodeContent(reply.content, reply.contentType))
                manageXmtpMessage(threadId, newMessage, client, reply.reference, context)
            }

            ContentTypeText -> template.copy(body = msg.body).also { messageDao.upsertMessage(it) }
            else -> {  }
        }
    }

    override suspend fun startStreamAllMessages(client: Client) {
        client.conversations.list().forEach { conversation ->
            conversation.streamMessages().collect {
                val threadId = TelephonyCompat.getOrCreateThreadId(context, conversation.peerAddresses)
                manageXmtpMessage(
                    threadId = threadId,
                    msg = it,
                    client = client,
                    context = context
                )
            }
        }
    }


    // Copied from XMTP sdk
    private fun <T> encodeContent(content: T, id: Content.ContentTypeId): EncodedContent {
        val codec = Client.codecRegistry.find(id)

        fun <Codec : ContentCodec<T>> encode(codec: Codec, content: Any?): EncodedContent {
            val contentType = content as? T
            if (contentType != null) {
                return codec.encode(contentType)
            } else {
                throw XMTPException("Codec type is not registered")
            }
        }

        var encoded = encode(codec = codec as ContentCodec<T>, content = content)
        val fallback = codec.fallback(content)
        if (!fallback.isNullOrBlank()) {
            encoded = encoded.toBuilder().also {
                it.fallback = fallback
            }.build()
        }

        return encoded
    }


    // Assuming you have registered all codecs in the Client's codecRegistry

    /*
    fun encodeReplyContent(reply: Reply, codecRegistry: CodecRegistry): EncodedContent? {
        val codec = codecRegistry.find(reply.contentType) as? ContentCodec<Any>
        return (reply.content as? Any)?.let { codec?.encode(it) }
    }
     */
}