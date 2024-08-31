package org.ethereumhpone.database.model

import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.Locale

@Entity("recipient")
@Serializable
data class Recipient(
    @PrimaryKey val id: Long = 0,
    val address: String = "",
    val contact: Contact? = null,
    val lastUpdate: Long = 0,

    // XMTP only
    val inboxId: String = ""
) {
    fun getDisplayName(): String = contact?.name?.takeIf { it.isNotBlank() }
        ?: PhoneNumberUtils.formatNumber(address, Locale.getDefault().country)
        ?: address

    fun getShortNameIfEthereum(): String {
        val longName = getDisplayName()
        return if (longName.startsWith("0x") && longName.length == 42) {
            longName.substring(0, 6) + "..." + longName.substring(longName.length - 6)
        } else {
            longName
        }
    }
}
