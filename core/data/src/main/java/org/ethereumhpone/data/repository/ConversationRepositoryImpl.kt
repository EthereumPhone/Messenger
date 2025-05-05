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

class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val conversationDao: ConversationDao,
    private val contactDao: ContactDao,
    private val recipientDao: RecipientDao,
    private val messageDao: MessageDao,
    private val xmtpClientManager: XmtpClientManager,
): ConversationRepository {
    override fun getConversations(): Flow<List<Conversation>> =
        conversationDao.getConversations()
            .map { it.map(CompositeConversation::toExternalModel) }

    override fun getConversation(conversationId: String): Flow<Conversation?> =
        conversationDao.getConversation(conversationId)
            .map { it?.toExternalModel() }

    override fun createConversation(addresses: List<String>): Flow<Result<Conversation>> = flow {
        val recipients = recipientDao.getRecipientsByAddress(addresses).first()
        val inboxIds = recipients.map { it.inboxId }

        val allRecipientsFound = inboxIds.size == addresses.size
        if (!allRecipientsFound) {
            val identities = addresses.map {
                PublicIdentity(kind = IdentityKind.ETHEREUM, identifier = it)
            }

            val consentMap = xmtpClientManager.client.canMessage(identities)
            val notAllowed = consentMap.filterValues { !it }

            if (notAllowed.isNotEmpty()) {
                val blocked = notAllowed.keys.joinToString(", ")
                emit(Result.Error("Could not create a conversation with: $blocked"))
                return@flow
            }

            if (addresses.size > 1) {
                emit(Result.Error("Group conversations are not yet supported"))
                return@flow
            }

            // Handle direct message creation
            try {
                val identity = identities.first()
                val dm = xmtpClientManager.client.conversations.findOrCreateDmWithIdentity(identity)

                val conversationEntity = ConversationEntity(
                    id = dm.id,
                    title = null,
                    members = listOf(dm.peerInboxId),
                    createdAt = dm.createdAt.time,
                    clientInbox = xmtpClientManager.client.inboxId
                )

                val recipientEntity = RecipientEntity(
                    inboxId = dm.peerInboxId,
                    address = identity.identifier
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
        val dm = xmtpClientManager.client.conversations.findOrCreateDm(inboxId)
        val conversation = conversationDao.getConversation(dm.id).first()

        if (conversation != null) {
            emit(Result.Success(conversation.toExternalModel()))
        } else {
            val newConversation = ConversationEntity(
                id = dm.id,
                title = null,
                members = listOf(dm.peerInboxId),
                createdAt = dm.createdAt.time,
                clientInbox = xmtpClientManager.client.inboxId
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



