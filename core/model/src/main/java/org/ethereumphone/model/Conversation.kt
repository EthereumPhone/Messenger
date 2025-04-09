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
    private val clientInbox: String
) {
    fun getHeader(): String =
        title.takeIf { !it.isNullOrBlank() }
            ?: recipients.firstOrNull()?.contact?.name
            ?: recipients.firstOrNull()?.ens
            ?: recipients.firstOrNull()?.address
            ?: ""


    fun getSummary(): String {
        val messageBody = lastMessage?.body.orEmpty()

        return when {
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