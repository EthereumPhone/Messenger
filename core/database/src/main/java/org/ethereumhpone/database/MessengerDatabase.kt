package org.ethereumhpone.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.model.Contact
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
        Recipient::class
    ],
    version = 1
)

@TypeConverters(Converters::class)
abstract class MessengerDatabase: RoomDatabase() {
    abstract val messageDao: MessageDao
}