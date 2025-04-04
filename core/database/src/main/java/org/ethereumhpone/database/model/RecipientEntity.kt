package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ethereumphone.model.Recipient

@Entity("recipient")
@Serializable
data class RecipientEntity(
    @PrimaryKey
    val inboxId: String,
    val address: String,
    val ens: String? = "",
    val contactLookupKey: String? = ""
)

fun RecipientEntity.toExternalModel(contact: ContactEntity?): Recipient = Recipient(
    id = inboxId,
    address = address,
    ens = ens,
    contact = contact?.toExternalModel()
)
