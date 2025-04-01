package org.ethereumhpone.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.media3.common.util.SystemClock
import com.google.android.mms.ContentType
import com.google.common.base.Utf8
import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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
import org.ethereumphone.walletsdk.WalletSDK
import org.xmtp.android.library.Client
import org.xmtp.android.library.ConsentState
import org.xmtp.android.library.Conversation.Type
import org.xmtp.android.library.XMTPException
import org.xmtp.android.library.codecs.Attachment
import org.xmtp.android.library.codecs.ContentCodec
import org.xmtp.android.library.codecs.ContentTypeAttachment
import org.xmtp.android.library.codecs.ContentTypeReaction
import org.xmtp.android.library.codecs.ContentTypeReactionV2
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
import org.xmtp.android.library.codecs.id
import org.xmtp.android.library.libxmtp.DecodedMessage
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.proto.message.contents.Content
import org.xmtp.proto.message.contents.MessageOuterClass
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlin.random.Random


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


        logTimeHandler.setLastLog(SyncLog().date)
        _isSyncing.value = false
    }

    override suspend fun syncMessage(uri: Uri): Message? {
        TODO("Not yet implemented")
    }

    override suspend fun syncContacts() {
        val contacts = getContacts()

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

    override suspend fun syncXmtp() = coroutineScope {
        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }.let {
            val client = xmtpClientManager.client

            val syncJob = launch {
                client.preferences.syncConsent()
                client.conversations.syncAllConversations()
            }
            syncJob.join()


            client.conversations.list().forEach { conversation ->
                // recipients
                launch {
                    //TODO: Add refs to contacts
                    val members = conversation.members()

                    val recipients = members.map { member ->
                        Recipient(
                            inboxId = member.inboxId,
                            address = member.identities.first { it.kind == IdentityKind.ETHEREUM }.identifier,
                            ens = null,
                            contactLookupKey = null
                        )
                    }
                    recipientDao.insertRecipients(recipients)
                }

                // conversation
                launch {
                    val members = conversation.members().map { it.inboxId }
                    //val consent = conversation.consentState()

                    val parsedConversation = Conversation(
                        id = conversation.id,
                        title = null, // change
                        members = members,
                    )
                    conversationDao.upsertConversation(parsedConversation)
                }


                // messages
                launch {
                    val messages = conversation.messagesWithReactions()

                    messages.chunked(10) { messageChunk ->
                        launch {
                            val parsedMessages = messageChunk.map { msg ->
                                val template = Message(
                                    id = msg.id,
                                    threadId = msg.conversationId,
                                    senderInboxId = msg.senderInboxId,
                                    date = System.currentTimeMillis(),
                                    seenDate = msg.sentAtNs,
                                    xmtpDeliveryStatus = msg.deliveryStatus,
                                    isMe = msg.senderInboxId == client.inboxId,
                                    replyReference = null,
                                    body = msg.body
                                )
                                processContent(template, msg.encodedContent.type, msg.content())
                            }
                            messageDao.insertMessages(parsedMessages.filterNotNull())
                        }
                    }

                }
            }
        }
    }

    private suspend fun processContent(message: Message, contentType: Content.ContentTypeId, content: Any?): Message? {
        return when(contentType) {
            // Handle reactions
            ContentTypeReactionV2 -> {
                val xmtpReaction = content as Reaction

                if (xmtpReaction.action == ReactionAction.Removed) {
                    reactionDao.deleteReaction(message.id)
                }
                if(xmtpReaction.action == ReactionAction.Added) {
                    val reaction = MessageReaction(
                        id = message.id,
                        inboxId = message.senderInboxId,
                        unicode = xmtpReaction.content
                    )
                    reactionDao.upsertReaction(reaction)
                }
                null
            }
            // Handle read receipts
            ContentTypeReadReceipt -> {
                null
            }
            // Handle replies
            ContentTypeReply -> {
                val reply = content as Reply

                val updatedMessage = message.copy(replyReference = reply.reference)
                processContent(updatedMessage, reply.contentType, reply.content)
            }

            ContentTypeAttachment, ContentTypeRemoteAttachment -> {
                null
            }
            else -> message // assume plain text
        }
    }

    override suspend fun startStreamAllMessages() {
        xmtpClientManager.clientState.collectLatest { clientState ->
            when(clientState) {
                is XmtpClientManager.ClientState.Ready -> {
                    val client = xmtpClientManager.client
                    client.conversations.list().forEach { conversation ->
                        conversation.streamMessages().collect {

                            val threadId = TelephonyCompat.getOrCreateThreadId(context, conversation.id)

                        }
                    }
                }
                is XmtpClientManager.ClientState.Error -> {

                }

                is XmtpClientManager.ClientState.Unknown -> {

                }
            }
        }
    }
}