package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable



@Entity("reaction")
@Serializable
data class MessageReaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: String = "",
    val senderAddress: String = "",
    val unicode: String = "",
) {

    fun getSummary(): String? {
        TODO()
    }
}
