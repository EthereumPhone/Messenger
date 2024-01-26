package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity("phone_number")
@Serializable
data class PhoneNumber(
    @PrimaryKey val id: Long = 0,
    val accountType: String? = "",
    val address: String = "",
    val type: String = "",
    val isDefault: Boolean = false
)
