package org.ethereumhpone.database

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ethereumhpone.database.model.SyncLog


internal object DatabaseMigrations {

}




val migration1To2 = object: Migration(1,2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        //clear synclog
        db.execSQL("DELETE FROM SyncLog")





    }
}

val migration3To4 = object: Migration(3,4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add soft-delete column to conversation
        db.execSQL("ALTER TABLE conversation ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
        // Create index expected by @ColumnInfo(index = true)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_conversation_deleted ON conversation(deleted)")
    }
}

val migration4To5 = object: Migration(4,5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add cutoff timestamp for hiding older messages
        db.execSQL("ALTER TABLE conversation ADD COLUMN hideBefore INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_conversation_hideBefore ON conversation(hideBefore)")
    }
}

val migration5To6 = object: Migration(5,6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add isGroup flag for group conversations
        db.execSQL("ALTER TABLE conversation ADD COLUMN isGroup INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_conversation_isGroup ON conversation(isGroup)")
        // Add imageUrl for group conversation images
        db.execSQL("ALTER TABLE conversation ADD COLUMN imageUrl TEXT DEFAULT NULL")
    }
}

val migration6To7 = object: Migration(6,7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add transaction request support to messages
        db.execSQL("ALTER TABLE message ADD COLUMN transactionRequest TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE message ADD COLUMN transactionStatus TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE message ADD COLUMN transactionHash TEXT DEFAULT NULL")
    }
}

val migration7To8 = object: Migration(7,8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add transaction reference support to messages (for completed transactions)
        db.execSQL("ALTER TABLE message ADD COLUMN transactionReference TEXT DEFAULT NULL")
    }
}