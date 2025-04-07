package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.RecipientEntity

data class ConversationWithRecipients(
    @Embedded val conversation: ConversationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "inboxId",
        associateBy = Junction(
            value = ConversationRecipientCrossRef::class,
            parentColumn = "conversationId",
            entityColumn = "inboxId"
        )
    )
    val recipients: List<RecipientEntity>
)
