package org.ethereumphone.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.datastore.MessengerPreferences
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val messengerPreferences: MessengerPreferences
) : ViewModel() {
    val settingsUiState: StateFlow<SettingsUiState> =
        messengerPreferences.prefs
            .map { userData ->
                SettingsUiState.Success(
                    settings = UserEditableSettings(
                        ringtone = userData.ringTone,
                        useXmtp = userData.useXmtp
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState.Loading
            )



    fun updateUseXmtp(useXmtp: Boolean) {
        viewModelScope.launch {
            messengerPreferences.setUseXmtp(useXmtp)
        }
    }

    fun updateRingtone(ringtone: String) {
        viewModelScope.launch {
            messengerPreferences.setRingTone(ringtone)
        }

    }
}







data class UserEditableSettings(
    val ringtone: String,
    val useXmtp: Boolean
)

sealed interface SettingsUiState {
    data object Loading: SettingsUiState
    data class Success(val settings: UserEditableSettings) : SettingsUiState
}