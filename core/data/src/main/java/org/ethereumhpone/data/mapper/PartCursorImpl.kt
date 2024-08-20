package org.ethereumhpone.data.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.domain.mapper.PartCursor
import javax.inject.Inject

class PartCursorImpl @Inject constructor(
    private val context: Context
): PartCursor {

    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://mms/part")
    }
    override fun getPartsCursor(messageId: Long?): Cursor? {
        return when (messageId) {
            null -> context.contentResolver.query(CONTENT_URI, null, null, null, null)
            else -> context.contentResolver.query(CONTENT_URI, null,
                "${Telephony.Mms.Part.MSG_ID} = ?", arrayOf(messageId.toString()), null)
        }
    }

    override fun map(from: Cursor): MmsPart {
        return MmsPart(
            id = from.getLong(from.getColumnIndexOrThrow(Telephony.Mms.Part._ID)).toString(),
            messageId = from.getLong(from.getColumnIndexOrThrow(Telephony.Mms.Part.MSG_ID)).toString(),
            type = from.getStringOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE)) ?: "*/*",
            seq = from.getIntOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.SEQ)) ?: -1,
            name = from.getStringOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.NAME))
                ?: from.getStringOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_LOCATION))
                    ?.split("/")?.last(),
            text = from.getStringOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.TEXT))
        )
    }
}