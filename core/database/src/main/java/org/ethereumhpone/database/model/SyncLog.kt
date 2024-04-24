package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SyncLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis()
)
