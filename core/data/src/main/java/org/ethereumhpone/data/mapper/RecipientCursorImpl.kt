package org.ethereumhpone.data.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.mapper.RecipientCursor
import javax.inject.Inject

class RecipientCursorImpl @Inject constructor(
   private val context: Context,
    private val permissionManager: PermissionManager
): RecipientCursor {
    companion object {
        val URI = Uri.parse("content://mms-sms/canonical-addresses")

        const val COLUMN_ID = 0
        const val COLUMN_ADDRESS = 1
    }

    override fun getRecipientCursor(): Cursor? {
        return when (permissionManager.hasReadSms()) {
            true -> context.contentResolver.query(URI, null, null, null, null)
            false -> null
        }
    }

    override fun getRecipientCursor(id: Long): Cursor? {
        return context.contentResolver.query(URI, null, "_id = ?", arrayOf(id.toString()), null)

    }

    override fun map(from: Cursor) = Recipient(
        id = from.getLong(COLUMN_ID),
        address = from.getString(COLUMN_ADDRESS),
        lastUpdate = System.currentTimeMillis()
    )
}