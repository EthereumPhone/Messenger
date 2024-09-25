package org.ethereumhpone.database

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ethereumhpone.database.model.SyncLog


internal object DatabaseMigrations {


    @DeleteTable.Entries(
        DeleteTable(
            tableName = "synclog"
        )
    )
    class Schema1to2: AutoMigrationSpec

}




val migration1To2 = object: Migration(1,2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //migrate convo
        database.execSQL("ALTER TABLE conversation ADD COLUMN isUnknown INTEGER NOT NULL DEFAULT 0")

        //migrate message:
        database.execSQL("DROP TABLE IF EXISTS message")
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS message (
                id INTEGER PRIMARY KEY NOT NULL,
                threadId INTEGER NOT NULL,
                contentId INTEGER NOT NULL,
                address TEXT NOT NULL,
                boxId INTEGER NOT NULL,
                type TEXT NOT NULL,
                date INTEGER NOT NULL,
                dateSent INTEGER NOT NULL,
                seen INTEGER NOT NULL,
                read INTEGER NOT NULL,
                locked INTEGER NOT NULL,
                subId INTEGER NOT NULL,
                body TEXT NOT NULL,
                errorCode INTEGER NOT NULL,
                deliveryStatus INTEGER NOT NULL,
                attachmentTypeString TEXT NOT NULL,
                attachmentType TEXT NOT NULL,
                mmsDeliveryStatusString TEXT NOT NULL,
                readReportString TEXT NOT NULL,
                errorType INTEGER NOT NULL,
                messageSize INTEGER NOT NULL,
                messageType INTEGER NOT NULL,
                mmsStatus INTEGER NOT NULL,
                subject TEXT NOT NULL,
                textContentType TEXT NOT NULL,
                parts TEXT NOT NULL,
                UNIQUE(threadId) ON CONFLICT IGNORE
            )
        """.trimIndent())

        // migrate mmsPart

    }
}