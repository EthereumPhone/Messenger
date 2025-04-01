package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID


@Entity("reaction")
@Serializable
data class MessageReaction(
    @PrimaryKey val id: String,
    val messageId: String = "",
    val inboxId: String = "",
    val unicode: String = "",
) {

    fun getSummary(): String? {
        TODO()
    }
}
