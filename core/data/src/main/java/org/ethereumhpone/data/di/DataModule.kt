package org.ethereumhpone.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.data.manager.PermissionManagerImpl
import org.ethereumhpone.data.mapper.ContactCursorImpl
import org.ethereumhpone.data.mapper.ContactGroupCursorImpl
import org.ethereumhpone.data.mapper.ContactGroupMemberCursorImpl
import org.ethereumhpone.data.mapper.ConversationCursorImpl
import org.ethereumhpone.data.mapper.ImageCursorImpl
import org.ethereumhpone.data.mapper.MessageCursorImpl
import org.ethereumhpone.data.mapper.PartCursorImpl
import org.ethereumhpone.data.mapper.RecipientCursorImpl
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.mapper.ContactCursor
import org.ethereumhpone.domain.mapper.ContactGroupCursor
import org.ethereumhpone.domain.mapper.ContactGroupMemberCursor
import org.ethereumhpone.domain.mapper.ConversationCursor
import org.ethereumhpone.domain.mapper.ImageCursor
import org.ethereumhpone.domain.mapper.MessageCursor
import org.ethereumhpone.domain.mapper.PartCursor
import org.ethereumhpone.domain.mapper.RecipientCursor

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideConversationCursor(mapper: ConversationCursorImpl): ConversationCursor = mapper
    @Provides
    fun provideRecipientCursor(mapper: RecipientCursorImpl): RecipientCursor = mapper
    @Provides
    fun provideContactCursor(mapper: ContactCursorImpl): ContactCursor = mapper
    @Provides
    fun provideContactGroupCursor(mapper: ContactGroupCursorImpl): ContactGroupCursor = mapper
    @Provides
    fun provideContactGroupMemberCursor(mapper: ContactGroupMemberCursorImpl): ContactGroupMemberCursor = mapper
    @Provides
    fun provideMessageCursor(mapper: MessageCursorImpl): MessageCursor = mapper
    @Provides
    fun providePartCursor(mapper: PartCursorImpl): PartCursor = mapper
    @Provides
    fun provideImageCursor(mapper: ImageCursorImpl): ImageCursor = mapper
    @Provides
    fun providePhoneNumberUtils(@ApplicationContext appContext: Context): PhoneNumberUtils = PhoneNumberUtils(appContext) // Assuming a default constructor is available
    @Provides
    fun providePermissionManager(permissionManagerImpl: PermissionManagerImpl): PermissionManager = permissionManagerImpl
}