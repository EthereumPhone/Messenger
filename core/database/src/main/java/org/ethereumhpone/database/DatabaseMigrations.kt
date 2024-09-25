package org.ethereumhpone.database

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ethereumhpone.database.model.SyncLog


internal object DatabaseMigrations {


    @DeleteTable.Entries(
        DeleteTable(
            tableName = "synclog"
        )
    )
    class Schema1to2: AutoMigrationSpec {

        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            super.onPostMigrate(db)
            db.execSQL("UPDATE conversation SET isUnknown = 0 WHERE isUnknown IS NULL")
        }
    }

}