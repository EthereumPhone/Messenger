package org.ethereumphone.model

import kotlinx.datetime.Instant

data class Message(
    val id: String,
    val recipient: Recipient,
    val date: Instant,
    val dateSent: Instant,
    val seen: Boolean,
    val deliveryStatus: DeliveryStatus,
    val replyReference: String?,
    val isMe: Boolean,
    val attachments: List<Attachment>,
    val reactions: List<Reaction>
) {

    fun getSummary(): String {
        TODO()
    }
}
