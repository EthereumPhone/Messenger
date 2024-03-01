package org.ethereumhpone.messenger.di



import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.migrations.SharedPreferencesMigration
import com.core.datastore.proto.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton
import org.ethereumhpone.datastore.UserPreferencesSerializer
import java.util.prefs.Preferences



private const val USER_PREFERENCES_NAME = "user_preferences"
private const val DATA_STORE_FILE_NAME = "user_prefs.pb"
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
        userPreferencesSerializer: UserPreferencesSerializer // Hilt will provide this
    ): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            serializer = userPreferencesSerializer,
            produceFile = { context.dataStoreFile(DATA_STORE_FILE_NAME) },
            corruptionHandler = null,
            scope = CoroutineScope(IO + SupervisorJob())
        )
    }

}