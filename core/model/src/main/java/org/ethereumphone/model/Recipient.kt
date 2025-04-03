package org.ethereumphone.model

data class Recipient(
    val id: String,
    val address: String,
    val ens: String?,
    val contact: Contact?,
)
