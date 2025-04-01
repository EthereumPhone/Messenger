package org.ethereumhpone.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity("conversation")
data class Conversation(
    @PrimaryKey val id: String,
    @ColumnInfo(index = true) val archived: Boolean = false,
    @ColumnInfo(index = true) val blocked: Boolean = false,
    @ColumnInfo(index = true) val pinned: Boolean = false,
    val title: String?,
    val members: List<String> = emptyList(),
    val draft: String = "",
    val blockingClient: Int? = null,
    val blockReason: String? = null,
) {
    fun getConversationTitle(): String = title.takeIf { it.isNullOrBlank() }
        ?: members.joinToString(", ")
}
