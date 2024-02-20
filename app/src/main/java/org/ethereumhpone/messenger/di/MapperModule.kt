package org.ethereumhpone.messenger.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.data.repository.ConversationCursorImpl
import org.ethereumhpone.data.repository.RecipientCursorImpl
import org.ethereumhpone.domain.mapper.ConversationCursor
import org.ethereumhpone.domain.mapper.RecipientCursor

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    fun provideConversationCursor(): ConversationCursor = ConversationCursorImpl()// Initialize your ConversationCursor here

    @Provides
    fun provideRecipientCursor(): RecipientCursor = RecipientCursorImpl()// Initialize your RecipientCursor here
}