package org.ethereumphone.model

data class Conversation(
    val id: String,
    val title: String,
    val recipients: List<Recipient>,
    val draft: String?
)
