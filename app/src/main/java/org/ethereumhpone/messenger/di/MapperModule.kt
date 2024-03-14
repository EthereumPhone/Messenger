package org.ethereumhpone.messenger.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.data.mapper.RecipientCursorImpl
import org.ethereumhpone.domain.mapper.ConversationCursor
import org.ethereumhpone.domain.mapper.RecipientCursor


@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    fun provideConversationCursor(cursor: ConversationCursor): ConversationCursor = cursor

    @Provides
    fun provideRecipientCursor(cursor: RecipientCursorImpl): RecipientCursor = cursor


//    @Provides
//    fun provideContactCursor(): ContactCursor = ContactCursorImpl() // Assuming ContactCursorImpl is an implementation

}