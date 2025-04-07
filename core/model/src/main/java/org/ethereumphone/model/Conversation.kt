package org.ethereumphone.model

data class Conversation(
    val id: String,
    val title: String?,
    val recipients: List<Recipient>,
    val draft: String?,
    val lastMessage: Message?,
    val archived: Boolean = false,
    val blocked: Boolean = false,
    val pinned: Boolean = false,
) {

    fun getTitle(): String =
        title.takeIf { !it.isNullOrBlank() }
            ?: recipients.first().contact?.name
            ?: recipients.first().ens
            ?: recipients.first().address


    fun getSummary(): String {
        val messageBody = lastMessage?.body.orEmpty()

        return when {
            recipients.size == 1 -> messageBody
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