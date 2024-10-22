package org.ethereumhpone.messenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.model.UserData
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    messengerPreferences: MessengerPreferences
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = messengerPreferences.prefs.map {
        MainActivityUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )






}



sealed interface MainActivityUiState {
    data object Loading: MainActivityUiState
    data class Success(val userData: UserData): MainActivityUiState
}