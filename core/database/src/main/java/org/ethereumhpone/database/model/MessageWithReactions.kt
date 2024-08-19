package org.ethereumhpone.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithReactions(
    @Embedded val message: Message,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val messageReactions: List<MessageReaction>
)
