package org.ethereumphone.model

import java.net.URI

data class Contact(
    val lookupKey: String,
    val name: String?,
    val photoUri: URI?,
    val ethAddress: String?
)
