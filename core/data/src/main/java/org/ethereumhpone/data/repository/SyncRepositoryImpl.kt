package org.ethereumhpone.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.common.extensions.map
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.PhoneNumberDao
import org.ethereumhpone.database.dao.ReactionDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.dao.SyncLogDao
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.database.model.ContactGroup
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.database.model.SyncLog
import org.ethereumhpone.database.model.relation.ConversationRecipientCrossRef
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.manager.AppStateMonitor
import org.ethereumhpone.domain.manager.NotificationManager
import org.ethereumhpone.domain.mapper.ContactCursor
import org.ethereumhpone.domain.mapper.ContactGroupCursor
import org.ethereumhpone.domain.mapper.ContactGroupMemberCursor
import org.ethereumhpone.domain.model.LogTimeHandler
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository
import org.kethereum.ens.ENS
import org.kethereum.*
import org.kethereum.model.Address
import org.xmtp.android.library.ConsentState
import org.xmtp.android.library.Conversation
import org.xmtp.android.library.codecs.ContentTypeAttachment
import org.xmtp.android.library.codecs.ContentTypeReactionV2
import org.xmtp.android.library.codecs.ContentTypeReadReceipt
import org.xmtp.android.library.codecs.ContentTypeRemoteAttachment
import org.xmtp.android.library.codecs.ContentTypeReply
import org.xmtp.android.library.codecs.Reaction
import org.xmtp.android.library.codecs.ReactionAction
import org.xmtp.android.library.codecs.Reply
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.proto.message.contents.Content
import javax.inject.Inject
import org.ethereumhpone.domain.manager.NetworkManager
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withTimeoutOrNull


class SyncRepositoryImpl @Inject constructor(
    private val context: Context,
    private val xmtpClientManager: XmtpClientManager,
    private val contentResolver: ContentResolver,
    private val activeConversationManager: ActiveConversationManager,
    private val conversationRepository: ConversationRepository,
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
    private val ensResolver: ENS,
    private val logTimeHandler: LogTimeHandler,
    private val notificationManager: NotificationManager,
    private val networkManager: NetworkManager,
    private val appStateMonitor: AppStateMonitor,
): SyncRepository {
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: Flow<Boolean> = _isSyncing.asStateFlow()
    companion object {
        private const val TAG = "SyncRepositoryImpl"
    }


    override suspend fun syncMessages() {
        // once sync at the time
        if(_isSyncing.value) return
        _isSyncing.value = true



        logTimeHandler.setLastLog(SyncLog().date)
        _isSyncing.value = false
    }

    override suspend fun syncMessage(uri: Uri): MessageEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun syncContacts() {
        val contacts = getContacts()
        contactDao.insertContacts(contacts)
    }

    private suspend fun getContacts(): List<ContactEntity> {
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


    private fun getContactGroups(contactEntities: List<ContactEntity>): List<ContactGroup> {
        val groupMembers = contactGroupMemberCursor.getGroupMembersCursor()?.use { cursor ->
            cursor.map(contactGroupMemberCursor::map).toList()
        }.orEmpty()

        val groups = contactGroupCursor.getContactGroupsCursor()?.use { cursor ->
            cursor.map(contactGroupCursor::map).toList()
        }.orEmpty()

        return groups.map { group ->
            group.copy(
                contactEntities = groupMembers
                    .filter { member -> member.groupId == group.id }
                    .mapNotNull { member -> contactEntities.find { contact -> contact.lookupKey == member.lookupKey } }
            )
        }
    }

    override suspend fun syncXmtp() = coroutineScope {
        // Wait for the client to be ready
        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
        
        val client = xmtpClientManager.client

        val syncJob = launch {
            client.preferences.syncConsent()
            val test = client.conversations.syncAllConversations()

            Log.d("SYNC XMTP", "Synced conversations: $test")
        }
        syncJob.join()

        client.conversations.list().forEach { conversation ->
            // recipients
            launch {
                //TODO: Add refs to contacts
                val members = conversation.members()


                // Fetch contacts once to match ETH addresses (case-insensitive)
                val contacts = contactDao.getContacts().first()

                val recipientEntities = members.map { member ->
                    val address = member.identities.first { it.kind == IdentityKind.ETHEREUM }.identifier
                    val ensAddress = ensResolver.reverseResolve(Address(address.removePrefix("0x")))

                    // Try to find a contact with the same ETH address (ignoring case)
                    val matchedContact = contacts.firstOrNull { contact ->
                        contact.ethAddress?.equals(address, ignoreCase = true) == true
                    }

                    RecipientEntity(
                        inboxId = member.inboxId,
                        address = address,
                        ens = ensAddress,
                        contactLookupKey = matchedContact?.lookupKey // link to contact if found
                    )
                }
                recipientDao.insertRecipients(recipientEntities)

            }

            // conversation
            launch {
                val members = conversation.members().map { it.inboxId }

                // refs
                val refs = members.map { inboxId ->
                    ConversationRecipientCrossRef(conversation.id, inboxId)
                }
                conversationDao.insertConversationMemberCrossRefs(refs)


                val existing = conversationDao.getConversationEntityById(conversation.id)
                
                val (id, title, createdAt, archived, consentState) = when (conversation.type) {
                    Conversation.Type.DM -> {
                        val dm = (conversation as Conversation.Dm).dm
                        listOf(
                            dm.id,
                            existing?.title, // Preserve existing title (e.g., ENS name)
                            dm.createdAt.time,
                            false, // TODO: Add a way to fill this
                            dm.consentState()
                        )
                    }

                    Conversation.Type.GROUP -> {
                        val group = (conversation as Conversation.Group).group
                        listOf(
                            group.id,
                            existing?.title ?: group.name, // Preserve existing title or use group name
                            group.createdAt.time,
                            !group.isActive(),
                            group.consentState()
                        )
                    }
                }

                val conversationEntity = ConversationEntity(
                    id = id as String,
                    title = title as String?,
                    members = members,
                    createdAt = createdAt as Long,
                    archived = archived as Boolean,
                    unknown = consentState == ConsentState.UNKNOWN,
                    blocked = consentState == ConsentState.DENIED,
                    clientInbox = client.inboxId,
                    deleted = existing?.deleted ?: false,
                    hideBefore = existing?.hideBefore ?: 0L
                )

                Log.d("INSERT CONVERSATION", id)
                conversationDao.insertConversation(conversationEntity)


            }


            // messages
            launch {
                val messages = conversation.messagesWithReactions()

                messages.chunked(10) { messageChunk ->
                    launch {

                        val parsedMessages = messageChunk.mapNotNull { msg ->
                            // Skip read receipts or empty text messages
                            if (msg.encodedContent.type == ContentTypeReadReceipt || (msg.body.isNullOrBlank())) {
                                messageDao.updateMessageSeenDate(msg.sentAtNs / 1_000_000)
                                return@mapNotNull null
                            }
                            
                            val baseMessage = MessageEntity(
                                id = msg.id,
                                threadId = msg.conversationId,
                                senderInboxId = msg.senderInboxId,
                                date = msg.sentAtNs / 1_000_000, // convert from nanoseconds to milliseconds
                                dateSent = msg.sentAtNs / 1_000_000, // convert from nanoseconds to milliseconds
                                deliveryStatus = msg.deliveryStatus,
                                isMe = msg.senderInboxId == client.inboxId,
                                replyReference = null,
                                body = msg.body
                            )

                            val processed = processContent(baseMessage, msg.encodedContent.type, msg.content())

                            processed?.let { newMessage ->
                                val existingMessage = messageDao.getMessage(msg.id).firstOrNull()
                                existingMessage?.let {
                                    newMessage.copy(
                                        seen = it.seen,
                                        read = it.read
                                    )
                                } ?: newMessage
                            }
                        }

                        messageDao.insertMessages(parsedMessages)
                    }
                }
            }
        }
    }

    override suspend fun startStream() = coroutineScope {
        // Listen to network connectivity and restart streams/sync when we regain a connection.
        networkManager.isOnline
            .distinctUntilChanged()
            .collectLatest { isOnline ->
                if (!isOnline) return@collectLatest // Wait until we're online again

                xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
                syncXmtp() // inital sync
                val client = xmtpClientManager.client
                launch {
                    client.conversations
                        .stream()
                        .collect { conversation ->

                            // recipients portion

                            //TODO: Add refs to contacts
                            val members = conversation.members()

                            Log.d(TAG, "Conversation members count: ${members.size}")
                            val recipientEntities = members.map { member ->
                                //TODO: Might fire too often.
                                val address = member.identities.first { it.kind == IdentityKind.ETHEREUM }.identifier
                                val ensAddress = ensResolver.reverseResolve(Address(address.removePrefix("0x")))

                                RecipientEntity(
                                    inboxId = member.inboxId,
                                    address = member.identities.first { it.kind == IdentityKind.ETHEREUM }.identifier,
                                    ens = ensAddress,
                                    contactLookupKey = null // TODO: Get contact lookupKeys
                                )
                            }
                            recipientDao.insertRecipients(recipientEntities)

                            val inboxIds = conversation.members().map { it.inboxId }
                            val refs = inboxIds.map { inboxId ->
                                ConversationRecipientCrossRef(conversation.id, inboxId)
                            }
                            conversationDao.insertConversationMemberCrossRefs(refs)

                            // conversation portion

                            val existing = conversationDao.getConversationEntityById(conversation.id)
                            
                            val (id, title, createdAt, archived, consentState) = when (conversation.type) {
                                Conversation.Type.DM -> {
                                    val dm = (conversation as Conversation.Dm).dm
                                    listOf(
                                        dm.id,
                                        existing?.title, // Preserve existing title (e.g., ENS name)
                                        dm.createdAt.time,
                                        false,
                                        dm.consentState()
                                    )
                                }

                                Conversation.Type.GROUP -> {
                                    val group = (conversation as Conversation.Group).group
                                    listOf(
                                        group.id,
                                        existing?.title ?: group.name, // Preserve existing title or use group name
                                        group.createdAt.time,
                                        !group.isActive(),
                                        group.consentState()
                                    )
                                }
                            }

                            val conversationEntity = ConversationEntity(
                                id = id as String,
                                title = title as String?,
                                members = inboxIds,
                                createdAt = createdAt as Long,
                                archived = archived as Boolean,
                                unknown = consentState == ConsentState.UNKNOWN,
                                blocked = consentState == ConsentState.DENIED,
                                clientInbox = client.inboxId,
                                deleted = existing?.deleted ?: false,
                                hideBefore = existing?.hideBefore ?: 0L
                            )
                            conversationDao.insertConversation(conversationEntity)
                        }
                }

                // Stream new messages
                launch {
                    client.conversations
                        .streamAllMessages()
                        .collect { message ->

                            val isMe = client.inboxId == message.senderInboxId

                            Log.d("incoming MESSAGE id", message.id)
                            
                            // Skip read receipts or empty text messages
                            if (message.encodedContent.type == ContentTypeReadReceipt || (message.body.isNullOrBlank())) {
                                messageDao.updateMessageSeenDate(message.sentAtNs / 1_000_000)
                                return@collect
                            }

                            val template = MessageEntity(
                                id = message.id,
                                threadId = message.conversationId,
                                body = message.body,
                                senderInboxId = message.senderInboxId,
                                replyReference = null,
                                deliveryStatus = message.deliveryStatus,
                                isMe = isMe,
                                date = message.sentAtNs / 1_000_000, // convert from nanoseconds to milliseconds
                                dateSent = message.sentAtNs / 1_000_000, // convert from nanoseconds to milliseconds
                            )

                            val processedMessage = processContent(template, message.encodedContent.type, message.content())
                            if (processedMessage != null) {
                                val localMessage = messageDao.getMessage(message.id).firstOrNull()
                                val updatedMessage = localMessage?.let {
                                    processedMessage.copy(
                                        seen = it.seen,
                                        read = it.read
                                    )
                                } ?: processedMessage

                                messageDao.upsertMessages(listOf(updatedMessage))
                                notifyForConversationIfNeeded(updatedMessage.threadId, isMe)
                            }


                            /* //TODO: Fix: Read-Receipt get displayed as normal message
                            // automatically send readReceipt for active conversation
                            if (!isMe && activeConversationManager.getActiveConversation() == message.conversationId) {
                                if (conversation != null) {
                                    try {
                                        conversation!!.send(
                                            content = ReadReceipt,
                                            options = SendOptions(contentType = ContentTypeReadReceipt)
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    try {
                                        val fetchedConversation = client.conversations.findConversation(message.conversationId)!!
                                        conversation = fetchedConversation

                                        fetchedConversation.send(
                                            content = ReadReceipt,
                                            options = SendOptions(contentType = ContentTypeReadReceipt)
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                             */

                        }
                }
            }
    }


    /**
     * syncNow() - Called by the OS-level XMTPNotificationsService every 5 minutes.
     * 
     * This method MUST:
     * 1. Call syncAllConversations() to pull new data from the XMTP network
     * 2. Process any new messages
     * 3. Store them in the local database
     * 4. Trigger notifications
     * 
     * Returns the count of new messages received.
     */
    override suspend fun syncNow(): Int = coroutineScope {
        Log.i(TAG, "syncNow() starting - fetching from XMTP network...")
        
        var newMessageCount = 0
        
        try {
            // Wait for client to be ready (should already be ready if MsgSyncService initialized it)
            val clientReady = withTimeoutOrNull(10_000L) {
                xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            }
            
            if (clientReady == null) {
                Log.w(TAG, "syncNow(): XMTP client not ready, aborting sync")
                return@coroutineScope 0
            }
            
            val client = xmtpClientManager.client
            val myInboxId = client.inboxId
            
            // CRITICAL: This is what actually pulls new messages from the XMTP network!
            // Without this call, we only read from local cache.
            Log.i(TAG, "syncNow(): Calling syncAllConversations() to fetch from network...")
            val syncedConversationCount = client.conversations.syncAllConversations()
            Log.i(TAG, "syncNow(): Synced $syncedConversationCount conversations from network")
            
            // Also sync consent state
            try {
                client.preferences.syncConsent()
            } catch (e: Exception) {
                Log.w(TAG, "syncNow(): Failed to sync consent preferences", e)
            }
            
            // Get the timestamp of the most recent message we have locally
            // to determine which messages are actually new
            val lastKnownMessageTime = messageDao.getLatestMessageTime() ?: 0L
            Log.i(TAG, "syncNow(): Last known message time: $lastKnownMessageTime")
            
            // Process each conversation
            val conversations = client.conversations.list()
            Log.i(TAG, "syncNow(): Processing ${conversations.size} conversations")
            
            for (conversation in conversations) {
                try {
                    // Fetch messages for this conversation
                    // The sync above should have pulled them into the local XMTP database
                    val messages = conversation.messagesWithReactions()
                    
                    var conversationNewCount = 0
                    
                    for (msg in messages) {
                        // Skip if we already have this message or it's older than our last sync
                        val msgTimeMs = msg.sentAtNs / 1_000_000
                        
                        // Check if message already exists in our database
                        val existingMessage = messageDao.getMessage(msg.id).firstOrNull()
                        if (existingMessage != null) {
                            continue // Already have this message
                        }
                        
                        // Skip read receipts and empty messages
                        if (msg.encodedContent.type == ContentTypeReadReceipt || msg.body.isNullOrBlank()) {
                            if (msg.encodedContent.type == ContentTypeReadReceipt) {
                                messageDao.updateMessageSeenDate(msgTimeMs)
                            }
                            continue
                        }
                        
                        val isMe = myInboxId == msg.senderInboxId
                        
                        val baseMessage = MessageEntity(
                            id = msg.id,
                            threadId = msg.conversationId,
                            senderInboxId = msg.senderInboxId,
                            date = msgTimeMs,
                            dateSent = msgTimeMs,
                            deliveryStatus = msg.deliveryStatus,
                            isMe = isMe,
                            replyReference = null,
                            body = msg.body
                        )
                        
                        val processedMessage = processContent(baseMessage, msg.encodedContent.type, msg.content())
                        
                        if (processedMessage != null) {
                            messageDao.insertMessages(listOf(processedMessage))
                            conversationNewCount++
                            newMessageCount++
                            
                            Log.d(TAG, "syncNow(): New message ${msg.id} in conversation ${conversation.id}")
                            
                            // Trigger notification for new incoming messages (not from me)
                            notifyForConversationIfNeeded(conversation.id, isMe)
                        }
                    }
                    
                    // Also ensure the conversation entity exists in our database
                    ensureConversationExists(client, conversation)
                    
                    if (conversationNewCount > 0) {
                        Log.i(TAG, "syncNow(): Found $conversationNewCount new messages in conversation ${conversation.id}")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "syncNow(): Error processing conversation ${conversation.id}", e)
                }
            }
            
            Log.i(TAG, "syncNow() completed: $newMessageCount new messages total")
            
        } catch (e: Exception) {
            Log.e(TAG, "syncNow() failed with exception", e)
        }
        
        newMessageCount
    }
    
    /**
     * Ensures a conversation entity exists in our local database.
     * Called during syncNow() to make sure we have conversation metadata.
     */
    private suspend fun ensureConversationExists(client: org.xmtp.android.library.Client, conversation: Conversation) {
        try {
            val existing = conversationDao.getConversationEntityById(conversation.id)
            
            // Get members and create cross-refs
            val members = conversation.members()
            val inboxIds = members.map { it.inboxId }
            
            // Insert/update recipients
            val contacts = contactDao.getContacts().first()
            val recipientEntities = members.map { member ->
                val address = member.identities.first { it.kind == IdentityKind.ETHEREUM }.identifier
                val ensAddress = try {
                    ensResolver.reverseResolve(Address(address.removePrefix("0x")))
                } catch (e: Exception) { null }
                
                val matchedContact = contacts.firstOrNull { contact ->
                    contact.ethAddress?.equals(address, ignoreCase = true) == true
                }
                
                RecipientEntity(
                    inboxId = member.inboxId,
                    address = address,
                    ens = ensAddress,
                    contactLookupKey = matchedContact?.lookupKey
                )
            }
            recipientDao.insertRecipients(recipientEntities)
            
            // Insert conversation-member cross refs
            val refs = inboxIds.map { inboxId ->
                ConversationRecipientCrossRef(conversation.id, inboxId)
            }
            conversationDao.insertConversationMemberCrossRefs(refs)
            
            // Create/update conversation entity
            val (id, title, createdAt, archived, consentState) = when (conversation.type) {
                Conversation.Type.DM -> {
                    val dm = (conversation as Conversation.Dm).dm
                    listOf(
                        dm.id,
                        existing?.title,
                        dm.createdAt.time,
                        false,
                        dm.consentState()
                    )
                }
                Conversation.Type.GROUP -> {
                    val group = (conversation as Conversation.Group).group
                    listOf(
                        group.id,
                        existing?.title ?: group.name,
                        group.createdAt.time,
                        !group.isActive(),
                        group.consentState()
                    )
                }
            }
            
            val conversationEntity = ConversationEntity(
                id = id as String,
                title = title as String?,
                members = inboxIds,
                createdAt = createdAt as Long,
                archived = archived as Boolean,
                unknown = consentState == ConsentState.UNKNOWN,
                blocked = consentState == ConsentState.DENIED,
                clientInbox = client.inboxId,
                deleted = existing?.deleted ?: false,
                hideBefore = existing?.hideBefore ?: 0L
            )
            
            conversationDao.insertConversation(conversationEntity)
        } catch (e: Exception) {
            Log.w(TAG, "ensureConversationExists(): Failed for ${conversation.id}", e)
        }
    }

    private fun shouldNotify(conversationId: String, isMessageFromMe: Boolean): Boolean {
        if (isMessageFromMe) {
            return false
        }

        val isForeground = appStateMonitor.isAppInForeground()
        val activeConversationId = activeConversationManager.getActiveConversation()

        if (isForeground && activeConversationId == conversationId) {
            Log.d(TAG, "Skipping notification for active conversation $conversationId while app is in foreground")
            return false
        }

        return true
    }

    private suspend fun notifyForConversationIfNeeded(conversationId: String, isMessageFromMe: Boolean) {
        if (!shouldNotify(conversationId, isMessageFromMe)) {
            return
        }

        try {
            notificationManager.update(conversationId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update notification for conversation $conversationId", e)
        }
    }

    private suspend fun processContent(messageEntity: MessageEntity, contentType: Content.ContentTypeId, content: Any?): MessageEntity? {
        return when(contentType) {
            // Handle reactions
            ContentTypeReactionV2 -> {
                val xmtpReaction = content as Reaction

                if (xmtpReaction.action == ReactionAction.Removed) {
                    reactionDao.deleteReaction(messageEntity.id)
                }
                if(xmtpReaction.action == ReactionAction.Added) {
                    val reactionEntity = org.ethereumhpone.database.model.ReactionEntity(
                        id = messageEntity.id,
                        inboxId = messageEntity.senderInboxId,
                        content = xmtpReaction.content
                    )
                    reactionDao.upsertReaction(reactionEntity)
                }
                null
            }
            // Handle read receipts
            ContentTypeReadReceipt -> {
                messageDao.updateMessageSeenDate(messageEntity.dateSent)
                null
            }
            // Handle replies
            ContentTypeReply -> {
                val reply = content as Reply

                val updatedMessage = messageEntity.copy(replyReference = reply.reference)
                processContent(updatedMessage, reply.contentType, reply.content)
            }

            ContentTypeAttachment, ContentTypeRemoteAttachment -> {
                null
            }
            else -> {
                if(messageEntity.body.isBlank()) {
                    // Empty text message likely a read receipt
                    return null
                }
                messageEntity // assume plain text
            }
        }
    }
}