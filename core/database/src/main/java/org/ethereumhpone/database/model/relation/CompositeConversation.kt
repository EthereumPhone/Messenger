package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.database.model.toExternalModel
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.Recipient

data class CompositeConversation(
    @Embedded
    val conversationEntity: ConversationEntity,
    
    @Embedded(prefix = "message_")
    val lastMessageEntity: MessageEntity?,

    @Relation(
        entity = RecipientEntity::class,
        parentColumn = "id",
        entityColumn = "inboxId",
        associateBy = Junction(
            value = ConversationRecipientCrossRef::class,
            parentColumn = "conversationId",
            entityColumn = "inboxId"
        )
    )
    val recipients: List<RecipientWithContact>
)

fun CompositeConversation.toExternalModel(): Conversation {

    val senderRecipient = if (lastMessageEntity != null) {
        recipients.firstOrNull {
            it.recipientEntity.inboxId == lastMessageEntity.senderInboxId
        }
    } else {
        null
    }
    
    return Conversation(
        id = conversationEntity.id,
        title = conversationEntity.title,
        recipients = recipients.map { it.recipientEntity.toExternalModel(it.contactEntity) },
        draft = conversationEntity.draft,
        lastMessage = senderRecipient?.let { lastMessageEntity?.toExternalModel(it.recipientEntity.toExternalModel(it.contactEntity)) },
        archived = conversationEntity.archived,
        blocked = conversationEntity.blocked,
        pinned = conversationEntity.pinned,
        clientInbox = conversationEntity.clientInbox
    )
}

