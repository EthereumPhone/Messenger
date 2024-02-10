package org.ethereumhpone.data.repository

import android.content.Context
import com.moez.QKSMS.filter.ConversationFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.common.extensions.map
import org.ethereumhpone.common.extensions.removeAccents
import org.ethereumhpone.common.util.tryOrNull
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.mapper.ConversationCursor
import org.ethereumhpone.domain.mapper.RecipientCursor
import org.ethereumhpone.domain.model.SearchResult
import org.ethereumhpone.domain.repository.ConversationRepository
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val conversationFilter: ConversationFilter,
    private val conversationDao: ConversationDao,
    private val recipientDao: RecipientDao,
    private val messageDao: MessageDao,
    private val conversationCursor: ConversationCursor,
    private val recipientCursor: RecipientCursor,
    private val phoneNumberUtils: PhoneNumberUtils
): ConversationRepository {
    override fun getConversations(archived: Boolean): Flow<List<Conversation>> =
        conversationDao.getConversations(archived)

    override fun getConversations(vararg threadIds: Long): Flow<List<Conversation>> =
        conversationDao.getConversations(threadIds.asList())

    override fun getConversationsSnapShot(): Flow<List<Conversation>> =
        conversationDao.getConversationsSnapshot()

    override fun getTopConversations(): Flow<List<Conversation>> =
        conversationDao.getTopConversations()

    override suspend fun setConversationName(id: Long, name: String) {
        conversationDao.getConversation(id).collect { conversation ->
            conversation?.copy(title = name)?.let {
                    conversationDao.updateConversation(it)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun searchConversations(query: CharSequence): Flow<List<SearchResult>> =
        conversationDao.getActiveConversations().flatMapConcat { conversations ->
            val normalizedQuery = query.removeAccents()
            messageDao.searchMessages(normalizedQuery)
                .map { messages ->
                    val messagesByConversation = messages
                        .asSequence()
                        .groupBy { message -> message.threadId }
                        .filter { (threadId, _) -> conversations.firstOrNull { it.id == threadId } != null }
                        .map { (threadId, messages) -> Pair(conversations.first { it.id == threadId }, messages.size) }
                        .map { (conversation, messages) -> SearchResult(normalizedQuery, conversation, messages) }
                        .sortedByDescending { result -> result.messages }
                        .toList()

                    conversations
                        .filter { conversation -> conversationFilter.filter(conversation, normalizedQuery) }
                        .map { conversation -> SearchResult(normalizedQuery, conversation, 0) } + messagesByConversation
                }
        }


    override fun getBlockedConversations(): Flow<List<Conversation>> =
        conversationDao.getBlockedConversations()

    override fun getConversation(threadId: Long): Flow<Conversation?> =
        conversationDao.getConversation(threadId)

    override fun getUnmanagedConversations(): Flow<List<Conversation>> =
        conversationDao.getUnmanagedConversations()

    override fun getRecipients(): Flow<List<Recipient>> =
        recipientDao.getRecipients()

    override fun getUnmanagedRecipients(): Flow<List<Recipient>> =
        recipientDao.getUnmanagedRecipients()

    override fun getRecipient(recipientId: Long): Flow<Recipient?> =
        recipientDao.getRecipient(recipientId)

    override fun getThreadId(recipient: String): Flow<Long?> = getThreadId(listOf(recipient))

    override fun getThreadId(recipients: Collection<String>): Flow<Long?> =
        conversationDao.getConversations(null).map {
            it.asSequence()
                .filter { conversation -> conversation.recipients.size == recipients.size }
                .find { conversation ->
                    conversation.recipients.map { it.address }.all { address ->
                        recipients.any { recipient -> phoneNumberUtils.compare(recipient, address) }
                    }
                }?.id
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getOrCreateConversation(threadId: Long): Flow<Conversation?> {
        return getConversation(threadId).flatMapLatest { conversation ->
            if (conversation != null) {
                flowOf(conversation)
            } else {
                getConversationFromCp(threadId)
            }
        }
    }

    override fun getOrCreateConversation(address: String): Flow<Conversation?> =
        getOrCreateConversation(listOf(address))

    // I want to throw up
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getOrCreateConversation(addresses: List<String>): Flow<Conversation?> {
        if(addresses.isEmpty()) {
            return flowOf(null)
        }

        return getThreadId(addresses).flatMapLatest { id ->
            (id ?: tryOrNull { TelephonyCompat.getOrCreateThreadId(context, addresses) })
                ?.takeIf { threadId -> threadId != 0L }
                ?.let { threadId ->
                    getConversation(threadId).flatMapLatest { conversation ->
                        if (conversation != null) {
                            flowOf(conversation)
                        } else {
                            getConversationFromCp(threadId)
                        }
                    }
                } ?: flowOf(null)
        }
    }

    override suspend fun saveDraft(threadId: Long, draft: String) {
        conversationDao.getConversation(threadId).collect { conversation ->
            conversation?.let {
                conversationDao.updateConversation(it.copy(draft = draft))
            }
        }
    }

    override suspend fun updateConversations(vararg threadIds: Long) =
        conversationDao.getConversations(threadIds.toList()).collect { conversations ->
            conversations.forEach { conversation ->
                messageDao.getLastConversationMessage(conversation.id).collect{ message ->
                    message?.let {
                        conversationDao.updateConversation(
                            conversation.copy(
                                lastMessage = it
                            )
                        )
                    }
                }
            }
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
        conversationDao.getConversations(threadIds.toList()).collect { conversations ->
            conversations.forEach { conversation ->
                conversationDao.updateConversation(
                    conversation.copy(
                        blocked = false,
                        blockingClient = null,
                        blockReason = null
                    )
                )
            }
        }
    }

    override suspend fun deleteConversations(vararg threadIds: Long) {
        conversationDao.getConversations(threadIds.toList()).collect { conversations ->
            conversations.forEach { conversation ->
                messageDao.getAllConversationMessages(conversation.id)
                    .collect(messageDao::deleteMessage)
                conversationDao.deleteConversation(listOf(conversation))
            }
        }
    }

    /**
     * Returns a [Conversation] from the system SMS ContentProvider, based on the [threadId]
     *
     * It should be noted that even if we have a valid [threadId], that does not guarantee that
     * we can return a [Conversation]. On some devices, the ContentProvider won't return the
     * conversation unless it contains at least 1 message
     */
    private fun getConversationFromCp(threadId: Long): Flow<Conversation?> {
        conversationCursor.getConversationsCursor()
            ?.map(ConversationCursor::map)
            ?.firstOrNull { it.id == threadId }



        return flow {  }
    }
}

