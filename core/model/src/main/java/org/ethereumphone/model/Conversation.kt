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
        // 1. Use explicit conversation title if present.
        title?.takeIf { it.isNotBlank() }?.let { return it }

        // 2. Exclude the current user's inbox from consideration to avoid showing their own address.
        val otherRecipient = recipients.firstOrNull { it.id != clientInbox }

        otherRecipient?.let { recipient ->
            // 1) Prefer the local contact name (if the user has saved one)
            // 2) Otherwise prefer the resolved ENS name
            // 3) Finally fall back to displaying the raw address             
            return recipient.contact?.name?.takeIf { it.isNotBlank() }
                ?: recipient.ens?.takeIf { it.isNotBlank() }
                ?: recipient.address
        }

        return ""
    }

    fun getOtherRecipients(): List<Recipient> = recipients.filter { it.id != clientInbox }

    fun getOtherRecipientAddress(): String? = recipients.firstOrNull { it.id != clientInbox }?.address


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