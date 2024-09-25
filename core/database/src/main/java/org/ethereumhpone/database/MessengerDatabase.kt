package org.ethereumhpone.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ethereumhpone.database.dao.BlockingDao
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.PhoneNumberDao
import org.ethereumhpone.database.dao.ReactionDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.dao.SyncLogDao
import org.ethereumhpone.database.model.BlockedNumber
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MessageReaction
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.database.model.SyncLog
import org.ethereumhpone.database.util.Converters

@Database(
    entities = [
        Contact::class,
        Conversation::class,
        Message::class,
        MessageReaction::class,
        MmsPart::class,
        PhoneNumber::class,
        Recipient::class,
        ContactGroup::class,
        BlockedNumber::class,
        SyncLog::class
    ],
    version = 2,
    autoMigrations = [
       AutoMigration(from = 1, to = 2, spec = DatabaseMigrations.Schema1to2::class)
    ],
    exportSchema = true,
)

@TypeConverters(Converters::class)
abstract class MessengerDatabase: RoomDatabase() {
    abstract val messageDao: MessageDao
    abstract val conversationDao: ConversationDao
    abstract val contactDao: ContactDao
    abstract val recipientDao: RecipientDao
    abstract val blockingDao: BlockingDao
    abstract val phoneNumberDao: PhoneNumberDao
    abstract val syncLogDao: SyncLogDao
    abstract val reactionDao: ReactionDao

}