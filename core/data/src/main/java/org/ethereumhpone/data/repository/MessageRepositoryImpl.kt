package org.ethereumhpone.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.relation.CompositeMessage
import org.ethereumhpone.database.model.relation.toExternalMessage
import org.ethereumhpone.database.model.toExternalModel
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.model.Message
import org.ethereumphone.model.Reaction
import org.ethereumphone.model.Recipient
import org.xmtp.android.library.codecs.ContentTypeText
import org.xmtp.android.library.codecs.Reply
import org.xmtp.android.library.libxmtp.DecodedMessage
import java.time.Instant
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val messengerPreferences: MessengerPreferences,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val syncRepository: SyncRepository,
    private val activeConversationManager: ActiveConversationManager,
    private val context: Context,
    private val xmtpClientManager: XmtpClientManager,
): MessageRepository {
    override fun getMessages(threadId: String): Flow<List<Message>> =
        messageDao.getMessages(threadId)
            .map { message -> message.map { it.toExternalMessage() } }

    override fun getMessage(id: String): Flow<Message?> =
        messageDao.getMessage(id)
            .map { it?.toExternalMessage() }


    override fun getUnreadCount(): Flow<Long> =
        TODO()


    override suspend fun canMessage(addresses: List<String>) {
        TODO()
    }

    override suspend fun getUnreadUnseenMessages(threadId: String): List<Message> = TODO()
        //messageDao.getUnreadUnseenMessages()

    override suspend fun markAllSeen() {
        messageDao.getUnseenMessages().collect { messages ->
            messageDao.updateMessages(
                messages.map {
                    it.copy(
                        seen = true
                    )
                }
            )
        }
    }

    override suspend fun markSeen(threadId: String) {


        messageDao.getMessages(threadId).map { messages ->

        }
    }

    override suspend fun markRead(vararg threadIds: String) {
        threadIds.forEach { threadId ->
            messageDao.getMessages(threadId).map { messages ->

            }
        }
    }

    override suspend fun markUnread(vararg threadIds: String) {
        TODO()
    }

    override suspend fun sendMessage(
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment>,
        reaction: Reaction?
    ): String? = coroutineScope {
        //TODO: add error handling?
        val conversation = xmtpClientManager.client.conversations.findConversation(threadId) ?: return@coroutineScope null

        val templateMessage = MessageEntity(
            id = "",
            threadId = threadId,
            dateSent = Instant.now().epochSecond,
            senderInboxId = xmtpClientManager.client.inboxId,
            body = body ?: "",
            deliveryStatus = DecodedMessage.MessageDeliveryStatus.UNPUBLISHED,
            isMe = true,
            replyReference = ""
        )


        return@coroutineScope when {

            // attachments
            attachments.isNotEmpty() -> {


                null
            }


            reaction != null -> {


                null
            }


            // plain text
            else -> {
                val messageId = if (replyReference != null) {
                    val reply = Reply(
                        reference = replyReference,
                        content = body ?: "",
                        contentType = ContentTypeText
                    )
                    conversation.prepareMessage(reply)
                } else {
                    conversation.prepareMessage(body)
                }

                val messageEntity = MessageEntity(
                    id = messageId,
                    threadId = threadId,
                    dateSent = Instant.now().epochSecond,
                    senderInboxId = xmtpClientManager.client.inboxId,
                    body = body ?: "",
                    deliveryStatus = DecodedMessage.MessageDeliveryStatus.UNPUBLISHED,
                    isMe = true,
                    replyReference = replyReference ?: ""
                )

                launch { messageDao.insertMessage(messageEntity) }
                launch { conversation.publishMessages() }

                messageId
            }
        }
    }

    private suspend fun handleMessage() {

    }



    override suspend fun sendReadReceipt(timestamp: Long) {
        TODO("Not yet implemented")
    }


    override suspend fun markSending(id: String) {
        TODO()
    }

    override suspend fun markSent(id: String) {
        TODO()
    }

    override suspend fun markFailed(id: String, resultCode: Int) {
        TODO()
    }

    override suspend fun markDelivered(id: String) {
        TODO()
    }



    override suspend fun deleteMessage(vararg messageIds: String) {

    }

}