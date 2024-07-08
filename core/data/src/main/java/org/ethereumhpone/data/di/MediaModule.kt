package org.ethereumhpone.data.di

import android.app.Application
import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object MediaModule {

    @Provides
    fun provideExoPlayer(@ApplicationContext appContext: Context): ExoPlayer = ExoPlayer.Builder(appContext).build()

}