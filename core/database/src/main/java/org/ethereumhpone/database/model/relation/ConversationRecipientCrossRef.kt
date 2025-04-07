package org.ethereumhpone.database.model.relation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.RecipientEntity


@Entity(
    tableName = "conversation_recipient_cross_ref",
    primaryKeys = ["conversationId", "memberInboxId"],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RecipientEntity::class,
            parentColumns = ["inboxId"],
            childColumns = ["memberInboxId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberInboxId"), Index("conversationId")]
)
data class ConversationRecipientCrossRef(
    val conversationId: String,
    val memberInboxId: String
)
