package org.ethereumhpone.database.model.relation

import androidx.room.Entity


@Entity(
    tableName = "conversation_recipient_cross_ref",
    primaryKeys = ["conversationId", "recipientId"]
)
data class ConversationRecipientCrossRef(
    val conversationId: String,
    val recipientId: String
)