package org.ethereumhpone.database.model.relation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index


@Entity(primaryKeys = ["conversationId", "inboxId"])
data class ConversationRecipientCrossRef(
    @ColumnInfo(index = true) val conversationId: String,
    @ColumnInfo(index = true) val inboxId: String
)
