package org.ethereumhpone.database.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.RecipientEntity
import org.xmtp.android.library.libxmtp.DecodedMessage

@OptIn(ExperimentalSerializationApi::class)
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromContactsList(contactEntities: List<ContactEntity>?): String? {
        return gson.toJson(contactEntities)
    }

    @TypeConverter
    fun toContactsList(contactsString: String?): List<ContactEntity>? {
        if (contactsString == null) return null
        val type = object : TypeToken<List<ContactEntity>>() {}.type
        return gson.fromJson(contactsString, type)
    }
    @TypeConverter
    fun fromPhoneNumberList(json: String?): List<PhoneNumber> {
        if (json == null) {
            return emptyList()
        }
        return Json.decodeFromString(json)
    }

    @TypeConverter
    fun toPhoneNumberList(numbers: List<PhoneNumber>): String {
        return Json.encodeToString(numbers)
    }

    @TypeConverter
    fun fromRecipientList(json: String?): List<RecipientEntity> {
        if (json == null) {
            return emptyList()
        }
        return Json.decodeFromString(json)
    }

    @TypeConverter
    fun toRecipientList(numbers: List<RecipientEntity>): String {
        return Json.encodeToString(numbers)
    }

    @TypeConverter
    fun fromMessage(json: String?): MessageEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun toMessage(messageEntity: MessageEntity?): String? {
        return messageEntity?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun fromContact(json: String?): ContactEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun toContact(message: ContactEntity?): String? {
        return message?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if(json == null) {
            return emptyList()
        }
        return Json.decodeFromString(json)
    }

    @TypeConverter
    fun fromStringList(stringList: List<String>): String {
        return Json.encodeToString(stringList)
    }

    @TypeConverter
    fun fromMessageDeliveryStatus(ordinal: Int): DecodedMessage.MessageDeliveryStatus {
        return DecodedMessage.MessageDeliveryStatus.entries[ordinal]
    }

    @TypeConverter
    fun toMessageDeliveryStatus(status: DecodedMessage.MessageDeliveryStatus): Int {
        return status.ordinal
    }
}