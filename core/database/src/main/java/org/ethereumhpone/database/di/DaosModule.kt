package org.ethereumhpone.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.database.MessengerDatabase
import org.ethereumhpone.database.dao.BlockingDao
import org.ethereumhpone.database.dao.ContactDao
import org.ethereumhpone.database.dao.ConversationDao
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.dao.PhoneNumberDao
import org.ethereumhpone.database.dao.ReactionDao
import org.ethereumhpone.database.dao.RecipientDao
import org.ethereumhpone.database.dao.SyncLogDao

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    @Provides
    fun provideBlockingDao(
        database: MessengerDatabase
    ): BlockingDao = database.blockingDao

    @Provides
    fun provideContactDao(
        database: MessengerDatabase
    ): ContactDao = database.contactDao

    @Provides
    fun provideConversationDao(
        database: MessengerDatabase
    ): ConversationDao = database.conversationDao

    @Provides
    fun provideMessageDao(
        database: MessengerDatabase
    ): MessageDao = database.messageDao

    @Provides
    fun providePhoneNumberDao(
        database: MessengerDatabase
    ): PhoneNumberDao = database.phoneNumberDao

    @Provides
    fun provideRecipientDao(
        database: MessengerDatabase
    ): RecipientDao = database.recipientDao

    @Provides
    fun provideSyncLogDao(
        database: MessengerDatabase
    ): SyncLogDao =  database.syncLogDao

    @Provides
    fun provideReactionDao(
        database: MessengerDatabase
    ): ReactionDao = database.reactionDao
}