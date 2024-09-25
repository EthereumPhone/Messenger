package org.ethereumhpone.database

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import org.ethereumhpone.database.model.SyncLog


internal object DatabaseMigrations {


    @DeleteTable.Entries(
        DeleteTable(
            tableName = "synclog"
        ),
        DeleteTable(
            tableName = "conversation"
        )
    )
    class Schema1to2: AutoMigrationSpec

}