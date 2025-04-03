package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.Recipient

data class CompositeConversation(
    @Embedded
    val conversationEntity: ConversationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "threadId",
    )
    val lastMessageEntity: MessageEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "inboxId",
        associateBy = Junction(ConversationRecipientCrossRef::class)
    )
    val recipients: List<RecipientEntity>
)

fun CompositeConversation.toExternalModel(): Conversation =
    Conversation(
        id = conversationEntity.id,
        title = conversationEntity.title,
        recipients = recipients,
        draft = conversationEntity.draft,
        lastMessage =
    )
