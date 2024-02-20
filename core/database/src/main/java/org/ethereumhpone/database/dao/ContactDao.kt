package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup


@Dao
interface ContactDao {

    @Query("SELECT * FROM contact")
    fun getContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contact WHERE lookupKey = :lookupKey LIMIT 1")
    fun getUnmanagedContact(lookupKey: String): Flow<Contact?>

    @Query("SELECT * FROM contact WHERE (:favourite IS NULL or favourite = :favourite)")
    fun getUnmanagedContacts(favourite: Boolean? = null): Flow<List<Contact>>

    @Query("SELECT * FROM contact_group WHERE contacts IS NOT NULL")
    fun getUnmanagedContactGroups(): Flow<List<ContactGroup>>

    @Update
    fun updateContract(contract: Contact)
}