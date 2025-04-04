package org.ethereumhpone.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ethereumphone.model.Contact

@Entity("contact")
@Serializable
data class ContactEntity(
    @PrimaryKey val lookupKey: String = "",
    val numbers: List<PhoneNumber> = emptyList(),
    val name: String = "",
    val photoUri: String? = null,
    val favourite: Boolean = false,
    val lastUpdate: Long = 0,
    val ethAddress: String? = null,
) {
    fun getDefaultNumber(): PhoneNumber? = numbers.find { number -> number.isDefault }
}


fun ContactEntity.toExternalModel(): Contact = Contact(
    lookupKey = lookupKey,
    name = name,
    photoUri = photoUri,
    ethAddress = ethAddress
)
