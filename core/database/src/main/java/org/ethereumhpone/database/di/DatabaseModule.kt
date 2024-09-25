package org.ethereumhpone.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.database.MessengerDatabase
import org.ethereumhpone.database.migration1To2
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
        .addMigrations(migration1To2)
        .fallbackToDestructiveMigration()
        .build()
}