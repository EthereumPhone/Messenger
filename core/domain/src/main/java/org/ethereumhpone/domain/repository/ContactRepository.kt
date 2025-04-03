package org.ethereumhpone.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.database.model.ContactGroup

interface ContactRepository {

    suspend fun findContactUri(address: String): Uri
    fun getContacts(): Flow<List<ContactEntity>>
    fun getUnmanagedContact(lookupKey: String): Flow<ContactEntity?>
    fun getUnmanagedContacts(starred: Boolean = false): Flow<List<ContactEntity>>
    fun getUnmanagedContactGroups(): Flow<List<ContactGroup>>
    suspend fun setDefaultPhoneNumber(lookupKey: String, phoneNumberId: Long)

}