package org.ethereumhpone.data.repository

import android.content.Context
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

        // Check if recipients already exist in the database
        val recipients = recipientDao.getRecipientsByAddress(addresses).first()
        val inboxIds = recipients.map { it.inboxId }

        val allRecipientsFound = inboxIds.size == addresses.size

        // missing recipients path
        if (!allRecipientsFound) {
            //build identities and check permissions
            val identities = addresses.map {
                PublicIdentity(
                    kind = IdentityKind.ETHEREUM,
                    identifier = it
                )
            }
            val consentMap = xmtpClientManager.client.canMessage(identities)
            val notAllowed = consentMap.filterValues { !it }

            if (notAllowed.isNotEmpty()) {
                val blocked = notAllowed.keys.joinToString(", ")
                emit(Result.Error("Could not create a conversation with: $blocked"))
                return@flow
            }

            // handle dms
            if(addresses.size == 1) {
                try {
                    val dm = xmtpClientManager.client.conversations.findOrCreateDmWithIdentity(identities.first())

                    //TODO: might need to add client address too
                    val conversationEntity = ConversationEntity(
                        id = dm.id,
                        title = null,
                        members = listOf(dm.peerInboxId),
                        createdAt = dm.createdAt.time,
                        clientInbox = xmtpClientManager.client.inboxId
                    )

                    val recipientEntity = RecipientEntity(
                        inboxId = dm.peerInboxId,
                        address = identities.first().identifier
                    )

                    // insert recipients & conversation
                    recipientDao.insertRecipients(listOf(recipientEntity))
                    conversationDao.insertConversation(conversationEntity)

                    // refs
                    val refs = ConversationRecipientCrossRef(dm.id, dm.peerInboxId)
                    conversationDao.insertConversationMemberCrossRefs(listOf(refs))

                } catch (e: Exception) {
                    emit(Result.Error(e.message?: "could not create dm conversation"))
                    return@flow
                }
            } else { // handle groups
                emit(Result.Error("Group conversations are not yet supported"))
                return@flow
            }
        }

        val existingConversation = conversationDao
            .getCompositeConversationByExactMembers(inboxIds)
            .first()
            ?.toExternalModel()

        if (existingConversation != null) {
            emit(Result.Success(existingConversation))
            return@flow
        }

        // fallback if conversation is not found
        if (inboxIds.size == 1) {
            val dm = xmtpClientManager.client.conversations.findOrCreateDm(inboxIds.first())

            val conversation = conversationDao.getConversation(dm.id).first()

            if (conversation == null) {
                val conversationEntity = ConversationEntity(
                    id = dm.id,
                    title = null,
                    members = listOf(dm.peerInboxId),
                    createdAt = dm.createdAt.time,
                    clientInbox = xmtpClientManager.client.inboxId
                )
                conversationDao.insertConversation(conversationEntity)
                emitAll(conversationDao.getConversation(dm.id).map { Result.Success(it!!.toExternalModel()) })
            } else {
                emit(Result.Success(conversation.toExternalModel()))
                //emit(conversation.map { Result.Success(it.toExternalModel()) })
            }


        } else {
            emit(Result.Error("Group conversations are not yet supported"))
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



