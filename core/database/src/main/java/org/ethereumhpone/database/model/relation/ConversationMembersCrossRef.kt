package org.ethereumhpone.database.model.relation

import androidx.room.Entity


@Entity(primaryKeys = ["conversationId", "recipientInboxId"])
data class ConversationRecipientCrossRef(
    val conversationId: String,
    val recipientInboxId: String
)