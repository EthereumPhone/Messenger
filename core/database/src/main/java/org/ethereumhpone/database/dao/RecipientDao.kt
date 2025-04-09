package org.ethereumhpone.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.database.model.relation.RecipientWithContact


@Dao
interface RecipientDao {

    @Query("SELECT * FROM recipient")
    fun getRecipients(): Flow<List<RecipientEntity>>

    @Query(
        """
            SELECT * FROM recipient
            WHERE address IN (:addresses)
        """
    )
    fun getRecipientsByAddress(addresses: List<String>): Flow<List<RecipientEntity>>

    @Transaction
    @Query("SELECT * FROM recipient")
    fun getRecipientsWithContact(): List<RecipientWithContact>

    @Transaction
    @Query("SELECT * FROM recipient where inboxId = :inboxId")
    fun getRecipientWithContact(inboxId: String): Flow<RecipientWithContact?>

    @Query("SELECT * FROM recipient WHERE inboxId = :inboxId")
    fun getRecipient(inboxId: Long): Flow<RecipientEntity?>

    @Query("SELECT * FROM recipient WHERE inboxId in (:recipientIds)")
    fun getRecipientsByIds(recipientIds: List<Long>): Flow<List<RecipientEntity>>

    @Upsert
    suspend fun upsertRecipient(recipientEntity: RecipientEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipients(recipientEntities: List<RecipientEntity>)

    @Upsert
    suspend fun upsertRecipients(recipientEntities: List<RecipientEntity>)


}