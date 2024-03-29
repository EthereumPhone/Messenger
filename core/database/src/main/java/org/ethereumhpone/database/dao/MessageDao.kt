package org.ethereumhpone.database.dao

import android.provider.Telephony
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.relation.MessageWithParts

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE threadId = :threadId AND " +
            "(body LIKE '%' || :query || '%' OR " +
            "EXISTS (SELECT 1 FROM mms_part WHERE messageId = message.id AND text LIKE '%' || :query || '%')) " +
            "ORDER BY date")
    fun getMessages(threadId: Long, query: String = ""): Flow<List<Message>>

    @Query("SELECT * FROM message where id == :id")
    fun getMessage(id: Long): Flow<Message?>

    @Query("SELECT * FROM message where id IN (SELECT messageId FROM mms_part WHERE id = :id)")
    fun getMessageForPart(id: Long): Flow<Message?>

    @Query("SELECT * FROM message where threadId = :threadId ORDER BY date DESC LIMIT 1")
    fun getLastConversationMessage(threadId: Long): Flow<Message?>

    @Query("SELECT * FROM message where threadId = :threadId")
    fun getAllConversationMessages(threadId: Long): Flow<List<Message>>

    @Query("SELECT * FROM message WHERE threadId = :threadId AND " +
            "((type = 'sms' AND boxId IN (:smsInboxTypes)) OR (type = 'mms' AND boxId IN (:mmsInboxTypes))) " +
            "ORDER BY date DESC LIMIT 1")
    fun getLastIncomingMessage(
        threadId: Long,
        smsInboxTypes: IntArray,
        mmsInboxTypes: IntArray
    ): Flow<Message>

    @Query("SELECT COUNT(*) FROM conversation INNER JOIN message ON conversation.id = message.threadId WHERE conversation.archived = 0 AND conversation.blocked = 0 AND message.read = 0")
    fun getUnreadCount(): Flow<Long>

    @Query("SELECT * FROM message WHERE seen = 0 AND read = 0 ORDER BY date")
    suspend fun getUnreadUnseenMessages(): List<Message>

    @Query("SELECT * FROM message WHERE seen = 0")
    fun getUnseenMessages(): Flow<List<Message>>

    @Query("SELECT * FROM mms_part WHERE id = :id")
    fun getPart(id: Long): Flow<MmsPart?>

    @Query("SELECT * FROM mms_part WHERE messageId IN (SELECT id FROM Message WHERE threadId = :threadId) AND (type LIKE 'image/%' OR type LIKE 'video/%') ORDER BY id DESC")
    fun getPartsForConversation(threadId: Long): Flow<List<MmsPart>>

    @Query("SELECT * FROM message WHERE id = :id")
    fun getMessagesWithParts(id: Long): Flow<List<MessageWithParts>>

    @Query("SELECT * FROM message WHERE body LIKE '%' || :query || '%' OR " +
            "EXISTS (SELECT 1 FROM mms_part WHERE id = mms_part.id AND mms_part.text LIKE '%' || :query || '%')")
    fun searchMessages(query: String): Flow<List<Message>>

    @Query("SELECT * FROM mms_part WHERE messageId = :messageId")
    fun getMmsPartByMessageId(messageId: Long): Flow<List<MmsPart>>

    @Query("SELECT id FROM message WHERE contentId = :contentId AND type = :type LIMIT 1")
    fun getMessageId(contentId: Long, type: String): Flow<Long?>


    @Insert
    suspend fun insertMessage(message: Message)

    @Update
    suspend fun updateMessages(messages: List<Message>)

    @Upsert
    suspend fun upsertMessage(message: Message)

    @Upsert
    suspend fun upsertMessagePart(mmsPart: MmsPart)

    @Delete
    suspend fun deleteAllMessage(message: List<Message>)

    @Delete
    suspend fun deleteMessage(message: Message)

}