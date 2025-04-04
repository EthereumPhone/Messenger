package org.ethereumhpone.database

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
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.database.model.ContactGroup
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.MessageReaction
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.database.model.SyncLog
import org.ethereumhpone.database.model.relation.ConversationRecipientCrossRef
import org.ethereumhpone.database.util.Converters

@Database(
    entities = [
        ContactEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MessageReaction::class,
        PhoneNumber::class,
        RecipientEntity::class,
        ContactGroup::class,
        BlockedNumber::class,
        SyncLog::class,
        ConversationRecipientCrossRef::class
    ],
    version = 3,
    //autoMigrations = [AutoMigration(from = 1, to = 2)],
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