package org.ethereumhpone.data.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup
import org.ethereumhpone.domain.repository.ContactRepository

class ContactRepositoryImpl(
    private val contactDao: ContactDao
): ContactRepository {
    override fun findContactUri(address: String): Flow<Uri?> {
        TODO("Not yet implemented")
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
            )?.let { contactDao.updateContract(it) }
        }
    }
}