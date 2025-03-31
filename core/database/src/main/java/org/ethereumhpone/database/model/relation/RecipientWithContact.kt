package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Recipient

data class RecipientWithContact(
    @Embedded
    val recipient: Recipient,
    @Relation(
        parentColumn = "contactLookupKey",
        entityColumn = "lookupKey"
    )
    val contact: Contact?
) {
    fun getDisplayName(): String = contact?.name?.takeIf { it.isNotBlank() }
        ?: recipient.ens
        ?: recipient.address
}
