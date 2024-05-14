package org.ethereumhpone.data.repository

import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.Telephony
import android.telephony.SmsManager
import android.webkit.MimeTypeMap
import androidx.core.content.contentValuesOf
import com.google.android.mms.ContentType
import com.google.android.mms.MMSPart
import com.google.android.mms.pdu_alt.MultimediaMessagePdu
import com.google.android.mms.pdu_alt.PduPersister
import com.klinker.android.send_message.StripAccents
import com.klinker.android.send_message.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.common.send_message.SmsManagerFactory
import org.ethereumhpone.common.util.ImageUtils
import org.ethereumhpone.common.util.removeAccents
import org.ethereumhpone.common.util.tryOrNull
import org.ethereumhpone.data.receiver.SmsDeliveredReceiver
import org.ethereumhpone.data.receiver.SmsSentReceiver
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.repository.SyncRepository
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.sqrt

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val messengerPreferences: MessengerPreferences,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val syncRepository: SyncRepository,
    private val activeConversationManager: ActiveConversationManager,
    private val context: Context,
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
        messengerPreferences.prefs.firstOrNull()?.let { prefs ->
            val signedBody = when {
                prefs.signature.isEmpty() -> body
                body.isNotBlank() -> body + '\n' + prefs.signature
                else -> prefs.signature
            }

            val smsManager = subId.takeIf { it != -1 }
                ?.let { SmsManagerFactory.createSmsManager(context, subId) }
                ?: SmsManager.getDefault()

            val strippedBody = when(prefs.unicode) {
                    true -> removeAccents(signedBody)
                    false -> signedBody
            }

            val parts = smsManager.divideMessage(strippedBody).orEmpty()
            val forceMms = prefs.longAsMms && parts.size > 1
            if (addresses.size == 1 && attachments.isEmpty() && !forceMms) { // S
                try {
                    val message = insertSentSms(subId, threadId, addresses.first(), strippedBody, System.currentTimeMillis())
                    sendSms(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else { // MMS
                val parts = arrayListOf<MMSPart>()
                val maxWidth = smsManager.carrierConfigValues.getInt(SmsManager.MMS_CONFIG_MAX_IMAGE_WIDTH)
                    .takeIf { prefs.mmsSize == -1 } ?: Int.MAX_VALUE
                val maxHeight = smsManager.carrierConfigValues.getInt(SmsManager.MMS_CONFIG_MAX_IMAGE_HEIGHT)
                    .takeIf { prefs.mmsSize == -1 } ?: Int.MAX_VALUE

                var remainingBytes = when (prefs.mmsSize) {
                    -1 -> smsManager.carrierConfigValues.getInt(SmsManager.MMS_CONFIG_MAX_MESSAGE_SIZE)
                    0 -> Int.MAX_VALUE
                    else -> prefs.mmsSize * 1024
                } * 0.9 // Ugly, but buys us a bit of wiggle room

                signedBody.takeIf { it.isNotEmpty() }?.toByteArray()?.let { bytes ->
                    remainingBytes -= bytes.size
                    parts += MMSPart("text", ContentType.TEXT_PLAIN, bytes)
                }

                // attach contacts
                parts += attachments
                    .mapNotNull { attachment -> attachment as? Attachment.Contact }
                    .map { attachment -> attachment.vCard.toByteArray() }
                    .map { vCard ->
                        remainingBytes -= vCard.size
                        MMSPart("contact", ContentType.TEXT_VCARD, vCard)
                    }

                val imageBytesByAttachment = attachments
                    .mapNotNull { attachment -> attachment as? Attachment.Image }
                    .associateWith { attachment ->
                        val uri = attachment.getUri() ?: return@associateWith byteArrayOf()
                        ImageUtils.getScaledDrawable(context, uri, maxWidth, maxHeight)
                    }
                    .toMutableMap()

                val imageByteCount =
                    imageBytesByAttachment.values.sumOf { byteArray -> byteArray.size }

                if (imageByteCount > remainingBytes) {
                    imageBytesByAttachment.forEach { (attachment, originalBytes) ->
                        val uri = attachment.getUri() ?: return@forEach
                        val maxBytes =
                            originalBytes.size / imageByteCount.toFloat() * remainingBytes

                        // Get the image dimensions
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(uri),
                            null,
                            options
                        )
                        val width = options.outWidth
                        val height = options.outHeight
                        val aspectRatio = width.toFloat() / height.toFloat()

                        var attempts = 0
                        var scaledBytes = originalBytes

                        while (scaledBytes.size > maxBytes) {
                            // Estimate how much we need to scale the image down by. If it's still too big, we'll need to
                            // try smaller and smaller values
                            val scale = maxBytes / originalBytes.size * (0.9 - attempts * 0.2)
                            if (scale <= 0) {
                                Timber.w("Failed to compress ${originalBytes.size / 1024}Kb to ${maxBytes.toInt() / 1024}Kb")
                                return@forEach
                            }

                            val newArea = scale * width * height
                            val newWidth = sqrt(newArea * aspectRatio).toInt()
                            val newHeight = (newWidth / aspectRatio).toInt()

                            attempts++
                            scaledBytes = ImageUtils.getScaledDrawable(context, uri, newWidth, newHeight, 80)
                        }
                        imageBytesByAttachment[attachment] = scaledBytes
                    }
                }
                imageBytesByAttachment.forEach { (attachment, bytes) ->
                    parts += when (attachment.isGif(context)) {
                        true -> MMSPart("image", ContentType.IMAGE_GIF, bytes)
                        false -> MMSPart("image", ContentType.IMAGE_JPEG, bytes)
                    }
                }

                val recipients = addresses.map(phoneNumberUtils::normalizeNumber)
                val transaction = Transaction(context)
                transaction.sendNewMessage(subId, threadId, recipients, parts,null, null)
            }
        }
    }


    override suspend fun sendSms(message: Message) {
        val smsManager = message.subId.takeIf { it != -1 }
            ?.let { SmsManagerFactory.createSmsManager(context, message.subId) }
            ?: SmsManager.getDefault()

        messengerPreferences.prefs.firstOrNull()?.let{ prefs ->
            val parts = smsManager
                .divideMessage(if (prefs.unicode) StripAccents.stripAccents(message.body) else message.body)
                ?: arrayListOf()

            val sentIntents = parts.map {
                val intent = Intent(context, SmsSentReceiver::class.java).putExtra("id", message.id)
                PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

            val deliveredIntents = parts.map {
                val intent = Intent(context, SmsDeliveredReceiver::class.java).putExtra("id", message.id)
                val pendingIntent = PendingIntent
                    .getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (prefs.delivery) pendingIntent else null
            }

            try {
                smsManager.sendMultipartTextMessage(
                    message.address,
                    null,
                    parts,
                    ArrayList(sentIntents),
                    ArrayList(deliveredIntents)
                )
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                markFailed(message.id, Telephony.MmsSms.ERR_TYPE_GENERIC)

            }
        }
    }

    override suspend fun resendMms(message: Message) {
        val subId = message.subId
        val threadId = message.threadId
        val pdu = tryOrNull {
            PduPersister.getPduPersister(context).load(message.getUri()) as MultimediaMessagePdu
        } ?: return

        val addresses = pdu.to.map { it.string }.filter { it.isNotBlank() }
        val parts = message.parts.mapNotNull { part ->
            val bytes = tryOrNull(false) {
                context.contentResolver.openInputStream(part.getUri())?.use { inputStream -> inputStream.readBytes() }
            } ?: return@mapNotNull null

            MMSPart(part.name.orEmpty(), part.type, bytes)
        }
        Transaction(context).sendNewMessage(subId, threadId, addresses, parts, message.subject, message.getUri())

    }

    override suspend fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ): Message {
        println("ETHOSDEBUG: insertSentSms $body")
        val message = Message(
            threadId = threadId,
            address = address,
            body = body,
            date = date,
            boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
            type = "sms",
            read = true,
            seen = true
        )

        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to address,
            Telephony.Sms.BODY to body,
            Telephony.Sms.DATE to System.currentTimeMillis(),
            Telephony.Sms.READ to true,
            Telephony.Sms.SEEN to true,
            Telephony.Sms.TYPE to Telephony.Sms.MESSAGE_TYPE_OUTBOX,
            Telephony.Sms.THREAD_ID to threadId
        )

        println("Before prefs")

        val prefs = messengerPreferences.prefs.firstOrNull()  // This will suspend until the first value is emitted and then return
        prefs?.let {
            if (it.canUseSubId) {
                values.put(Telephony.Sms.SUBSCRIPTION_ID, message.subId)
            }
        }



        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
        uri?.lastPathSegment?.toLong()?.let { id ->
            messageDao.upsertMessage(
                message.copy(contentId = id)
            )
        }

        if (threadId == 0L) {
            uri?.let { syncRepository.syncMessage(it) }
        }

        return message
    }

    override suspend fun insertReceivedSms(
        subId: Int,
        address: String,
        body: String,
        sentTime: Long
    ): Message {
        val threadId = TelephonyCompat.getOrCreateThreadId(context, address)
        val message = Message(
            threadId = threadId,
            address = address,
            body = body,
            date = System.currentTimeMillis(),
            boxId = Telephony.Sms.MESSAGE_TYPE_INBOX,
            type = "sms",
            read = activeConversationManager.getActiveConversation() == threadId,
            seen = true
        )

        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to address,
            Telephony.Sms.BODY to body,
            Telephony.Sms.DATE_SENT to sentTime
        )

        val prefs = messengerPreferences.prefs.firstOrNull()  // This will suspend until the first value is emitted and then return
        prefs?.let {
            if (it.canUseSubId) {
                values.put(Telephony.Sms.SUBSCRIPTION_ID, message.subId)
            }
        }

        context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)?.lastPathSegment?.toLong()?.let { id ->
            messageDao.upsertMessage(
                message.copy(contentId = id)
            )
        }

        return message
    }

    override suspend fun markSending(id: Long) {
        messageDao.getMessage(id).collect { message ->
            message?.let {
                messageDao.upsertMessage(
                    it.copy(
                        boxId = when(it.isSms()) {
                            true -> Telephony.Sms.MESSAGE_TYPE_OUTBOX
                            false -> Telephony.Mms.MESSAGE_BOX_OUTBOX
                        }
                    )
                )
                val values = when (message.isSms()) {
                    true -> contentValuesOf(Telephony.Sms.TYPE to Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                    false -> contentValuesOf(Telephony.Mms.MESSAGE_BOX to Telephony.Mms.MESSAGE_BOX_OUTBOX)
                }
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }

    }

    override suspend fun markSent(id: Long) {
        messageDao.getMessage(id).collect { message ->
            message?.let {
                messageDao.upsertMessage(
                    it.copy(
                        boxId = Telephony.Sms.MESSAGE_TYPE_SENT
                    )
                )
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)

                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override suspend fun markFailed(id: Long, resultCode: Int) {
        messageDao.getMessage(id).collect { message ->
            message?.let {
                messageDao.upsertMessage(
                    it.copy(
                        boxId = Telephony.Sms.MESSAGE_TYPE_FAILED,
                        errorCode = resultCode
                    )
                )
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
                values.put(Telephony.Sms.ERROR_CODE, resultCode)

                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override suspend fun markDelivered(id: Long) {
        messageDao.getMessage(id).collect { message ->
            message?.let {
                messageDao.upsertMessage(
                    it.copy(
                        boxId = Telephony.Sms.STATUS_COMPLETE,
                    )
                )
                val values = ContentValues()
                values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE)
                values.put(Telephony.Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Telephony.Sms.READ, true)

                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override suspend fun markDeliveryFailed(id: Long, resultCode: Int) {
        messageDao.getMessage(id).collect { message ->
            message?.let {
                messageDao.upsertMessage(
                    it.copy(
                        deliveryStatus = Telephony.Sms.STATUS_FAILED,
                        dateSent = System.currentTimeMillis(),
                        read = true,
                        errorCode = resultCode
                    )
                )
                val values = ContentValues()
                values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_FAILED)
                values.put(Telephony.Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Telephony.Sms.READ, true)
                values.put(Telephony.Sms.ERROR_CODE, resultCode)

                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override suspend fun deleteMessage(vararg messageIds: Long) {
        messageIds.forEach { id ->
            messageDao.getMessage(id).collect { message ->
                message?.let {
                    val uri = message.getUri()
                    messageDao.deleteMessage(message)
                    context.contentResolver.delete(uri, null, null)
                }
            }
        }
    }

}