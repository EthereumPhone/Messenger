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
import kotlinx.coroutines.withContext
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.common.extensions.map
import org.ethereumhpone.common.extensions.removeAccents
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
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.PublicIdentity
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val conversationDao: ConversationDao,
    private val contactDao: ContactDao,
    private val recipientDao: RecipientDao,
    private val messageDao: MessageDao,
    private val xmtpClientManager: XmtpClientManager
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

    override fun getOrCreateConversation(addresses: List<String>): Flow<Conversation> = flow {
        //check if conversation already exists
        val recipients = recipientDao.getRecipientsByAddress(addresses)










        // Assume direct
        val publicIdentities = addresses.map {
            PublicIdentity(
                kind = IdentityKind.ETHEREUM,
                identifier = it
            )
        }

        val inboxIds = publicIdentities.map { xmtpClientManager.client.inboxIdFromIdentity(it) }

        xmtpClientManager.client.inboxIdFromIdentity()

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

