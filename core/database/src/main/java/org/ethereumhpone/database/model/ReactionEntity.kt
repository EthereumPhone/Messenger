package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ethereumphone.model.Reaction
import org.ethereumphone.model.ReactionSchema


@Entity("reaction")
@Serializable
data class ReactionEntity(
    @PrimaryKey val id: String,
    val messageId: String = "",
    val inboxId: String = "",
    val content: String = "",
) {

    fun getSummary(): String? {
        TODO()
    }
}


fun ReactionEntity.toExternalModel(): Reaction = Reaction(
    id = id,
    messageId = messageId,
    senderInboxId = inboxId,
    reactionSchema = ReactionSchema.UNICODE,
    content = content
)
