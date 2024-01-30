package org.ethereumhpone.database.dao

import android.provider.Telephony
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart

@Dao
interface MessageDao {


    @Query("SELECT * FROM message WHERE threadId = :threadId AND " +
            "(body LIKE '%' || :query || '%' OR " +
            "EXISTS (SELECT 1 FROM mms_part WHERE messageId = message.id AND text LIKE '%' || :query || '%')) " +
            "ORDER BY date")
    fun getMessages(threadId: Long, query: String): Flow<List<Message>>

    @Query("SELECT * FROM message where id == :id")
    fun getMessage(id: Long): Message?

    @Query("SELECT * FROM message where id = (SELECT id FROM mms_part WHERE id = :id)")
    fun getMessageForPart(id: Long): Message?

    @Query("SELECT * FROM message WHERE threadId = :threadId AND " +
            "((type = 'sms' AND boxId IN (:smsInboxTypes)) OR (type = 'mms' AND boxId IN (:mmsInboxTypes))) " +
            "ORDER BY date DESC LIMIT 1")
    fun getLastIncomingMessage(
        threadId: Long,
        smsInboxTypes: IntArray = intArrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX, Telephony.Sms.MESSAGE_TYPE_ALL),
        mmsInboxTypes: IntArray = intArrayOf(Telephony.Mms.MESSAGE_BOX_INBOX, Telephony.Mms.MESSAGE_BOX_ALL)
    ): Flow<Message>

    @Query("SELECT COUNT(*) FROM conversation INNER JOIN message ON conversation.id = message.threadId WHERE conversation.archived = 0 AND conversation.blocked = 0 AND message.read = 0")
    fun getUnreadCount(): Long

    @Query("")
    fun getPart(id: Long): MmsPart?

    fun getPartsForConversation(threadId: Long): Flow<List<MmsPart>>

    @Insert
    fun savePart(mmsPart: MmsPart)

    @Insert
    fun insertMessage(message: Message)

    @Update
    fun updateMessage(message: Message)


}