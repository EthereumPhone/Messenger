package org.ethereumhpone.data.mapper

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.mapper.ConversationCursor
import java.util.UUID
import javax.inject.Inject

class ConversationCursorImpl @Inject constructor(
    private val context: Context,
    private val permissionManager: PermissionManager
): ConversationCursor {

    companion object {
        val URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
        val PROJECTION = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.RECIPIENT_IDS,
            Telephony.Threads.DATE, // This might be the date of the last message.
            Telephony.Threads.SNIPPET, // This is typically the last message snippet.
            Telephony.Threads.READ // Indicates if the last message was read.
        )

        const val ID = 0
        const val RECIPIENT_IDS = 1
        const val DATE = 2
        const val SNIPPET = 3
        const val READ = 4
    }

    override fun getConversationsCursor(): Cursor? {
        return when (permissionManager.hasReadSms()) {
            true -> context.contentResolver.query(URI, PROJECTION, null, null, "date desc")
            false -> null
        }
    }

    override fun map(from: Cursor): Conversation {
        val id = from.getLong(ID)
        val recipientIds = from.getString(RECIPIENT_IDS)
            .split(" ")
            .filter { it.isNotBlank() }
            .map { recipientId -> recipientId.toLong() }

        val recipientList = recipientIds.mapNotNull { recipientId ->
            fetchRecipientDetails(recipientId)
        }

        val date = from.getLong(DATE) // Last message date
        val snippet = from.getString(SNIPPET) // Snippet of the last message
        val read = from.getInt(READ) > 0 // Whether the last message was read

        // Assuming defaults for non-available values
        val conversation = try {
            Conversation(
                id = id,
                recipients = recipientList,
                lastMessage = if (snippet != null) Message(body = snippet, date = date, read = read, type = "sms") else null,
                archived = false,
                blocked = false,
                pinned = false,
                draft = "",
                title = "" // This needs to be populated if you have a logic to set titles
            )
        } catch (e: Exception) {
            Conversation(id = id, recipients = recipientList)
        }

        return conversation
    }

    private fun fetchRecipientDetails(recipientId: Long): Recipient {
        // This method should query your data source for the recipient's address and contact info
        // Here's a placeholder implementation; you need to replace this with actual fetching logic
        val address = fetchAddressForRecipient(context, recipientId)
        val contact = getContactFromRecipientId(context, recipientId)

        return Recipient(
            id = recipientId,
            address = address,
            contact = contact,
            lastUpdate = System.currentTimeMillis() // Example: updating with current timestamp
        )
    }

    @SuppressLint("Range")
    private fun fetchAddressForRecipient(context: Context, recipientId: Long): String {
        val addressCursor = context.getContentResolver().query(
            Uri.parse("content://mms-sms/canonical-addresses"), null,
            "_id = $recipientId", null, null
        )
        if (addressCursor == null || !addressCursor.moveToFirst()) {
            return ""
        }
        val phoneNumber = addressCursor.getString(addressCursor.getColumnIndex("address"))
        addressCursor.close()
        return phoneNumber ?: ""
    }

    @SuppressLint("Range")
    private
    fun getContactFromRecipientId(ctx: Context, recipientId: Long): Contact? {
        // Fetch the phone number using recipient ID
        val phoneNumber = ctx.contentResolver.query(
            Uri.parse("content://mms-sms/canonical-addresses"),
            null,
            "_id = ?",
            arrayOf(recipientId.toString()),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndex("address")) else null
        } ?: return null

        // Use the phone number to look up the contact's details in the Contacts content provider
        if (phoneNumber.isEmpty()) return null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        ctx.contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                val isFavorite = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1

                // Fetch all phone numbers for this contact
                val numbers = mutableListOf<PhoneNumber>()
                val phonesCursor = ctx.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET),
                    ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?",
                    arrayOf(lookupKey),
                    null
                )
                phonesCursor?.use {
                    var isDefaultSet = false
                    while (it.moveToNext()) {
                        val id = it.getLong(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
                        val address = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                        val type = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)) ?: ""
                        val accountType = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET)) ?: ""
                        val isDefault = if (!isDefaultSet) { isDefaultSet = true; true } else false // Assume the first number is the default
                        numbers.add(PhoneNumber(id, accountType, address, type, isDefault))
                    }
                }

                return Contact(lookupKey, numbers, name, photoUri, isFavorite, System.currentTimeMillis(), getData15ForContact(lookupKey))
            }
        }
        return null
    }

    @SuppressLint("Range")
    fun getData15ForContact(contactId: String): String? {
        val contentResolver: ContentResolver = context.contentResolver
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.DATA15)
        val selection = "${ContactsContract.Data.LOOKUP_KEY} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA15))
            }
        }
        return null
    }

    private fun fetchPhoneNumbersForContact(context: Context, lookupKey: String): List<PhoneNumber> {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ?"
        val selectionArgs = arrayOf(lookupKey)
        val phoneNumbers = mutableListOf<PhoneNumber>()

        val phoneCursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        phoneCursor?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            val labelIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
            val primaryIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)
            val accountTypeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET)

            while (cursor.moveToNext()) {
                val id = UUID.randomUUID().mostSignificantBits // Generate a pseudo-random ID
                val number = cursor.getString(numberIndex)
                val type = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.resources, cursor.getInt(typeIndex), cursor.getString(labelIndex)).toString()
                val isDefault = cursor.getInt(primaryIndex) > 0
                val accountType = cursor.getString(accountTypeIndex)

                phoneNumbers.add(PhoneNumber(
                    id = id,
                    accountType = accountType,
                    address = number,
                    type = type,
                    isDefault = isDefault
                ))
            }
        }

        return phoneNumbers
    }


}