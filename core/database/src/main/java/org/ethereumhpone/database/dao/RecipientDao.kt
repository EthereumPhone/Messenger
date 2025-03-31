package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.database.model.relation.RecipientWithContact


@Dao
interface RecipientDao {

    @Query("SELECT * FROM recipient")
    fun getRecipients(): Flow<List<Recipient>>

    @Query("SELECT * FROM recipient")
    fun getRecipientsWithContact(): List<RecipientWithContact>

    @Query("SELECT * FROM recipient WHERE inboxId = :inboxId")
    fun getRecipient(inboxId: Long): Flow<Recipient?>

    @Query("SELECT * FROM recipient WHERE inboxId in (:recipientIds)")
    fun getRecipientsByIds(recipientIds: List<Long>): Flow<List<Recipient>>

    @Upsert
    suspend fun upsertRecipient(recipient: Recipient)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipient(recipient: Recipient)

    @Upsert
    suspend fun upsertRecipients(recipients: List<Recipient>)


}