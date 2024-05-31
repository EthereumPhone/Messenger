package org.ethereumhpone.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.data.blocking.MessengerBlockingClient
import org.ethereumhpone.data.manager.ActiveConversationManagerImpl
import org.ethereumhpone.data.manager.KeyManagerImpl
import org.ethereumhpone.data.manager.NotificationManagerImpl
import org.ethereumhpone.data.repository.BlockingRepositoryImpl
import org.ethereumhpone.data.repository.ContactRepositoryImpl
import org.ethereumhpone.data.repository.ConversationRepositoryImpl
import org.ethereumhpone.data.repository.MediaRepositoryImpl
import org.ethereumhpone.data.repository.MessageRepositoryImpl
import org.ethereumhpone.data.repository.SyncRepositoryImpl
import org.ethereumhpone.domain.blocking.BlockingClient
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.manager.KeyManager
import org.ethereumhpone.domain.manager.NotificationManager
import org.ethereumhpone.domain.repository.BlockingRepository
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MediaRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule{

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        conversationRepositoryImpl: ConversationRepositoryImpl
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindBlockingRepository(impl: BlockingRepositoryImpl): BlockingRepository

    @Binds
    @Singleton
    abstract fun bindActiveConversationManager(impl: ActiveConversationManagerImpl): ActiveConversationManager

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    abstract fun provideBlockingClient(message: MessengerBlockingClient): BlockingClient

    @Binds
    abstract fun provideNotificationManager(message: NotificationManagerImpl): NotificationManager

//    @Binds
//    @Singleton
//    abstract fun bindKeyManager(impl: KeyManagerImpl): KeyManager

//    companion object {
//        @JvmStatic @Provides
//        fun provideSyncRepository(impl: SyncRepositoryImpl?): SyncRepository? {
//            return impl
//        }
//        @JvmStatic @Provides
//        fun provideActiveConversationManager(impl: ActiveConversationManagerImpl?): ActiveConversationManager? {
//            return impl
//        }
//    }

}