package org.ethereumhpone.data.mapper

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.mapper.ContactCursor
import javax.inject.Inject

class ContactCursorImpl @Inject constructor(
    private val context: Context,
    private val permissionManager: PermissionManager

) : ContactCursor {

    companion object {
        val URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED,
            ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP,
            ContactsContract.Data.DATA15
        )

        const val COLUMN_ID = 0
        const val COLUMN_LOOKUP_KEY = 1
        const val COLUMN_ACCOUNT_TYPE = 2
        const val COLUMN_NUMBER = 3
        const val COLUMN_TYPE = 4
        const val COLUMN_LABEL = 5
        const val COLUMN_DISPLAY_NAME = 6
        const val COLUMN_PHOTO_URI = 7
        const val COLUMN_STARRED = 8
        const val CONTACT_LAST_UPDATED = 9
        const val COLUMN_ETH_ADDRESS = 10
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
    override fun getContactsCursor(): Cursor? {
        return when (permissionManager.hasContacts()) {
            true -> context.contentResolver.query(URI, PROJECTION, null, null, null)
            false -> null
        }    }

    @SuppressLint("Range")
    override fun map(from: Cursor): Contact {
        return Contact(
            lookupKey = from.getString(COLUMN_LOOKUP_KEY),
            name = from.getString(COLUMN_DISPLAY_NAME) ?: "",
            photoUri = from.getString(COLUMN_PHOTO_URI),
            numbers = listOf(PhoneNumber(
                id = from.getLong(COLUMN_ID),
                accountType = from.getString(COLUMN_ACCOUNT_TYPE),
                address = from.getString(COLUMN_NUMBER) ?: "",
                type = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.resources, from.getInt(COLUMN_TYPE),
                    from.getString(COLUMN_LABEL)).toString()
            )),
            favourite = from.getInt(COLUMN_STARRED) != 0,
            lastUpdate = from.getLong(CONTACT_LAST_UPDATED),
            ethAddress = getData15ForContact(from.getString(COLUMN_LOOKUP_KEY))
        )
    }

}