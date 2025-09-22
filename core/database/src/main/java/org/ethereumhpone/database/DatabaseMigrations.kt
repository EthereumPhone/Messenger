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