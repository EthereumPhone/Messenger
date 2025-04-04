package org.ethereumphone.model

data class Contact(
    val lookupKey: String,
    val name: String?,
    val photoUri: String?,
    val ethAddress: String?
)
