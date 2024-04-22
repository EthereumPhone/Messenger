package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.PhoneNumber

@Dao
interface PhoneNumberDao {

    @Query("SELECT * FROM phone_number WHERE isDefault = 1")
    fun getDefaultNunmberIds(): Flow<List<PhoneNumber>>
}