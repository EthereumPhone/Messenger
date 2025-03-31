package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message

data class ConversationWithLastMessage(
    @Embedded
    val conversation: Conversation,
    @Relation(
        parentColumn = "id",
        entityColumn = "threadId",
    )
    val lastMessage: Message?
) {
    val date: Long get() = lastMessage?.date ?: 0
    val snippet: String? get() = lastMessage?.getSummary()
    val unread: Boolean get() = lastMessage?.read == false

    fun getConversationTitle(): String = conversation.getConversationTitle()
}
