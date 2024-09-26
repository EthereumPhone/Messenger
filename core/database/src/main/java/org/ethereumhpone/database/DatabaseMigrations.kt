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
    }
}