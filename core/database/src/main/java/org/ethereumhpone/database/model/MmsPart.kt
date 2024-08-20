package org.ethereumhpone.database.model

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.mms.ContentType
import kotlinx.serialization.Serializable

@Entity("mms_part")
@Serializable
data class MmsPart(
    @PrimaryKey val id: String = "",
    @ColumnInfo(index = true) val messageId: String = "",
    val type: String = "",
    val seq: Int = -1,
    val name: String? = null,
    val text: String? = null
) {

    // We want to reuse MmsPart for Xmtp messages, without needing to change the database
    fun getUri(partType: String = "mms"): Uri {
        return when(partType) {
            "mms" -> "content://mms/part/$id".toUri()
            else -> id.toUri()
        }
    }

    fun getSummary(): String? = when {
        type == "text/plain" -> text
        type == "text/x-vCard" -> "Contact card"
        type.startsWith("image") -> "Photo"
        type.startsWith("video") -> "Video"
        type.startsWith("audio") -> "Audio"
        else -> null
    }
}

fun MmsPart.isSmil() = ContentType.APP_SMIL == type

fun MmsPart.isImage() = ContentType.isImageType(type)

fun MmsPart.isVideo() = ContentType.isVideoType(type)

fun MmsPart.isText() = ContentType.TEXT_PLAIN == type

fun MmsPart.isAudio() = ContentType.isAudioType(type)

fun MmsPart.isVCard() = ContentType.TEXT_VCARD == type

