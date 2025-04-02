package org.ethereumhpone.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.contentValuesOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.common.send_message.SmsManagerFactory
import org.ethereumhpone.common.util.removeAccents
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.model.XMTPConversationHandler
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.repository.SyncRepository
import org.xmtp.android.library.XMTPException
import org.xmtp.android.library.libxmtp.DecodedMessage
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
    override fun getMessages(threadId: Long): Flow<List<Message>> =
        messageDao.getMessages(threadId)

    override fun getMessage(id: String): Flow<Message?> =
        flowOf(messageDao.getMessage(id))


    override fun getUnreadCount(): Flow<Long> =
        TODO()


    override suspend fun canMessage(addresses: List<String>) {

    }

    override suspend fun getUnreadUnseenMessages(threadId: Long): List<Message> =
        messageDao.getUnreadUnseenMessages()

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

    override suspend fun markSeen(threadId: Long) {
        messageDao.getMessages(threadId).map { messages ->
            messageDao.updateMessages(
                messages
                    .filter { !it.seen }
                    .map { it.copy(
                        seen = true
                    ) }
            )
        }
    }

    override suspend fun markRead(vararg threadIds: Long) {
        threadIds.forEach { threadId ->
            messageDao.getMessages(threadId).map { messages ->
                messageDao.updateMessages(
                    messages.filter { it.read && !it.seen }
                        .map { it.copy(
                            seen = true,
                            read = true
                        ) }
                )
            }
        }
        val values =  ContentValues()
        values.put(Telephony.Sms.SEEN, true)
        values.put(Telephony.Sms.READ, true)

        threadIds.forEach { threadId ->
            try {
                val uri = ContentUris.withAppendedId(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId)
                context.contentResolver.update(uri, values, "${Telephony.Sms.READ} = 0", null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun markUnread(vararg threadIds: Long) {
        TODO()
    }

    private suspend fun sendXmtpMessage(message: Message): String? {
        TODO()
    }

    override suspend fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>,
    ) {
        TODO()
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