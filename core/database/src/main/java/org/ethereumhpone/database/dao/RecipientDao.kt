package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Recipient


@Dao
interface RecipientDao {

    @Query("SELECT * FROM recipient")
    fun getRecipients(): Flow<List<Recipient>>

    @Query("SELECT * FROM recipient WHERE contact IS NOT NULL")
    fun getUnmanagedRecipients(): Flow<List<Recipient>>

    @Query("SELECT * FROM recipient WHERE id = :recipientId")
    fun getRecipient(recipientId: Long): Flow<Recipient?>

    @Query("SELECT * FROM recipient WHERE id in (:recipientIds)")
    fun getRecipientsByIds(recipientIds: List<Long>): Flow<List<Recipient>>

    @Upsert
    suspend fun upsertRecipient(recipient: Recipient)


}