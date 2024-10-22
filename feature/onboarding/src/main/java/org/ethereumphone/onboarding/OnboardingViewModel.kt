package org.ethereumphone.onboarding

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.EthOSSigningKey
import org.ethereumhpone.data.manager.KeyUtil
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.model.XMTPPrivateKeyHandler
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumphone.walletsdk.WalletSDK
import org.xmtp.android.library.Client
import org.xmtp.android.library.messages.PrivateKeyBundleV1Builder
import uniffi.xmtpv3.GenericException
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val walletSDK: WalletSDK,
    @ApplicationContext private val context: Context,
    private val messengerPreferences: MessengerPreferences,
    private val xmtpClientManager: XmtpClientManager,
    private val syncRepository: SyncRepository
): ViewModel() {

    private val _syncState = MutableStateFlow(false)
    val syncState: StateFlow<Boolean> = _syncState

    fun generateXMTP() {
        Log.d("swag","swwag ")

        viewModelScope.launch {
            _syncState.value = true

            try {
                val keyManager = KeyUtil(context)
                var keys = keyManager.retrieveKey(walletSDK.getAddress())

                if(keys == null) {
                    Client().create(
                        EthOSSigningKey(walletSDK),
                        XmtpClientManager.clientOptions(context, walletSDK.getAddress())
                    ).apply {
                        keyManager.storeKey(walletSDK.getAddress(), PrivateKeyBundleV1Builder.encodeData(privateKeyBundleV1))
                        keys = PrivateKeyBundleV1Builder.encodeData(privateKeyBundleV1)
                    }
                }
                xmtpClientManager.createClient(keys!! , context)
            } catch (exception: GenericException.ApiException) {
                exception.localizedMessage
            }

            _syncState.value = false
        }
    }

    fun startFirstSync() {
        viewModelScope.launch {
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
            syncRepository.syncMessages()
            syncRepository.startStreamAllMessages()

        }
    }

    fun hideOnboarding() {
        viewModelScope.launch {
            messengerPreferences.setShouldHideOnboarding(true)
        }
    }
}