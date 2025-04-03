package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity("recipient")
@Serializable
data class RecipientEntity(
    @PrimaryKey
    val inboxId: String,
    val address: String,
    val ens: String? = "",
    val contactLookupKey: String? = ""
)
