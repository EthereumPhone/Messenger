package org.ethereumhpone.database.model

import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.Locale

@Entity("recipient")
@Serializable
data class Recipient(
    @PrimaryKey
    val inboxId: String,
    val address: String,
    val ens: String? = "",
    val contactLookupKey: String? = ""
)
