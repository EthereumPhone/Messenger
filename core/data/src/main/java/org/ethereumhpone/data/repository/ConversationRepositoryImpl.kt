package org.ethereumhpone.data.repository

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Identity
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.common.extensions.map
import org.ethereumhpone.common.extensions.removeAccents
import org.ethereumhpone.common.util.Result
import org.ethereumhpone.common.util.tryOrNull
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.mapper.ConversationCursor
import org.ethereumhpone.domain.mapper.RecipientCursor
import org.ethereumhpone.domain.model.SearchResult
import org.ethereumhpone.domain.repository.ConversationRepository
import org.xmtp.android.library.Client
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.PublicIdentity
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val conversationDao: ConversationDao,
    private val contactDao: ContactDao,
    private val recipientDao: RecipientDao,
    private val messageDao: MessageDao,
    private val xmtpClientManager: XmtpClientManager,
): ConversationRepository {
    override fun getConversations(archived: Boolean): Flow<List<Conversation>> =
        conversationDao.getConversations(archived)

    override fun getConversations(vararg threadIds: Long): Flow<List<Conversation>> =
        conversationDao.getConversations(threadIds.asList())

    override fun getConversationsSnapShot(): Flow<List<Conversation>> = TODO()
        //conversationDao.getConversationsSnapshot()

    override fun getTopConversations(): Flow<List<Conversation>> = TODO()
        //conversationDao.getTopConversations()

    override suspend fun setConversationName(id: Long, name: String) {
        conversationDao.getConversation(id).firstOrNull().let { conversation ->
            conversation?.copy(title = name)?.let {
                conversationDao.updateConversation(it)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun searchConversations(query: CharSequence): Flow<List<SearchResult>> = TODO()


    override fun getBlockedConversations(): Flow<List<Conversation>> =
        conversationDao.getBlockedConversations()

    override fun getConversation(threadId: Long): Flow<Conversation?> =
        conversationDao.getConversation(threadId)

    override fun getUnmanagedConversations(): Flow<List<Conversation>> = TODO()

    override fun getRecipients(): Flow<List<Recipient>> =
        recipientDao.getRecipients()

    override fun getUnmanagedRecipients(): Flow<List<Recipient>> = TODO()

    override fun getRecipient(recipientId: Long): Flow<Recipient?> =
        recipientDao.getRecipient(recipientId)

    override fun getThreadId(recipient: String): Flow<Long?> = getThreadId(listOf(recipient))

    override fun getThreadId(recipients: Collection<String>): Flow<Long?> = TODO()

    @OptIn(ExperimentalSerializationApi::class)
    override fun getOrCreateConversation(addresses: List<String>): Flow<Result<Conversation>> = flow {
        //check if conversation already exists
        val recipients = recipientDao.getRecipientsByAddress(addresses)
            .first()
            .map { it.inboxId }


        // recipients not found
        if (recipients.size != addresses.size) {
            val identities = addresses.map {
                PublicIdentity(
                    kind = IdentityKind.ETHEREUM,
                    identifier = it
                )
            }

           // check if you user is allowed to message
            val consentMap = xmtpClientManager.client.canMessage(identities)
            if (!consentMap.values.all { it }) {
                emit(Result.Error("Could not create a conversation with " + consentMap.filter { !it.value }.toList().joinToString(",")))
            } else {
                //dm
                if (addresses.size == 1) {
                    try {
                        val dm = xmtpClientManager.client.conversations.findOrCreateDmWithIdentity(identities.first())

                        val conversation = Conversation(
                            id = dm.id,
                            title = null,
                            members = listOf(dm.peerInboxId)
                        )

                        val recipient = Recipient(
                            inboxId = dm.peerInboxId,
                            address = identities.first().identifier
                        )
                        recipientDao.insertRecipients(listOf(recipient))
                        conversationDao.insertConversation(conversation)
                        emit(Result.Success(conversation))
                    } catch (e: Exception) {
                        emit(Result.Error(e.message?: "could not create dm conversation"))
                    }
                } else { // group
                    //TODO: Handle groups
                }
            }
        } else {
            // recipients were found
            val json = Json.encodeToString(recipients)
            val conversation = conversationDao.getConversationByExactMembers(recipients.size, json)
            emit(Result.Success(conversation!!)) // TODO CHANGE
        }
    }

    override suspend fun saveDraft(threadId: Long, draft: String) {
        conversationDao.getConversation(threadId).firstOrNull().let { conversation ->
            conversation?.let {
                conversationDao.updateConversation(it.copy(draft = draft))
            }
        }
    }

    override suspend fun updateConversations(vararg threadIds: Long) {
        TODO()
    }

    override suspend fun markArchived(vararg threadIds: Long) {
        conversationDao.getConversations(threadIds.toList()).collect { conversations ->
            conversations.forEach { conversation ->
                conversationDao.updateConversation(
                    conversation.copy(
                        archived = true
                    )
                )
            }
        }
    }

    override suspend fun markRead(threadId: Long) {
        TODO()
    }

    override suspend fun markUnarchived(vararg threadIds: Long) {
        conversationDao.getConversations(threadIds.toList()).collect { conversations ->
            conversations.forEach { conversation ->
                conversationDao.updateConversation(
                    conversation.copy(
                        archived = false
                    )
                )
            }
        }
    }

    override suspend fun markPinned(vararg threadIds: Long) {
        conversationDao.getConversations(threadIds.toList()).collect { conversations ->
            conversations.forEach { conversation ->
                conversationDao.updateConversation(
                    conversation.copy(
                        pinned = true
                    )
                )
            }
        }
    }

    override suspend fun markUnpinned(vararg threadIds: Long) {
        conversationDao.getConversations(threadIds.toList()).collect { conversations ->
            conversations.forEach { conversation ->
                conversationDao.updateConversation(
                    conversation.copy(
                        pinned = false
                    )
                )
            }
        }
    }

    override suspend fun markBlocked(
        threadIds: List<Long>,
        blockingClient: Int,
        blockReason: String?
    ) {
        conversationDao.getConversations(threadIds).collect { conversations ->
            conversations.forEach { conversation ->
                conversationDao.updateConversation(
                    conversation.copy(
                        blocked = true,
                        blockingClient = blockingClient,
                        blockReason = blockReason
                    )
                )
            }
        }
    }

    override suspend fun markUnblocked(vararg threadIds: Long) {
        val conversations = conversationDao.getConversations(threadIds.toList()).firstOrNull()
        conversations?.forEach { conversation ->
            conversationDao.updateConversation(
                conversation.copy(
                    blocked = false,
                    blockingClient = null,
                    blockReason = null
                )
            )
        }
    }

    override suspend fun deleteConversations(vararg threadIds: Long) {
        TODO()
    }

    override suspend fun markAccepted(threadId: Long) {
        TODO()
    }

}

