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
import org.ethereumphone.model.Recipient

data class CompositeMessage(
    @Embedded
    val message: MessageEntity,

    @Relation(
        parentColumn = "senderInboxId",
        entityColumn = "inboxId",
        entity = RecipientEntity::class
    )
    // Nullable: a message can be stored before its sender's recipient row exists
    // (e.g. a message just sent into a brand-new conversation).
    val recipient: RecipientWithContact?,


    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val reactions: List<ReactionEntity>

    //TODO: add attachments
)


fun CompositeMessage.toExternalMessage(): Message {
    return Message(
        id = message.id,
        threadId = message.threadId,
        recipient = recipient?.recipientEntity?.toExternalModel(recipient.contactEntity)
            ?: Recipient(id = message.senderInboxId, address = "", ens = null, contact = null),
        dateSent = Instant.fromEpochMilliseconds(message.dateSent),
        date = Instant.fromEpochMilliseconds(message.date),
        seen = message.seen,
        deliveryStatus = DeliveryStatus.valueOf(message.deliveryStatus.name),
        replyReference = message.replyReference,
        isMe = message.isMe,
        attachments = emptyList(),
        reactions = reactions.map { it.toExternalModel() },
        body = message.body
    )
}