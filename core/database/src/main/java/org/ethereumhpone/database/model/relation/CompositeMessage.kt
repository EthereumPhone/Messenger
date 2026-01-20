package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.datetime.Instant
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.ReactionEntity
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.database.model.toExternalModel
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message

data class CompositeMessage(
    @Embedded
    val message: MessageEntity,

    @Relation(
        parentColumn = "senderInboxId",
        entityColumn = "inboxId",
        entity = RecipientEntity::class
    )
    val recipient: RecipientWithContact,


    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val reactions: List<ReactionEntity>

    //TODO: add attachments
)


fun CompositeMessage.toExternalMessage(): Message {
    val recipientModel = recipient.recipientEntity.toExternalModel(recipient.contactEntity)
    return message.toExternalModel(recipientModel).copy(
        reactions = reactions.map { it.toExternalModel() }
    )
}