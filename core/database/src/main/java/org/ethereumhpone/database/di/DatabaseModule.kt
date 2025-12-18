package org.ethereumhpone.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.database.MessengerDatabase
import org.ethereumhpone.database.migration1To2
import org.ethereumhpone.database.migration3To4
import org.ethereumhpone.database.migration4To5
import org.ethereumhpone.database.migration5To6
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMessengerDatabase(
        @ApplicationContext context: Context,
    ): MessengerDatabase = Room.databaseBuilder(
        context,
        MessengerDatabase::class.java,
        "messenger-database"
    )
        .addMigrations(migration1To2, migration3To4, migration4To5, migration5To6)
        .fallbackToDestructiveMigration()
        .addCallback(clearSyncLogCallback) // Add the callback here
        .build()
}


val clearSyncLogCallback = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // This will run after the database is recreated
        db.execSQL("DELETE FROM SyncLog")
    }
}