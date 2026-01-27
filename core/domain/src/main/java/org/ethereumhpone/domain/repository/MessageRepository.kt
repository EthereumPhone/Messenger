package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.domain.model.Attachment
import org.ethereumphone.model.Message
import org.ethereumphone.model.Reaction
import org.ethereumphone.model.TransactionRequest
import org.ethereumphone.model.TransactionReference
import org.xmtp.android.library.Conversation
import org.xmtp.android.library.codecs.ReactionAction

interface MessageRepository {

    fun getMessages(threadId: String): Flow<List<Message>>
    fun getMessage(id: String): Flow<Message?>
    fun getUnreadCount(): Flow<Long>
    suspend fun canMessage(addresses: List<String>)
    suspend fun getUnreadUnseenMessages(threadId: String): List<Message>
    suspend fun markAllSeen()
    suspend fun markSeen(threadId: String)
    suspend fun markRead(vararg threadIds: String)
    suspend fun markUnread(vararg threadIds: String)
    suspend fun sendMessage(
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment>,
        reaction: Reaction?
    ): String?
    suspend fun sendMessageWithConversation(
        xmtpConversation: Conversation,
        threadId: String,
        body: String?,
        replyReference: String?,
        attachments: List<Attachment>,
        reaction: Reaction?
    ): String?
    suspend fun sendReadReceipt(timestamp: Long)
    suspend fun markSending(id: String)
    suspend fun markSent(id: String)
    suspend fun markFailed(id: String, resultCode: Int)
    suspend fun markDelivered(id: String)
    suspend fun deleteMessage(vararg messageIds: String)
    
    /**
     * Send a transaction request via XMTP.
     */
    suspend fun sendTransactionRequest(
        xmtpConversation: Conversation,
        threadId: String,
        transactionRequest: TransactionRequest
    ): String?
    
    /**
     * Update the transaction status of a message.
     */
    suspend fun updateTransactionStatus(
        messageId: String,
        status: String,
        txHash: String?
    )
    
    /**
     * Send a transaction reference (completed transaction) via XMTP.
     * @param replyReference Optional message ID this transaction is paying (for payment confirmations)
     */
    suspend fun sendTransactionReference(
        xmtpConversation: Conversation,
        threadId: String,
        transactionReference: TransactionReference,
        replyReference: String? = null
    ): String?
    
    /**
     * Send a reaction to a message via XMTP.
     * @param xmtpConversation The conversation to send the reaction in
     * @param messageId The ID of the message being reacted to
     * @param emoji The emoji/reaction content
     * @param action Whether to add or remove the reaction
     */
    suspend fun sendReaction(
        xmtpConversation: Conversation,
        messageId: String,
        emoji: String,
        action: ReactionAction
    )
    
    /**
     * Remove a reaction from a message.
     */
    suspend fun removeReaction(
        xmtpConversation: Conversation,
        messageId: String,
        emoji: String
    )

}