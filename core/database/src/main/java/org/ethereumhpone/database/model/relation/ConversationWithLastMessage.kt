package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.MessageEntity

data class ConversationWithLastMessage(
    @Embedded
    val conversationEntity: ConversationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "threadId",
    )
    val lastMessageEntity: MessageEntity?
) {
    val date: Long get() = lastMessageEntity?.date ?: 0
    val snippet: String? get() = lastMessageEntity?.getSummary()
    val unread: Boolean get() = lastMessageEntity?.read == false

    fun getConversationTitle(): String = conversationEntity.getConversationTitle()
}
