package org.ethereumhpone.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup

interface ContactRepository {

    fun findContactUri(address: String): Uri?
    fun getContacts(): Flow<List<Contact>>
    fun getUnmanagedContact(lookupKey: String): Contact?
    fun getUnmanagedContacts(starred: Boolean = false): Flow<List<Contact>>
    fun getUnmanagedContactGroups(): Flow<List<ContactGroup>>
    fun setDefaultPhoneNumber(lookupKey: String, phoneNumberId: Long)

}