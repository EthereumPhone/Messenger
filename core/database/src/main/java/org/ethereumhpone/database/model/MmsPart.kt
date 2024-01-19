package org.ethereumhpone.database.model

import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MmsPart(
    @PrimaryKey
    val id: Long = 0,
    val messageId: Long = 0,
    var type: String = "",
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
