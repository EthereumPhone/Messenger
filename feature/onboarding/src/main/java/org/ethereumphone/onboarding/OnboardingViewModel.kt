package org.ethereumphone.onboarding

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.NetworkManager
import org.ethereumhpone.domain.model.UserData
import org.ethereumhpone.domain.model.XMTPPrivateKeyHandler
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject
import android.content.Intent
import androidx.core.content.ContextCompat
import org.ethereumhpone.data.services.XmtpMessageStreamService
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val walletSDK: WalletSDK,
    @ApplicationContext private val context: Context,
    private val messengerPreferences: MessengerPreferences,
    private val xmtpClientManager: XmtpClientManager,
    private val syncRepository: SyncRepository,
    private val networkManager: NetworkManager
): ViewModel() {

    // Expose network connectivity status so the UI can react in real-time
    val isOnline: Flow<Boolean> = networkManager.isOnline

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Loading)
    val syncState: StateFlow<SyncState> = _syncState

    fun generateXMTP() {
        viewModelScope.launch(Dispatchers.IO) {
            // Observe network connectivity first
            networkManager.isOnline.collectLatest { isOnline ->
                if (!isOnline) {
                    _syncState.value = SyncState.Error("No internet connection found")
                    return@collectLatest
                }

                try {
                    // Create the XMTP client – the library will trigger any required
                    // wallet signature prompts ("Authenticate to inbox") the first time
                    // it needs to generate or load keys.
                    xmtpClientManager.createClient(walletSDK, context)

                    // Wait until the client reports it is ready before moving on
                    xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }

                    _syncState.value = SyncState.Success
                } catch (exception: Exception) {
                    _syncState.value = SyncState.Error(exception.localizedMessage ?: "Error")
                }
            }
        }
    }

    fun startFirstSync() {
        viewModelScope.launch(Dispatchers.IO) {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            syncRepository.syncMessages()
            syncRepository.syncXmtp()
            
            // Start the XMTP stream service
            //val serviceIntent = Intent(context, XmtpMessageStreamService::class.java)
            //ContextCompat.startForegroundService(context, serviceIntent)
            //Log.d("OnboardingViewModel", "Started XMTP stream service after first sync")
        }
    }

    fun hideOnboarding(useXmtp: Boolean) {
        viewModelScope.launch {
            // Persist the user's XMTP preference
            messengerPreferences.setUseXmtp(useXmtp)

            // Only hide the onboarding screen permanently once the user has completed
            // the XMTP setup. If they skipped, keep showing it on next launch.
            messengerPreferences.setShouldHideOnboarding(useXmtp)
        }
    }
}

sealed interface SyncState {
    data object Loading: SyncState
    data object Success: SyncState
    data class Error(val error: String): SyncState
}