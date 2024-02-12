package org.ethereumhpone.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.Telephony
import android.telephony.SmsManager
import android.webkit.MimeTypeMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.ethereumhpone.common.send_message.SmsManagerFactory
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.domain.manager.KeyManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.MessageRepository
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val context: Context,
    private val keyManager: KeyManager,
): MessageRepository {
    override fun getMessages(threadId: Long, query: String): Flow<List<Message>> =
        messageDao.getMessages(threadId, query)

    override fun getMessage(id: Long): Flow<Message?> =
        messageDao.getMessage(id)

    override fun getMessageForPart(id: Long): Flow<Message?> =
        messageDao.getMessageForPart(id)

    override fun getLastIncomingMessage(
        threadId: Long,
        smsInboxTypes: IntArray,
        mmsInboxTypes: IntArray
    ): Flow<Message> = messageDao.getLastIncomingMessage(threadId, smsInboxTypes, mmsInboxTypes)

    override fun getUnreadCount(): Flow<Long> =
        messageDao.getUnreadCount()

    override fun getPart(id: Long): Flow<MmsPart?> =
        messageDao.getPart(id)

    override fun getPartsForConversation(threadId: Long): Flow<List<MmsPart>> =
        messageDao.getPartsForConversation(threadId)

    override suspend fun savePart(id: Long): File? {
        val part = getPart(id).first() ?: return null
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(part.type) ?: return null
        val date = getMessageForPart(id).first()?.date
        val dir = File(
            Environment.getExternalStorageDirectory(),
            "Messaging/Media")
            .apply { mkdirs() }
        val fileName = part.name?.takeIf { name -> name.endsWith(extension) }
            ?: "${part.type.split("/").last()}_$date.$extension"
        var file: File
        var index = 0
        do {
            file = File(dir, if (index == 0) fileName else fileName.replace(".$extension", " ($index).$extension"))
            index++
        } while (file.exists())

        try {
            FileOutputStream(file).use { outputStream ->
                context.contentResolver.openInputStream(part.getUri())?.use { inputStream ->
                    inputStream.copyTo(outputStream, 1024)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        MediaScannerConnection.scanFile(context, arrayOf(file.path), null, null)

        return file.takeIf { it.exists() }
    }

    override fun getUnreadUnseenMessages(threadId: Long): Flow<List<Message>> =
        messageDao.getUnreadUnseenMessages()


    override suspend fun insertSms() {
        TODO("Not yet implemented")
    }

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
        conversationDao.getConversations(threadIds.toList()).map { conversations ->
            conversations
                .filter { it.lastMessage?.read == true }
                .forEach { conversation ->
                    conversationDao.updateConversation(
                        conversation.copy(
                            lastMessage = conversation.lastMessage?.copy(read = false)
                        )
                    )
                }
        }
    }

    override suspend fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>
    ) {

        val smsManager = subId.takeIf { it != -1 }
            ?.let { SmsManagerFactory.createSmsManager(context, subId) }
            ?: SmsManager.getDefault()





    }

    override suspend fun sendSms(message: Message) {
        TODO("Not yet implemented")
    }

    override suspend fun resendMms(message: Message) {
        TODO("Not yet implemented")
    }

    override suspend fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun insertReceivedSms(
        subId: Int,
        address: String,
        body: String,
        sentTime: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun markSending(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markSent(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markFailed(id: Long, resultCode: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun markDelivered(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun markDeliveryFailed(id: Long, resultCode: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(vararg messageIds: Long) {
        TODO("Not yet implemented")
    }

}