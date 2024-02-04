package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("blocked_number")
data class BlockedNumber(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address: String = ""
)
