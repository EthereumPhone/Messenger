package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID


@Entity("reaction")
@Serializable
data class MessageReaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val messageId: String = "",
    val senderAddress: String = "",
    val unicode: String = "",
) {

    fun getSummary(): String? {
        TODO()
    }
}
