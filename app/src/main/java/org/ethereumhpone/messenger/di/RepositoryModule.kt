package org.ethereumhpone.messenger.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.data.manager.ActiveConversationManagerImpl
import org.ethereumhpone.data.manager.KeyManagerImpl
import org.ethereumhpone.data.repository.BlockingRepositoryImpl
import org.ethereumhpone.data.repository.ContactRepositoryImpl
import org.ethereumhpone.data.repository.ConversationRepositoryImpl
import org.ethereumhpone.data.repository.MessageRepositoryImpl
import org.ethereumhpone.data.repository.SyncRepositoryImpl
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.manager.KeyManager
import org.ethereumhpone.domain.repository.BlockingRepository
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
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
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindBlockingRepository(impl: BlockingRepositoryImpl): BlockingRepository

    @Binds
    @Singleton
    abstract fun bindActiveConversationManager(impl: ActiveConversationManagerImpl): ActiveConversationManager


//    @Binds
//    @Singleton
//    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepositoryImpl

//    @Provides
//    @Singleton
//    open fun provideSyncRepository(impl: SyncRepositoryImpl?): SyncRepository? {
//        return impl
//    }

//    @Binds
//    @Singleton
//    abstract fun bindActiveConversationManager(impl: ActiveConversationManagerImpl): ActiveConversationManager

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

    companion object {
        @JvmStatic
        @Provides
        @Singleton
        fun provideSyncRepository(impl: SyncRepositoryImpl): SyncRepository {
            // Ensure this method never returns null by not allowing the parameter to be null
            return impl
        }
    }
}