package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("contact_group")
data class ContactGroup(
    @PrimaryKey val id: Long = 0,
    val title: String = "",
    val contacts: List<Contact> = emptyList()
)
