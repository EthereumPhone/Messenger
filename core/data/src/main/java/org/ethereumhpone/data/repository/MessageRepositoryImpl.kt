package org.ethereumhpone.data.repository


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log as AndroidLog
import androidx.media3.common.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.relation.toExternalMessage
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.model.Message
import org.ethereumphone.model.Reaction
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
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun getMessages(threadId: String): Flow<List<Message>> =
        messageDao.getMessages(threadId)
            .map { message -> message.map { it.toExternalMessage() } }

    override fun getMessage(id: String): Flow<Message?> =
        messageDao.getCompositeMessage(id)
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

    @SuppressLint("UnsafeOptInUsageError")
    override suspend fun sendMessage(
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment>,
        reaction: Reaction?
    ): String? {
        val conversation = xmtpClientManager.client.conversations.findConversation(threadId)
        if (conversation == null) {
            Log.e("MessageRepository", "Conversation not found for threadId: $threadId")
            return null
        }

        when {
            attachments.isNotEmpty() -> {
                // TODO: Handle attachments
                Log.w("MessageRepository", "Attachments not yet supported")
                return null
            }

            reaction != null -> {
                // TODO: Handle reaction
                Log.w("MessageRepository", "Reactions not yet supported")
                return null
            }

            else -> {
                return try {
                    val messageId = if (replyReference != null) {
                        conversation.prepareMessage(
                            Reply(
                                reference = replyReference,
                                content = body.orEmpty(),
                                contentType = ContentTypeText
                            )
                        )
                    } else {
                        conversation.prepareMessage(body)
                    }

                    val messageEntity = MessageEntity(
                        id = messageId,
                        threadId = threadId,
                        dateSent = System.currentTimeMillis(),
                        date = System.currentTimeMillis(),
                        senderInboxId = xmtpClientManager.client.inboxId,
                        body = body.orEmpty(),
                        deliveryStatus = DecodedMessage.MessageDeliveryStatus.UNPUBLISHED,
                        isMe = true,
                        replyReference = replyReference,
                        seen = true,
                        read = true
                    )

                    // Save the message to database first for optimistic UI
                    messageDao.upsertMessages(listOf(messageEntity))

                    // Then publish it in a background job, re-fetching the conversation
                    // to ensure thread safety with the XMTP SDK.
                    scope.launch {
                        try {
                            val conversationForPublish = xmtpClientManager.client.conversations.findConversation(threadId)
                            conversationForPublish?.publishMessages()
                            println("Done!")
                            // Optionally, update the message status in the DB to PUBLISHED here
                        } catch (e: Exception) {
                            Log.e("MessageRepository", "Failed to publish message", e)
                            // Optionally, update the message status in the DB to FAILED here
                        }
                    }

                    messageId
                } catch (e: Exception) {
                    Log.e("MessageRepository", "Failed to prepare message", e)
                    null
                }
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