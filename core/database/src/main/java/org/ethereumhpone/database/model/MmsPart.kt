package org.ethereumhpone.database.model

import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity("mms_part")
@Serializable
data class MmsPart(
    @PrimaryKey val id: Long = 0,
    @ColumnInfo(index = true) val messageId: Long = 0,
    val type: String = "",
    val seq: Int = -1,
    val name: String? = null,
    val text: String? = null
) {
    fun getUri() = "content://mms/part/$id".toUri()

    fun getSummary(): String? = when {
        type == "text/plain" -> text
        type == "text/x-vCard" -> "Contact card"
        type.startsWith("image") -> "Photo"
        type.startsWith("video") -> "Video"
        else -> null
    }
}
