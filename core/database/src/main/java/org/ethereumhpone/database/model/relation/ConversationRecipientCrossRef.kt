package org.ethereumhpone.database.model.relation

import androidx.room.Entity



@Entity(primaryKeys = ["conversationId", "inboxId"])
data class ConversationRecipientCrossRef(
    val conversationId: String,
    val inboxId: String
)
