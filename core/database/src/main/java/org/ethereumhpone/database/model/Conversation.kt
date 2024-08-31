package org.ethereumhpone.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity("conversation")
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val archived: Boolean = false,
    @ColumnInfo(index = true) val blocked: Boolean = false,
    @ColumnInfo(index = true) val pinned: Boolean = false,
    val recipients: List<Recipient> = emptyList(),
    val lastMessage: Message? = null,
    val draft: String = "",
    val blockingClient: Int? = null,
    val blockReason: String? = null,
    val title: String = "", // conversation title
    // XMTP field
    var unknown : Boolean = false
) {
    val date: Long get() = lastMessage?.date ?: 0
    val snippet: String? get() = lastMessage?.getSummary()
    val unread: Boolean get() = lastMessage?.read == false
    val me: Boolean get() = lastMessage?.isMe() == true

    fun getConversationTitle(): String {
        return title.takeIf { it.isNotBlank() }
            ?: recipients.joinToString { recipient -> recipient.getDisplayName() }
    }

    fun getShortTitleIfEthereum(): String {
        val longName = getConversationTitle()
        return if (longName.startsWith("0x") && longName.length == 42) {
            longName.substring(0, 6) + "..." + longName.substring(longName.length - 6)
        } else {
            longName
        }
    }
}
