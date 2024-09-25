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
        //clear synclog
        database.execSQL("DELETE FROM SyncLog")

        //migrate convo
        database.execSQL("ALTER TABLE conversation ADD COLUMN isUnknown INTEGER NOT NULL DEFAULT 0")

        //drop message:
        database.execSQL("DROP TABLE IF EXISTS message")

        database.execSQL("DROP TABLE IF EXISTS mms_part")


        // migrate mmsPart

    }
}