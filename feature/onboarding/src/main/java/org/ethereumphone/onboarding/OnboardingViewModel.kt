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
import org.ethereumhpone.data.manager.EOAWallet
import org.ethereumhpone.data.manager.KeyUtil
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.NetworkManager
import org.ethereumhpone.domain.model.UserData
import org.ethereumhpone.domain.model.XMTPPrivateKeyHandler
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.walletsdk.WalletSDK
import org.xmtp.android.library.Client
import uniffi.xmtpv3.GenericException
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val walletSDK: WalletSDK,
    @ApplicationContext private val context: Context,
    private val messengerPreferences: MessengerPreferences,
    private val xmtpClientManager: XmtpClientManager,
    private val syncRepository: SyncRepository,
    private val networkManager: NetworkManager
): ViewModel() {


    private val _syncState = MutableStateFlow<SyncState>(SyncState.Loading)
    val syncState: StateFlow<SyncState> = _syncState

    fun generateXMTP() {


        viewModelScope.launch(Dispatchers.IO) {
            networkManager.isOnline.collectLatest { isOnline ->
                if (!isOnline) {
                    _syncState.value = SyncState.Error("No internet connection found")
                    return@collectLatest
                }

                try {
                    val address = walletSDK.getAddress()
                    val keyManager = KeyUtil(context)
                    var keys = keyManager.retrieveKey(address)

                    // generate keys
                    if(keys == null) {
                        try {
                            Client.create(
                                account = EOAWallet(walletSDK, address),
                                options = XmtpClientManager.clientOptions(context, address)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        keyManager.storeKey(walletSDK.getAddress(), "set")
                    }

                    xmtpClientManager.createClient(walletSDK , context)
                } catch (exception: Exception) {
                    _syncState.value = SyncState.Error(exception.localizedMessage ?: "Error")
                }
                _syncState.value = SyncState.Success
            }
        }
    }

    fun startFirstSync() {
        viewModelScope.launch(Dispatchers.IO) {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            syncRepository.syncMessages()
            syncRepository.startStreamAllMessages()

        }
    }

    fun hideOnboarding(useXmtp: Boolean) {
        viewModelScope.launch {
            messengerPreferences.setUseXmtp(useXmtp)
            messengerPreferences.setShouldHideOnboarding(true)
        }
    }
}

sealed interface SyncState {
    data object Loading: SyncState
    data object Success: SyncState
    data class Error(val error: String): SyncState
}