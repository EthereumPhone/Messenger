package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable



@Entity("reaction")
@Serializable
data class Reaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long = 0,
    val senderAddress: String = "",
    val content: String = "",
) {
    enum class Schema { UNICODE, SHORTCODE }

}
