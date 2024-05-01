package org.ethereumhpone.data.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.mapper.ConversationCursor
import javax.inject.Inject

class ConversationCursorImpl @Inject constructor(
    private val context: Context,
    private val permissionManager: PermissionManager
): ConversationCursor {

    companion object {
        val URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
        val PROJECTION = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.RECIPIENT_IDS
        )

        const val ID = 0
        const val RECIPIENT_IDS = 1
    }
    override fun getConversationsCursor(): Cursor? {
        return when (permissionManager.hasReadSms()) {
            true -> context.contentResolver.query(URI, PROJECTION, null, null, "date desc")
            false -> null
        }
    }

    override fun map(from: Cursor): Conversation {
        // Primary Conversation fields
        val id = from.getLong(from.getColumnIndexOrThrow("id"))
        val archived = from.getInt(from.getColumnIndexOrThrow("archived")) > 0
        val blocked = from.getInt(from.getColumnIndexOrThrow("blocked")) > 0
        val pinned = from.getInt(from.getColumnIndexOrThrow("pinned")) > 0
        val draft = from.getString(from.getColumnIndexOrThrow("draft"))
        val blockingClient = from.getInt(from.getColumnIndexOrThrow("blockingClient")).takeIf { it != 0 }
        val blockReason = from.getString(from.getColumnIndexOrThrow("blockReason"))
        val title = from.getString(from.getColumnIndexOrThrow("title"))

        // Recipient processing
        val recipientIds = from.getString(from.getColumnIndexOrThrow("recipient_ids"))
            .split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.toLongOrNull() }
        val recipients = recipientIds.map { Recipient(id = it) }

        // Message processing
        val lastMessage = getMessageFromCursor(from)

        return Conversation(
            id = id,
            archived = archived,
            blocked = blocked,
            pinned = pinned,
            recipients = recipients,
            lastMessage = lastMessage,
            draft = draft,
            blockingClient = blockingClient,
            blockReason = blockReason,
            title = title
        )
    }

    private fun getMessageFromCursor(cursor: Cursor): Message? {
        // Check if the cursor has message related data; adjust depending on your database schema
        // Here we assume that you have column names that match those in the Message data class
        try {
            val messageId = cursor.getLong(cursor.getColumnIndexOrThrow("message_id"))
            val threadId = cursor.getLong(cursor.getColumnIndexOrThrow("threadId"))
            val contentId = cursor.getLong(cursor.getColumnIndexOrThrow("contentId"))
            val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val boxId = cursor.getInt(cursor.getColumnIndexOrThrow("boxId"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            val date = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
            val dateSent = cursor.getLong(cursor.getColumnIndexOrThrow("dateSent"))
            val seen = cursor.getInt(cursor.getColumnIndexOrThrow("seen")) > 0
            val read = cursor.getInt(cursor.getColumnIndexOrThrow("read")) > 0
            val locked = cursor.getInt(cursor.getColumnIndexOrThrow("locked")) > 0
            val subId = cursor.getInt(cursor.getColumnIndexOrThrow("subId"))
            val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
            val errorCode = cursor.getInt(cursor.getColumnIndexOrThrow("errorCode"))
            val deliveryStatus = cursor.getInt(cursor.getColumnIndexOrThrow("deliveryStatus"))

            // Assuming defaults for MMS fields as these might not be present in every cursor row
            // You may need to handle these conditionally based on message type (SMS/MMS)
            val attachmentTypeString = Message.AttachmentType.NOT_LOADED.toString()
            val attachmentType = Message.AttachmentType.NOT_LOADED

            // Create and return the Message object
            return Message(
                id = messageId,
                threadId = threadId,
                contentId = contentId,
                address = address,
                boxId = boxId,
                type = type,
                date = date,
                dateSent = dateSent,
                seen = seen,
                read = read,
                locked = locked,
                subId = subId,
                body = body,
                errorCode = errorCode,
                deliveryStatus = deliveryStatus,
                attachmentTypeString = attachmentTypeString,
                attachmentType = attachmentType
            )
        } catch (e: Exception) {
            // Log or handle the absence of message data appropriately
            return null
        }
    }
}