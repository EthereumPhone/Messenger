package org.ethereumhpone.data.repository

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup
import org.ethereumhpone.domain.repository.ContactRepository
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val context: Context,

    ): ContactRepository {
    override suspend fun findContactUri(address: String): Uri {
        val uri = when {
            address.contains('@') -> {
                Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(address))
            }
            else -> Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
        }

        /*
        uri?.let {
            val cur = context.contentResolver.query(uri, arrayOf(BaseColumns._ID), null, null, null)
            cur.
        }
         */

        TODO()
    }

    override fun getContacts(): Flow<List<Contact>> =
        contactDao.getContacts()

    override fun getUnmanagedContact(lookupKey: String): Flow<Contact?> =
        contactDao.getUnmanagedContact(lookupKey)

    override fun getUnmanagedContacts(starred: Boolean): Flow<List<Contact>> =
        contactDao.getUnmanagedContacts(starred)

    override fun getUnmanagedContactGroups(): Flow<List<ContactGroup>> =
        contactDao.getUnmanagedContactGroups()

    override suspend fun setDefaultPhoneNumber(lookupKey: String, phoneNumberId: Long) {
        contactDao.getUnmanagedContact(lookupKey).map { contact ->
            contact?.copy(
                numbers = contact.numbers.map { number ->
                    number.copy(isDefault = number.id == phoneNumberId)
                }
            )?.let { contactDao.upsertContact(listOf(it)) }
        }
    }
}