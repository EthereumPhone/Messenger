package org.ethereumhpone.data.repository

import android.content.Context
import android.util.Log
import io.basenameservice.BaseNameResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.database.model.relation.CompositeConversation
import org.ethereumhpone.database.model.relation.toExternalModel
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumphone.model.Conversation
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.PublicIdentity
import javax.inject.Inject
import org.ethereumhpone.common.util.Result
import org.ethereumhpone.database.model.relation.ConversationRecipientCrossRef
import org.kethereum.ens.ENS
import org.kethereum.model.Address
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.isPotentialENSDomain
import org.ethereumphone.dgenlibrary.showDgenToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.xmtp.android.library.Conversation as XmtpConversation
import org.xmtp.android.library.Group

class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val conversationDao: ConversationDao,
    private val contactDao: ContactDao,
    private val recipientDao: RecipientDao,
    private val messageDao: MessageDao,
    private val xmtpClientManager: XmtpClientManager,
    private val ensResolver: ENS,
    private val baseNameResolver: BaseNameResolver
): ConversationRepository {
    override fun getConversations(): Flow<List<Conversation>> =
        conversationDao.getConversations()
            .map { it.map(CompositeConversation::toExternalModel) }

    override fun getConversation(conversationId: String): Flow<Conversation?> =
        conversationDao.getConversation(conversationId)
            .map { it?.toExternalModel() }

    override fun getUnreadConversations(): Flow<List<Conversation>> =
        conversationDao.getConversationsWithUnseenMessages()
            .map { it.map(CompositeConversation::toExternalModel) }

    override fun getUnreadConversationsForNotifications(): Flow<List<Conversation>> =
        conversationDao.getConversationsWithUnseenMessagesForNotifications()
            .map { it.map(CompositeConversation::toExternalModel) }

    override fun createConversation(
        addresses: List<String>,
        preResolvedAddresses: Map<String, String>?,
        groupName: String?,
        groupDescription: String?,
        groupImageUrl: String?
    ): Flow<Result<Conversation>> = flow {
        // First check if a conversation already exists with the ENS name as title
        if (addresses.size == 1) {
            val address = addresses.first()
            // Check if conversation exists with ENS name as title
            if (address.isValidEns() || address.isValidBaseEns()) {
                val existingByTitle = conversationDao.getConversations().first()
                    .map { it.toExternalModel() }
                    .find { it.title?.equals(address, ignoreCase = true) == true }
                if (existingByTitle != null) {
                    emit(Result.Success(existingByTitle))
                    return@flow
                }
            }

        }
        
        // Normalize and resolve addresses
        val normalizedAddresses = addresses.map { it.normalizedString() }
        
        val recipients = recipientDao.getRecipientsByAddress(normalizedAddresses).first()
        // Wait until XMTP client is ready before making any client calls to avoid IllegalStateException
        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
        val client = xmtpClientManager.client
        val inboxIds = recipients.map { it.inboxId }

        val allRecipientsFound = inboxIds.size == normalizedAddresses.size
        if (!allRecipientsFound) {
            // Resolve ENS names to Ethereum addresses in parallel for performance
            val resolvedAddresses = withContext(Dispatchers.IO) {
                coroutineScope {
                    normalizedAddresses.mapIndexed { index, address ->
                        async {
                            // Check if we have a pre-resolved address for this ENS/Base name
                            val preResolved = preResolvedAddresses?.get(address)
                            if (preResolved != null) {
                                Log.d("ConversationRepo", "⚡ Using pre-resolved address for '$address': $preResolved")
                                preResolved
                            } else if (address.isValidEns() && !address.isValidBaseEns()) {
                                try {
                                    val resolvedAddress = ensResolver.getAddress(ENSName(address))
                                    if (resolvedAddress != null) {
                                        resolvedAddress.toString()
                                    } else {
                                        showDgenToast(context, "Could not resolve ENS name: ${addresses[index]}")
                                        null
                                    }
                                } catch (e: Exception) {
                                    Log.e("ConversationRepo", "ENS resolution failed for ${addresses[index]}", e)
                                    showDgenToast(context, "Could not resolve ENS name: ${addresses[index]}")
                                    null
                                }
                            } else if(address.isValidBaseEns()) {
                                try {
                                    val resolveAddress = baseNameResolver.resolve(address)

                                    if (resolveAddress.error == null) {
                                        resolveAddress.address!!
                                    } else {
                                        showDgenToast(context, "Could not resolve ENS name: ${addresses[index]}")
                                        null
                                    }
                                } catch (e: Exception) {
                                    Log.e("ConversationRepo", "Base Name resolution failed for ${addresses[index]}", e)
                                    showDgenToast(context, "Could not resolve Base Name: ${addresses[index]}")
                                    null
                                }
                            } else {
                                address
                            }
                        }
                    }.map { it.await() }
                }
            }
            
            // Check if any ENS resolution failed
            if (resolvedAddresses.contains(null)) {
                emit(Result.Error("Could not resolve ENS name"))
                return@flow
            }
            
            @Suppress("UNCHECKED_CAST")
            val validResolvedAddresses = resolvedAddresses as List<String>
            
            // Check again for recipients with resolved addresses
            val resolvedRecipients = recipientDao.getRecipientsByAddress(validResolvedAddresses).first()
            if (resolvedRecipients.isNotEmpty()) {
                // Found recipients with resolved addresses - check for existing conversation
                val resolvedInboxIds = resolvedRecipients.map { it.inboxId }
                val existingConversation = conversationDao
                    .getCompositeConversationByExactMembers(resolvedInboxIds)
                    .first()
                    ?.toExternalModel()
                    
                if (existingConversation != null) {
                    emit(Result.Success(existingConversation))
                    return@flow
                }
            }
            
            val identities = validResolvedAddresses.map {
                PublicIdentity(kind = IdentityKind.ETHEREUM, identifier = it)
            }

            val consentMap = client.canMessage(identities)
            val notAllowed = consentMap.filterValues { !it }

            if (notAllowed.isNotEmpty()) {
                val addressText = if (notAllowed.keys.size == 1) "Address" else "Addresses"
                showDgenToast(context, "$addressText not registered with XMTP")
                emit(Result.Error("NOT_REGISTERED_WITH_XMTP"))
                return@flow
            }

            // Handle GROUP conversation creation when multiple addresses
            if (addresses.size > 1) {
                try {
                    // Get inbox IDs by first creating temporary DMs with each identity
                    // This is needed because newGroup requires inbox IDs
                    val memberInboxIdList = mutableListOf<String>()
                    for (identity in identities) {
                        try {
                            val dm = client.conversations.findOrCreateDmWithIdentity(identity)
                            memberInboxIdList.add(dm.peerInboxId)
                        } catch (e: Exception) {
                            Log.e("ConversationRepo", "Failed to get inbox ID for ${identity.identifier}", e)
                        }
                    }
                    
                    if (memberInboxIdList.size != identities.size) {
                        showDgenToast(context, "Some addresses could not be resolved to inbox IDs")
                        emit(Result.Error("Could not resolve all addresses to inbox IDs"))
                        return@flow
                    }
                    
                    // Create group with XMTP using inbox IDs
                    val group = client.conversations.newGroup(memberInboxIdList)
                    
                    // Update group metadata after creation
                    if (!groupName.isNullOrBlank()) {
                        try { group.updateName(groupName) } catch (e: Exception) { 
                            Log.w("ConversationRepo", "Failed to set group name", e) 
                        }
                    }
                    if (!groupDescription.isNullOrBlank()) {
                        try { group.updateDescription(groupDescription) } catch (e: Exception) { 
                            Log.w("ConversationRepo", "Failed to set group description", e) 
                        }
                    }
                    if (!groupImageUrl.isNullOrBlank()) {
                        try { group.updateImageUrl(groupImageUrl) } catch (e: Exception) { 
                            Log.w("ConversationRepo", "Failed to set group image", e) 
                        }
                    }
                    
                    Log.d("ConversationRepo", "Created new group: ${group.id}")
                    
                    // Get all members including self
                    val allMembers = group.members()
                    val memberInboxIds = allMembers.map { it.inboxId }
                    
                    // Create recipient entities for all members
                    val contacts = contactDao.getContacts().first()
                    val recipientEntities = allMembers.map { member ->
                        val memberAddress = member.identities.first { it.kind == IdentityKind.ETHEREUM }.identifier
                        
                        val matchedContact = contacts.firstOrNull { contact ->
                            contact.ethAddress?.equals(memberAddress, ignoreCase = true) == true
                        }
                        
                        RecipientEntity(
                            inboxId = member.inboxId,
                            address = memberAddress,
                            contactLookupKey = matchedContact?.lookupKey
                        )
                    }
                    recipientDao.insertRecipients(recipientEntities)
                    
                    // Create conversation entity
                    val conversationEntity = ConversationEntity(
                        id = group.id,
                        title = groupName ?: "Group Chat",
                        description = groupDescription,
                        members = memberInboxIds,
                        createdAt = group.createdAt.time,
                        clientInbox = client.inboxId,
                        deleted = false,
                        hideBefore = 0L,
                        isGroup = true,
                        imageUrl = groupImageUrl
                    )
                    conversationDao.insertConversation(conversationEntity)
                    
                    // Create cross-references
                    val refs = memberInboxIds.map { inboxId ->
                        ConversationRecipientCrossRef(group.id, inboxId)
                    }
                    conversationDao.insertConversationMemberCrossRefs(refs)
                    
                    showDgenToast(context, "Group created successfully")
                    emit(Result.Success(conversationDao.getConversation(group.id).first()!!.toExternalModel()))
                } catch (e: Exception) {
                    Log.e("ConversationRepo", "Failed to create group", e)
                    showDgenToast(context, "Failed to create group: ${e.message}")
                    emit(Result.Error(e.message ?: "Could not create group conversation"))
                }
                return@flow
            }

            // Handle direct message creation
            try {
                val identity = identities.first()
                val dm = client.conversations.findOrCreateDmWithIdentity(identity)

                // Use original ENS/Base name as title if provided
                val conversationTitle = if (addresses.size == 1 && 
                    (addresses.first().isValidEns() || addresses.first().isValidBaseEns())) {
                    addresses.first()
                } else {
                    null
                }
                
                val existing = conversationDao.getConversationEntityById(dm.id)
                val conversationEntity = ConversationEntity(
                    id = dm.id,
                    title = conversationTitle,
                    members = listOf(dm.peerInboxId),
                    createdAt = dm.createdAt.time,
                    clientInbox = client.inboxId,
                    deleted = false,
                    hideBefore = existing?.hideBefore ?: 0L,
                    isGroup = false
                )

                // Attempt to link the new recipient to an existing contact 
                // Check both original ENS name and resolved address (case-insensitive)
                val originalAddress = addresses.first() // Original input (might be ENS)
                val resolvedAddress = identity.identifier // Resolved Ethereum address
                
                val contactLookupKey = contactDao.getContacts().first()
                    .firstOrNull { contact ->
                        // Check if contact's ETH address matches the resolved address
                        contact.ethAddress?.equals(resolvedAddress, ignoreCase = true) == true ||
                        // Or if contact's ETH address matches the original ENS name
                        contact.ethAddress?.equals(originalAddress, ignoreCase = true) == true
                    }?.lookupKey

                val recipientEntity = RecipientEntity(
                    inboxId = dm.peerInboxId,
                    address = identity.identifier,
                    contactLookupKey = contactLookupKey
                )

                //
                Log.d("HELLO THERE", "IM HERERERER")
                recipientDao.insertRecipients(listOf(recipientEntity))
                conversationDao.insertConversation(conversationEntity)
                conversationDao.insertConversationMemberCrossRefs(
                    listOf(ConversationRecipientCrossRef(dm.id, dm.peerInboxId))
                )

                //TODO: might cause problems
                emit(Result.Success(conversationDao.getConversation(dm.id).first()!!.toExternalModel()))
            } catch (e: Exception) {
                emit(Result.Error(e.message ?: "Could not create DM conversation"))
            }
            return@flow
        }

        // All recipients found - check for existing conversation
        val existingConversation = conversationDao
            .getCompositeConversationByExactMembers(inboxIds)
            .first()
            ?.toExternalModel()

        if (existingConversation != null) {
            emit(Result.Success(existingConversation))
            return@flow
        }

        // Handle GROUP conversation with existing recipients
        if (inboxIds.size > 1) {
            try {
                // Create group with XMTP using existing inbox IDs (simple version)
                val group = client.conversations.newGroup(inboxIds)
                
                // Update group metadata after creation
                if (!groupName.isNullOrBlank()) {
                    try { group.updateName(groupName) } catch (e: Exception) { 
                        Log.w("ConversationRepo", "Failed to set group name", e) 
                    }
                }
                if (!groupDescription.isNullOrBlank()) {
                    try { group.updateDescription(groupDescription) } catch (e: Exception) { 
                        Log.w("ConversationRepo", "Failed to set group description", e) 
                    }
                }
                if (!groupImageUrl.isNullOrBlank()) {
                    try { group.updateImageUrl(groupImageUrl) } catch (e: Exception) { 
                        Log.w("ConversationRepo", "Failed to set group image", e) 
                    }
                }
                
                Log.d("ConversationRepo", "Created new group with existing recipients: ${group.id}")
                
                // Get all members including self
                val allMembers = group.members()
                val memberInboxIds = allMembers.map { it.inboxId }
                
                // Create conversation entity
                val conversationEntity = ConversationEntity(
                    id = group.id,
                    title = groupName ?: "Group Chat",
                    description = groupDescription,
                    members = memberInboxIds,
                    createdAt = group.createdAt.time,
                    clientInbox = client.inboxId,
                    deleted = false,
                    hideBefore = 0L,
                    isGroup = true,
                    imageUrl = groupImageUrl
                )
                conversationDao.insertConversation(conversationEntity)
                
                // Create cross-references
                val refs = memberInboxIds.map { inboxId ->
                    ConversationRecipientCrossRef(group.id, inboxId)
                }
                conversationDao.insertConversationMemberCrossRefs(refs)
                
                showDgenToast(context, "Group created successfully")
                emit(Result.Success(conversationDao.getConversation(group.id).first()!!.toExternalModel()))
            } catch (e: Exception) {
                Log.e("ConversationRepo", "Failed to create group", e)
                showDgenToast(context, "Failed to create group: ${e.message}")
                emit(Result.Error(e.message ?: "Could not create group conversation"))
            }
            return@flow
        }

        // Fallback: attempt to re-fetch or create DM
        val inboxId = inboxIds.first()
        val dm = client.conversations.findOrCreateDm(inboxId)
        val conversation = conversationDao.getConversation(dm.id).first()

        if (conversation != null) {
            emit(Result.Success(conversation.toExternalModel()))
        } else {
            // Use original ENS/Base name as title if provided
            val conversationTitle = if (addresses.size == 1 && 
                (addresses.first().isValidEns() || addresses.first().isValidBaseEns())) {
                addresses.first()
            } else {
                null
            }
            
            val existing = conversationDao.getConversationEntityById(dm.id)
            val newConversation = ConversationEntity(
                id = dm.id,
                title = conversationTitle,
                members = listOf(dm.peerInboxId),
                createdAt = dm.createdAt.time,
                clientInbox = client.inboxId,
                deleted = false,
                hideBefore = existing?.hideBefore ?: 0L,
                isGroup = false
            )
            conversationDao.insertConversation(newConversation)
            emitAll(conversationDao.getConversation(dm.id).map { Result.Success(it!!.toExternalModel()) })
        }
    }


    override suspend fun updatePinnedConversation(id: String, pinned: Boolean) {
        conversationDao.updatePinnedStatus(id, pinned)
    }

    override suspend fun updateArchivedConversation(id: String, archived: Boolean) {
        conversationDao.updateArchivedStatus(id, archived)
    }

    override suspend fun updateBlockedConversation(id: String, blocked: Boolean) {
        conversationDao.updateBlockedStatus(id, blocked)
    }

    override suspend fun updateSeenConversation(id: String, seen: Boolean) {
        val conversation = conversationDao.getConversation(id).first()
        conversation?.lastMessageEntity?.let {
            messageDao.updateSeenMessage(it.id, seen)
        }
    }

    override suspend fun deleteConversation(id: String) {
        val cutoff = System.currentTimeMillis()
        conversationDao.softDeleteConversation(id, cutoff)
    }
    
    override suspend fun updateGroupName(conversationId: String, name: String): Result<Unit> {
        return try {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            val client = xmtpClientManager.client
            
            val conversation = client.conversations.findConversation(conversationId)
            if (conversation == null) {
                return Result.Error("Conversation not found")
            }
            
            if (conversation.type != XmtpConversation.Type.GROUP) {
                return Result.Error("Not a group conversation")
            }
            
            val group = (conversation as XmtpConversation.Group).group
            group.updateName(name)
            
            // Update local database
            val existing = conversationDao.getConversationEntityById(conversationId)
            if (existing != null) {
                conversationDao.insertConversation(existing.copy(title = name))
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Failed to update group name", e)
            Result.Error(e.message ?: "Failed to update group name")
        }
    }
    
    override suspend fun updateGroupDescription(conversationId: String, description: String): Result<Unit> {
        return try {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            val client = xmtpClientManager.client
            
            val conversation = client.conversations.findConversation(conversationId)
            if (conversation == null) {
                return Result.Error("Conversation not found")
            }
            
            if (conversation.type != XmtpConversation.Type.GROUP) {
                return Result.Error("Not a group conversation")
            }
            
            val group = (conversation as XmtpConversation.Group).group
            group.updateDescription(description)
            
            // Update local database
            val existing = conversationDao.getConversationEntityById(conversationId)
            if (existing != null) {
                conversationDao.insertConversation(existing.copy(description = description))
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Failed to update group description", e)
            Result.Error(e.message ?: "Failed to update group description")
        }
    }
    
    override suspend fun updateGroupImageUrl(conversationId: String, imageUrl: String): Result<Unit> {
        return try {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            val client = xmtpClientManager.client
            
            val conversation = client.conversations.findConversation(conversationId)
            if (conversation == null) {
                return Result.Error("Conversation not found")
            }
            
            if (conversation.type != XmtpConversation.Type.GROUP) {
                return Result.Error("Not a group conversation")
            }
            
            val group = (conversation as XmtpConversation.Group).group
            group.updateImageUrl(imageUrl)
            
            // Update local database
            val existing = conversationDao.getConversationEntityById(conversationId)
            if (existing != null) {
                conversationDao.insertConversation(existing.copy(imageUrl = imageUrl))
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Failed to update group image", e)
            Result.Error(e.message ?: "Failed to update group image")
        }
    }
    
    override suspend fun addGroupMembers(conversationId: String, addresses: List<String>): Result<Unit> {
        return try {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            val client = xmtpClientManager.client
            
            val conversation = client.conversations.findConversation(conversationId)
            if (conversation == null) {
                return Result.Error("Conversation not found")
            }
            
            if (conversation.type != XmtpConversation.Type.GROUP) {
                return Result.Error("Not a group conversation")
            }
            
            val group = (conversation as XmtpConversation.Group).group
            
            // Resolve addresses and create identities
            val identities = addresses.map { address ->
                val resolvedAddress = when {
                    address.isValidEns() && !address.isValidBaseEns() -> {
                        ensResolver.getAddress(ENSName(address))?.toString() ?: address
                    }
                    address.isValidBaseEns() -> {
                        val result = baseNameResolver.resolve(address)
                        if (result.error == null) result.address!! else address
                    }
                    else -> address
                }
                PublicIdentity(kind = IdentityKind.ETHEREUM, identifier = resolvedAddress)
            }
            
            // Check if all addresses can be messaged
            val canMessageMap = client.canMessage(identities)
            val notAllowed = canMessageMap.filterValues { !it }
            if (notAllowed.isNotEmpty()) {
                showDgenToast(context, "Some addresses are not registered with XMTP")
                return Result.Error("NOT_REGISTERED_WITH_XMTP")
            }
            
            // Add members to the group
            group.addMembersByIdentity(identities)
            
            // Sync and update local database
            group.sync()
            
            val allMembers = group.members()
            val memberInboxIds = allMembers.map { it.inboxId }
            
            // Create recipient entities for new members
            val contacts = contactDao.getContacts().first()
            val recipientEntities = allMembers.map { member ->
                val memberAddress = member.identities.first { it.kind == IdentityKind.ETHEREUM }.identifier
                
                val matchedContact = contacts.firstOrNull { contact ->
                    contact.ethAddress?.equals(memberAddress, ignoreCase = true) == true
                }
                
                RecipientEntity(
                    inboxId = member.inboxId,
                    address = memberAddress,
                    contactLookupKey = matchedContact?.lookupKey
                )
            }
            recipientDao.insertRecipients(recipientEntities)
            
            // Update conversation members
            val existing = conversationDao.getConversationEntityById(conversationId)
            if (existing != null) {
                conversationDao.insertConversation(existing.copy(members = memberInboxIds))
            }
            
            // Create cross-references for new members
            val refs = memberInboxIds.map { inboxId ->
                ConversationRecipientCrossRef(conversationId, inboxId)
            }
            conversationDao.insertConversationMemberCrossRefs(refs)
            
            // Remove stale cross-refs for members no longer in the XMTP group
            // (e.g. members removed by concurrent operations whose sync hadn't propagated yet)
            conversationDao.deleteRemovedMemberCrossRefs(conversationId, memberInboxIds)
            
            showDgenToast(context, "Members added successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Failed to add group members", e)
            showDgenToast(context, "Failed to add members: ${e.message}")
            Result.Error(e.message ?: "Failed to add group members")
        }
    }
    
    override suspend fun removeGroupMembers(conversationId: String, inboxIds: List<String>): Result<Unit> {
        return try {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            val client = xmtpClientManager.client
            
            val conversation = client.conversations.findConversation(conversationId)
            if (conversation == null) {
                return Result.Error("Conversation not found")
            }
            
            if (conversation.type != XmtpConversation.Type.GROUP) {
                return Result.Error("Not a group conversation")
            }
            
            val group = (conversation as XmtpConversation.Group).group
            
            // Remove members from the XMTP group
            group.removeMembers(inboxIds)
            
            // Immediately remove cross-refs for the removed members so the UI updates
            // even if group.sync()/members() hasn't propagated the change yet
            conversationDao.deleteMemberCrossRefs(conversationId, inboxIds)
            
            // Update the conversation entity's member list
            val existing = conversationDao.getConversationEntityById(conversationId)
            if (existing != null) {
                val updatedMembers = existing.members.filterNot { it in inboxIds }
                conversationDao.insertConversation(existing.copy(members = updatedMembers))
            }
            
            // Sync in background to reconcile with server state
            try {
                group.sync()
                // After syncing, reconcile cross-refs with the authoritative XMTP member list
                // to ensure no stale cross-refs remain
                val currentMembers = group.members().map { it.inboxId }
                if (currentMembers.isNotEmpty()) {
                    conversationDao.deleteRemovedMemberCrossRefs(conversationId, currentMembers)
                }
            } catch (syncError: Exception) {
                Log.w("ConversationRepo", "Post-remove sync failed (removal still succeeded)", syncError)
            }
            
            showDgenToast(context, "Member removed")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Failed to remove group members", e)
            showDgenToast(context, "Failed to remove member: ${e.message}")
            Result.Error(e.message ?: "Failed to remove group members")
        }
    }
    
    override suspend fun leaveGroup(conversationId: String): Result<Unit> {
        return try {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            val client = xmtpClientManager.client
            
            val conversation = client.conversations.findConversation(conversationId)
            if (conversation == null) {
                return Result.Error("Conversation not found")
            }
            
            if (conversation.type != XmtpConversation.Type.GROUP) {
                return Result.Error("Not a group conversation")
            }
            
            val group = (conversation as XmtpConversation.Group).group
            
            group.leaveGroup()
            
            // Mark the conversation as deleted locally
            val cutoff = System.currentTimeMillis()
            conversationDao.softDeleteConversation(conversationId, cutoff)
            
            showDgenToast(context, "Left the group")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Failed to leave group", e)
            showDgenToast(context, "Failed to leave group: ${e.message}")
            Result.Error(e.message ?: "Failed to leave group")
        }
    }
}

// Extension functions for ENS validation and string normalization
private fun String.normalizedString(): String = this.replace("\\s".toRegex(), "").lowercase()
private fun String.isValidEns(): Boolean = try {
    ENSName(this).isPotentialENSDomain()
} catch (e: Exception) { false }

private fun String.isValidBaseEns(): Boolean = this.contains(Regex("^[a-z0-9]{3,}\\.base\\.eth$", RegexOption.IGNORE_CASE))


private fun String.isValidEthAddress(): Boolean = this.matches(Regex("^0x[a-fA-F0-9]{40}$"))