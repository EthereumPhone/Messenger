package org.ethereumhpone.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup

interface ContactRepository {

    suspend fun findContactUri(address: String): Uri
    fun getContacts(): Flow<List<Contact>>
    fun getUnmanagedContact(lookupKey: String): Flow<Contact?>
    fun getUnmanagedContacts(starred: Boolean = false): Flow<List<Contact>>
    fun getUnmanagedContactGroups(): Flow<List<ContactGroup>>
    suspend fun setDefaultPhoneNumber(lookupKey: String, phoneNumberId: Long)

}