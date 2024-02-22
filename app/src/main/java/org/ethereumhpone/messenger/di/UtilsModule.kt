package org.ethereumhpone.messenger.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumhpone.data.util.PhoneNumberUtils

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    fun providePhoneNumberUtils(@ApplicationContext appContext: Context): PhoneNumberUtils =
        PhoneNumberUtils(appContext) // Assuming a default constructor is available
}