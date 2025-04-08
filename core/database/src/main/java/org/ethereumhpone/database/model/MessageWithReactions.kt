package org.ethereumhpone.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithReactions(
    @Embedded val messageEntity: MessageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val reactionEntities: List<ReactionEntity>
)
