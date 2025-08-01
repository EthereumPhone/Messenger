package org.ethereumhpone.data.repository

import android.content.Context
import android.util.Log
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
import org.ethereumphone.dgenlibrary.showDgenToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val conversationDao: ConversationDao,
    private val contactDao: ContactDao,
    private val recipientDao: RecipientDao,
    private val messageDao: MessageDao,
    private val xmtpClientManager: XmtpClientManager,
    private val ensResolver: ENS,
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

    override fun createConversation(addresses: List<String>): Flow<Result<Conversation>> = flow {
        // First check if a conversation already exists with the ENS name as title
        if (addresses.size == 1) {
            val address = addresses.first()
            // Check if conversation exists with ENS name as title
            if (address.isValidEns()) {
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
                            if (address.isValidEns()) {
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

            if (addresses.size > 1) {
                showDgenToast(context, "Group conversations not yet supported")
                return@flow
            }

            // Handle direct message creation
            try {
                val identity = identities.first()
                val dm = client.conversations.findOrCreateDmWithIdentity(identity)

                // Use original ENS name as title if provided
                val conversationTitle = if (addresses.size == 1 && addresses.first().isValidEns()) {
                    addresses.first()
                } else {
                    null
                }
                
                val conversationEntity = ConversationEntity(
                    id = dm.id,
                    title = conversationTitle,
                    members = listOf(dm.peerInboxId),
                    createdAt = dm.createdAt.time,
                    clientInbox = client.inboxId
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

        if (inboxIds.size > 1) {
            emit(Result.Error("Group conversations are not yet supported"))
            return@flow
        }

        // Fallback: attempt to re-fetch or create DM
        val inboxId = inboxIds.first()
        val dm = client.conversations.findOrCreateDm(inboxId)
        val conversation = conversationDao.getConversation(dm.id).first()

        if (conversation != null) {
            emit(Result.Success(conversation.toExternalModel()))
        } else {
            // Use original ENS name as title if provided
            val conversationTitle = if (addresses.size == 1 && addresses.first().isValidEns()) {
                addresses.first()
            } else {
                null
            }
            
            val newConversation = ConversationEntity(
                id = dm.id,
                title = conversationTitle,
                members = listOf(dm.peerInboxId),
                createdAt = dm.createdAt.time,
                clientInbox = client.inboxId
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
//            messageDao.updateSeenMessage(it.id, seen)
        }
    }

    override suspend fun deleteConversation(id: String) {
        conversationDao.deleteConversation(id)
    }
}

// Extension functions for ENS validation and string normalization
private fun String.normalizedString(): String = this.replace("\\s".toRegex(), "").lowercase()
private fun String.isValidEns(): Boolean = this.matches(Regex("^[a-zA-Z0-9-_\$]{3,}\\.eth$"))
private fun String.isValidEthAddress(): Boolean = this.matches(Regex("^0x[a-fA-F0-9]{40}$"))