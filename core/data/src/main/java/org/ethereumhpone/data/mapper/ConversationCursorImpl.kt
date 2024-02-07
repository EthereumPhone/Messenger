package org.ethereumhpone.data.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import org.ethereumhpone.database.model.Conversation
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
        val recipientIds = from.getString(RECIPIENT_IDS)
            .split(" ")
            .filter { it.isNotBlank() }
            .map { recipientId -> recipientId.toLong() }

        val recipientList = recipientIds.map { recipientId -> Recipient(id = recipientId) }

        return Conversation(
            id = from.getLong(ID),
            recipients = recipientList
        )
    }
}