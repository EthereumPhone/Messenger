package org.ethereumhpone.messenger.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.database.MessengerDatabase
import org.ethereumhpone.database.dao.BlockingDao
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.RecipientDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): MessengerDatabase =
        Room.databaseBuilder(appContext, MessengerDatabase::class.java, "messaging_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideConversationDao(database: MessengerDatabase): ConversationDao = database.conversationDao
    @Provides
    fun provideContactDao(database: MessengerDatabase): ContactDao = database.contactDao

    @Provides
    fun provideMessageDao(database: MessengerDatabase): MessageDao = database.messageDao

    @Provides
    fun provideRecipientDao(database: MessengerDatabase): RecipientDao = database.recipientDao
    @Provides
    fun provideBlockingDao(database: MessengerDatabase): BlockingDao = database.blockingDao


}

// Add similar provides methods for ContactDao, RecipientDao, and MessageDao
