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
import org.ethereumhpone.database.dao.ReactionDao
import org.ethereumhpone.database.model.ReactionEntity
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.relation.toExternalMessage
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.model.Message
import org.ethereumphone.model.Reaction
import org.ethereumphone.model.TransactionRequest
import org.ethereumphone.model.TransactionReference
import org.xmtp.android.library.Conversation
import org.xmtp.android.library.SendOptions
import org.xmtp.android.library.codecs.ContentTypeText
import org.xmtp.android.library.codecs.Reaction as XmtpReaction
import org.xmtp.android.library.codecs.ReactionAction
import org.xmtp.android.library.codecs.ReactionCodec
import org.xmtp.android.library.codecs.ReactionSchema
import org.xmtp.android.library.codecs.Reply
import org.xmtp.android.library.libxmtp.DecodedMessage
import org.ethereumhpone.data.codec.ContentTypeTransactionRequest
import org.ethereumhpone.data.codec.ContentTypeTransactionReference
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import android.content.Intent
import org.ethereumhpone.data.services.IdentityCallbackRegistry
import org.ethereumhpone.data.services.ThirdPartyIdentityService
import org.xmtp.android.library.libxmtp.IdentityKind

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val reactionDao: ReactionDao,
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
        messageDao.markAllRead(threadId)
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
    ): String? = coroutineScope {
        // Wait until the XMTP client is ready
        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }

        val conversation = xmtpClientManager.client.conversations.findConversation(threadId)
            ?: return@coroutineScope null

        when {
            attachments.isNotEmpty() -> {
                // TODO: Handle attachments
                null
            }

            reaction != null -> {
                // TODO: Handle reaction
                null
            }

            else -> {
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

                Log.d("MESSAGE ID", messageId)

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

                launch { messageDao.upsertMessages(listOf(messageEntity)) }
                launch {
                    conversation.publishMessages()
                    notifyThirdPartyRecipients(conversation, body.orEmpty())
                }

                messageId
            }
        }
    }

    override suspend fun sendMessageWithConversation(
        xmtpConversation: Conversation,
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment>,
        reaction: Reaction?
    ): String? = coroutineScope {
        // Wait until the XMTP client is ready
        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }

        when {
            attachments.isNotEmpty() -> {
                // TODO: Handle attachments
                null
            }

            reaction != null -> {
                // TODO: Handle reaction
                null
            }

            else -> {
                val messageId = try {
                    if (replyReference != null) {
                        xmtpConversation.prepareMessage(
                            Reply(
                                reference = replyReference,
                                content = body.orEmpty(),
                                contentType = ContentTypeText
                            )
                        )
                    } else {
                        xmtpConversation.prepareMessage(body)
                    }
                } catch (e: Exception) {
                    AndroidLog.e("MessageRepository", "Failed to prepare message", e)
                    return@coroutineScope null
                }

                Log.d("MESSAGE ID", messageId)

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

                launch { messageDao.upsertMessages(listOf(messageEntity)) }
                launch {
                    xmtpConversation.publishMessages()
                    notifyThirdPartyRecipients(xmtpConversation, body.orEmpty())
                }

                messageId
            }
        }
    }

    /**
     * After a message is published, checks if any conversation member is a
     * registered third-party isolated identity and immediately notifies that
     * app via the callback registry and an explicit broadcast.
     *
     * @param messageBody the text of the message that was just sent
     */
    private suspend fun notifyThirdPartyRecipients(conversation: Conversation, messageBody: String) {
        try {
            val members = conversation.members()

            // Resolve the sender's (user's) ETH address
            val myInboxId = xmtpClientManager.client.inboxId
            val senderAddress = members
                .firstOrNull { it.inboxId == myInboxId }
                ?.identities
                ?.firstOrNull { it.kind == IdentityKind.ETHEREUM }
                ?.identifier

            for (member in members) {
                val address = member.identities
                    .firstOrNull { it.kind == IdentityKind.ETHEREUM }
                    ?.identifier ?: continue

                val callerKey = ThirdPartyIdentityService.findCallerKeyByAddress(context, address)
                    ?: continue

                AndroidLog.d("MessageRepository", "Notifying third-party $callerKey of new message")
                IdentityCallbackRegistry.notifyNewMessages(callerKey, 1)

                val packageName = callerKey.substringBeforeLast('_')
                val relayIntent = Intent("org.ethereumhpone.messenger.action.RELAY_TO_THIRD_PARTY").apply {
                    putExtra("target_package", packageName)
                    putExtra("sender_address", senderAddress ?: "")
                    putExtra("message_text", messageBody)
                }
                context.sendBroadcast(relayIntent)
            }
        } catch (e: Exception) {
            AndroidLog.w("MessageRepository", "Failed to notify third-party recipients", e)
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
    
    private val jsonSerializer = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    override suspend fun sendTransactionRequest(
        xmtpConversation: Conversation,
        threadId: String,
        transactionRequest: TransactionRequest
    ): String? = coroutineScope {
        // Wait until the XMTP client is ready
        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
        
        try {
            // Prepare the transaction request message
            val messageId = xmtpConversation.prepareMessage(
                content = transactionRequest,
                options = SendOptions(contentType = ContentTypeTransactionRequest)
            )
            
            Log.d("TRANSACTION REQUEST MESSAGE ID", messageId)
            
            // Build fallback body for display
            val fallbackBody = buildTransactionRequestBody(transactionRequest)
            val txRequestJson = jsonSerializer.encodeToString(transactionRequest)
            
            val messageEntity = MessageEntity(
                id = messageId,
                threadId = threadId,
                dateSent = System.currentTimeMillis(),
                date = System.currentTimeMillis(),
                senderInboxId = xmtpClientManager.client.inboxId,
                body = fallbackBody,
                deliveryStatus = DecodedMessage.MessageDeliveryStatus.UNPUBLISHED,
                isMe = true,
                replyReference = null,
                seen = true,
                read = true,
                transactionRequest = txRequestJson,
                transactionStatus = "PENDING"
            )
            
            launch { messageDao.upsertMessages(listOf(messageEntity)) }
            launch { xmtpConversation.publishMessages() }
            
            messageId
        } catch (e: Exception) {
            AndroidLog.e("MessageRepository", "Failed to send transaction request", e)
            null
        }
    }
    
    override suspend fun updateTransactionStatus(
        messageId: String,
        status: String,
        txHash: String?
    ) {
        messageDao.updateTransactionStatus(messageId, status, txHash)
    }
    
    private fun buildTransactionRequestBody(txRequest: TransactionRequest): String {
        val chainName = chainIdToName(txRequest.chainId)
        val metadata = txRequest.metadata
        
        return if (metadata?.tokenAmount != null && metadata.tokenSymbol != null) {
            "Transaction Request: ${metadata.tokenAmount} ${metadata.tokenSymbol} on $chainName"
        } else {
            "Transaction Request: ${txRequest.calls.size} call(s) on $chainName"
        }
    }
    
    private fun chainIdToName(chainId: Long): String = when (chainId) {
        1L -> "Ethereum"
        10L -> "Optimism"
        137L -> "Polygon"
        42161L -> "Arbitrum"
        8453L -> "Base"
        11155111L -> "Sepolia"
        else -> "Chain $chainId"
    }
    
    override suspend fun sendTransactionReference(
        xmtpConversation: Conversation,
        threadId: String,
        transactionReference: TransactionReference
    ): String? = coroutineScope {
        // Wait until the XMTP client is ready
        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
        
        try {
            // Prepare the transaction reference message
            val messageId = xmtpConversation.prepareMessage(
                content = transactionReference,
                options = SendOptions(contentType = ContentTypeTransactionReference)
            )
            
            Log.d("TRANSACTION REFERENCE MESSAGE ID", messageId)
            
            // Build fallback body for display
            val fallbackBody = buildTransactionReferenceBody(transactionReference)
            val txReferenceJson = jsonSerializer.encodeToString(transactionReference)
            
            val messageEntity = MessageEntity(
                id = messageId,
                threadId = threadId,
                dateSent = System.currentTimeMillis(),
                date = System.currentTimeMillis(),
                senderInboxId = xmtpClientManager.client.inboxId,
                body = fallbackBody,
                deliveryStatus = DecodedMessage.MessageDeliveryStatus.UNPUBLISHED,
                isMe = true,
                replyReference = null,
                seen = true,
                read = true,
                transactionReference = txReferenceJson
            )
            
            launch { messageDao.upsertMessages(listOf(messageEntity)) }
            launch { xmtpConversation.publishMessages() }
            
            messageId
        } catch (e: Exception) {
            AndroidLog.e("MessageRepository", "Failed to send transaction reference", e)
            null
        }
    }
    
    private fun buildTransactionReferenceBody(txReference: TransactionReference): String {
        val chainName = chainIdToName(txReference.networkId)
        val metadata = txReference.metadata
        
        // Capture nullable values to enable smart casting
        val amount = metadata?.amount
        val decimals = metadata?.decimals
        val currency = metadata?.currency
        val transactionType = metadata?.transactionType
        
        return when {
            amount != null && currency != null && decimals != null -> {
                val humanAmount = formatAmount(amount, decimals)
                val txType = transactionType?.capitalize() ?: "Transaction"
                "$txType: $humanAmount $currency on $chainName"
            }
            transactionType != null -> {
                "${transactionType.capitalize()} on $chainName"
            }
            else -> {
                "Transaction on $chainName: ${truncateHash(txReference.reference)}"
            }
        }
    }
    
    private fun formatAmount(amount: String, decimals: Int): String {
        return try {
            val divisor = Math.pow(10.0, decimals.toDouble())
            val result = amount.toDouble() / divisor
            if (result == result.toLong().toDouble()) {
                result.toLong().toString()
            } else {
                String.format("%.6f", result).trimEnd('0').trimEnd('.')
            }
        } catch (e: NumberFormatException) {
            amount // Return original string if parsing fails
        }
    }
    
    private fun truncateHash(hash: String): String {
        return if (hash.length > 12) {
            "${hash.take(6)}...${hash.takeLast(4)}"
        } else {
            hash
        }
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
    
    override suspend fun sendReaction(
        xmtpConversation: Conversation,
        messageId: String,
        emoji: String,
        action: ReactionAction
    ) {
        // Wait until the XMTP client is ready
        xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
        
        try {
            // Create the XMTP Reaction object
            val reaction = XmtpReaction(
                reference = messageId,
                action = action,
                content = emoji,
                schema = ReactionSchema.Unicode
            )
            
            // Send the reaction using XMTP codec
            // Use ReactionCodec's content type to ensure correct encoding
            xmtpConversation.send(
                content = reaction,
                options = SendOptions(contentType = ReactionCodec().contentType)
            )
            
            // Update local database
            val myInboxId = xmtpClientManager.client.inboxId
            val reactionId = "${myInboxId}_${messageId}_${emoji}"
            
            if (action == ReactionAction.Added) {
                val reactionEntity = ReactionEntity(
                    id = reactionId,
                    messageId = messageId,
                    inboxId = myInboxId,
                    content = emoji
                )
                reactionDao.upsertReaction(reactionEntity)
            } else {
                reactionDao.deleteReaction(reactionId)
            }
            
            AndroidLog.d("MessageRepository", "Reaction $action sent: $emoji to message $messageId")
        } catch (e: Exception) {
            AndroidLog.e("MessageRepository", "Failed to send reaction", e)
            throw e
        }
    }
    
    override suspend fun removeReaction(
        xmtpConversation: Conversation,
        messageId: String,
        emoji: String
    ) {
        sendReaction(xmtpConversation, messageId, emoji, ReactionAction.Removed)
    }

}