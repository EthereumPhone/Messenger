package org.ethereumhpone.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ethereumhpone.database.dao.BlockingDao
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.model.BlockedNumber
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.ContactGroup
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.database.util.Converters

@Database(
    entities = [
        Contact::class,
        Conversation::class,
        Message::class,
        MmsPart::class,
        PhoneNumber::class,
        Recipient::class,
        ContactGroup::class,
        BlockedNumber::class
    ],
    version = 1
)

@TypeConverters(Converters::class)
abstract class MessengerDatabase: RoomDatabase() {
    abstract val messageDao: MessageDao
    abstract val conversationDao: ConversationDao
    abstract val contactDao: ContactDao
    abstract val recipientDao: RecipientDao
    abstract val blockingDao: BlockingDao

}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `contact_group` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ...)")
    }
}