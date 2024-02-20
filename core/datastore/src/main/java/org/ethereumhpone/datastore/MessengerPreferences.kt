package org.ethereumhpone.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.map

import org.ethereumhpone.domain.model.DarkThemeConfig
import org.ethereumhpone.domain.model.UserData
import javax.inject.Inject

class MessengerPreferences @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>
) {
    val prefs = userPreferences.data
        .map {
            UserData(
                canUseSubId = it.canUseSubId,
                signature = it.signature,
                longAsMms = it.longAsMms,
                mmsSize = it.mmsSize,
                delivery = it.delivery,
                shouldHideOnboarding = it.shouldHideOnboarding,
                darkThemeConfig = when(it.darkThemeConfig) {
                    null,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED,
                    DarkThemeConfigProto.UNRECOGNIZED,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM,
                        ->
                            DarkThemeConfig.FOLLOW_SYSTEM
                    DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                            -> DarkThemeConfig.LIGHT
                    DarkThemeConfigProto.DARK_THEME_CONFIG_DARK ->
                        DarkThemeConfig.DARK
                },

            )
        }


    suspend fun setCanUseSubId(canUseSubId: Boolean) {
        userPreferences.updateData {
            it.copy {
                this.canUseSubId = canUseSubId
            }
        }
    }

    suspend fun setSignature(signature: String) {
        userPreferences.updateData {
            it.copy {
                this.signature = signature
            }
        }
    }

    suspend fun setDelivery(delivery: Boolean) {
        userPreferences.updateData {
            it.copy {
                this.delivery = delivery
            }
        }
    }

    suspend fun setLongAsMms(longAsMms: Boolean) {
        userPreferences.updateData {
            it.copy {
                this.longAsMms = longAsMms
            }
        }
    }

    suspend fun setMmsSize(mmsSize: Int) {
        userPreferences.updateData {
            it.copy {
                this.mmsSize = mmsSize
            }
        }
    }

    suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
        userPreferences.updateData {
            it.copy {
                this.shouldHideOnboarding = shouldHideOnboarding
            }
        }
    }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferences.updateData {
            it.copy {
                this.darkThemeConfig = when (darkThemeConfig) {
                    DarkThemeConfig.FOLLOW_SYSTEM ->
                        DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                    DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                    DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                }
            }
        }
    }
}