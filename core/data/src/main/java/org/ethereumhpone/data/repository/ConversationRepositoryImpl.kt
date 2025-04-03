package org.ethereumhpone.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumphone.model.Conversation
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val conversationDao: ConversationDao,
    private val contactDao: ContactDao,
    private val recipientDao: RecipientDao,
    private val messageDao: MessageDao,
    private val xmtpClientManager: XmtpClientManager,
): ConversationRepository {
    override fun getConversations(): Flow<List<Conversation>> {
        conversationDao.getConversations()
            .map {  }
    }

    override fun getConversation(conversationId: String): Flow<org.xmtp.android.library.Conversation> {
        TODO("Not yet implemented")
    }
}


/*


@OptIn(ExperimentalSerializationApi::class)
    override fun getOrCreateConversation(addresses: List<String>): Flow<Result<ConversationEntity>> = flow {
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

                        val conversationEntity = ConversationEntity(
                            id = dm.id,
                            title = null,
                            members = listOf(dm.peerInboxId)
                        )

                        val recipientEntity = RecipientEntity(
                            inboxId = dm.peerInboxId,
                            address = identities.first().identifier
                        )
                        recipientDao.insertRecipients(listOf(recipientEntity))
                        conversationDao.insertConversation(conversationEntity)
                        emit(Result.Success(conversationEntity))
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
 */
