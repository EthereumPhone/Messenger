package org.ethereumhpone.database

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ethereumhpone.database.model.SyncLog


internal object DatabaseMigrations {

}




val migration1To2 = object: Migration(1,2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //clear synclog
        database.execSQL("DELETE FROM SyncLog")

        //convo
        database.execSQL("ALTER TABLE conversation ADD COLUMN isUnknown INTEGER NOT NULL DEFAULT 0")

        //recipient
        database.execSQL("ALTER TABLE recipient ADD COLUMN inboxId TEXT NOT NULL DEFAULT ''")

        //message
        database.execSQL("DELETE FROM message")
        // Add columns to the message table
        database.execSQL("ALTER TABLE message ADD COLUMN clientAddress TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE message ADD COLUMN replyReference TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE message ADD COLUMN seenDate INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE message ADD COLUMN xmtpDeliveryStatus INTEGER NOT NULL DEFAULT 1")

    }
}