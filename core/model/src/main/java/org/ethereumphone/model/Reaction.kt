package org.ethereumphone.model

data class Reaction(
    val id: String,
    val messageId: String,
    val senderInboxId: String,
    val reactionSchema: ReactionSchema,
    val content: String
)

enum class ReactionSchema {
    UNICODE,
    SHORTCODE,
    CUSTOM
}
