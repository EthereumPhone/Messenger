package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart

data class MessageWithParts(
    @Embedded val message: Message,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val parts: List<MmsPart>
)


