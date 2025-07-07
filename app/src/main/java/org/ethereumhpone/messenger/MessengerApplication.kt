package org.ethereumhpone.messenger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.ethereumphone.dgenlibrary.SystemColorManager


@HiltAndroidApp
class MessengerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        SystemColorManager.refresh(this)
    }
}