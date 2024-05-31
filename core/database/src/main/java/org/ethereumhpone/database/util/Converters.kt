package org.ethereumhpone.database.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.Recipient

@OptIn(ExperimentalSerializationApi::class)
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromContactsList(contacts: List<Contact>?): String? {
        return gson.toJson(contacts)
    }

    @TypeConverter
    fun toContactsList(contactsString: String?): List<Contact>? {
        if (contactsString == null) return null
        val type = object : TypeToken<List<Contact>>() {}.type
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
    fun fromRecipientList(json: String?): List<Recipient> {
        if (json == null) {
            return emptyList()
        }
        return Json.decodeFromString(json)
    }

    @TypeConverter
    fun toRecipientList(numbers: List<Recipient>): String {
        return Json.encodeToString(numbers)
    }

    @TypeConverter
    fun fromMessage(json: String?): Message? {
        return json?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun toMessage(message: Message?): String? {
        return message?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun fromMmsPartList(json: String?): List<MmsPart> {
        if (json == null) {
            return emptyList()
        }
        return Json.decodeFromString(json)
    }

    @TypeConverter
    fun toMmsPartList(numbers: List<MmsPart>): String {
        return Json.encodeToString(numbers)
    }

    @TypeConverter
    fun fromContact(json: String?): Contact? {
        return json?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun toContact(message: Contact?): String? {
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
}