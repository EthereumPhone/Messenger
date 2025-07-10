package org.ethereumphone.model

import kotlinx.datetime.Instant

data class Conversation(
    val id: String,
    val title: String?,
    val recipients: List<Recipient>,
    val draft: String?,
    val lastMessage: Message?,
    val createdAt: Instant = Instant.DISTANT_PAST,
    val archived: Boolean = false,
    val blocked: Boolean = false,
    val pinned: Boolean = false,
    val unknown: Boolean = false,
    val isGroup: Boolean = false,
    val isOptionsRevealed: Boolean = false,
    private val clientInbox: String
) {
    fun getHeader(): String {
        // Prioritize explicit conversation title when provided
        title?.takeIf { it.isNotBlank() }?.let { return it }

        // Attempt to derive header from the recipient of the most recent message first
        val candidateRecipient = lastMessage?.recipient ?: recipients.firstOrNull()

        candidateRecipient?.let { recipient ->
            return listOfNotNull(
                recipient.contact?.name,
                recipient.ens,
                recipient.address
            ).firstOrNull { it.isNotBlank() } ?: ""
        }

        return ""
    }


    fun getSummary(): String {
        val messageBody = lastMessage?.body.orEmpty()

        return when {
            messageBody.isEmpty() -> ""
            recipients.size == 2 -> messageBody
            lastMessage?.isMe == true -> messageBody
            else -> {
                val sender = lastMessage?.recipient?.let {
                    it.contact?.name
                        ?: it.ens
                        ?: it.contact?.ethAddress
                        ?: it.id
                }.orEmpty()

                if (sender.isNotBlank() && messageBody.isNotBlank()) {
                    "$sender: $messageBody"
                } else {
                    sender.ifBlank { messageBody }
                }
            }
        }
    }

}