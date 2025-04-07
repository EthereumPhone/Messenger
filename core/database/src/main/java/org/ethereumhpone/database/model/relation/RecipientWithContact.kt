package org.ethereumhpone.database.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.database.model.RecipientEntity

data class RecipientWithContact(
    @Embedded
    val recipientEntity: RecipientEntity,
    @Relation(
        parentColumn = "contactLookupKey",
        entityColumn = "lookupKey"
    )
    val contactEntity: ContactEntity?
)
