package org.ethereumhpone.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messenger.terminalsdk.TerminalSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.navigation.AddressesArgs
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.manager.WalletContentProvider
import org.ethereumhpone.domain.model.UserData
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.usecase.GetAllTokensUseCase
import org.ethereumphone.dgenlibrary.components.TransactionStatus
import org.ethereumphone.walletsdk.WalletSDK
import org.ethereumphone.model.TokenAsset
//import org.kethereum.ens.ENS
import javax.inject.Inject


@HiltViewModel
class ChatSendViewModel @SuppressLint("StaticFieldLeak")
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    private val activeConversationManager: ActiveConversationManager,
    private var walletSDK: WalletSDK,
    private val terminalSDK: TerminalSDK?,
    private val _getAllTokensUseCase: GetAllTokensUseCase,
    private val walletContentProvider: WalletContentProvider,
    @ApplicationContext private val context: Context
): ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ownedTokens = walletContentProvider.getAllOwnedTokens()
                ownedTokens.forEach { token ->
                    Log.d("ChatSendViewModel", "OwnedToken: $token")
                }
            } catch (e: Exception) {
                Log.e("ChatSendViewModel", "Error querying owned tokens", e)
            }
        }
    }


    // nav arguments
    private val threadId = ThreadIdArgs(savedStateHandle).threadId ?: ""
    private val addresses = AddressesArgs(savedStateHandle).addresses ?: emptyList()

    // conversation state
    val conversation = conversationRepository.getConversation(threadId)
        .map { conversation ->
            if (conversation == null) {
                // TODO add fallback if convo does not exist?
                ConversationUiState.Loading
            } else {
                activeConversationManager.setActiveConversation(conversation.id)
                ConversationUiState.Success(conversation = conversation)
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = ConversationUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )


    val recipients = conversationRepository.getConversation(threadId)
        .map { conversation ->
            if (conversation != null) {
                RecipientUiState.Success(conversation.recipients)
            } else {
                RecipientUiState.Error
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = RecipientUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )


    // contacts state
    val contacts: StateFlow<List<ContactEntity>> = contactRepository.getContacts()
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5_000)
        )



    // Add send transaction trigger state
    private val _sendTransactionTriggered = MutableStateFlow(false)
    val sendTransactionTriggered: StateFlow<Boolean> = _sendTransactionTriggered.asStateFlow()

    private val _transactionStatus = MutableStateFlow<TransactionStatus?>(null)
    val transactionStatus: StateFlow<TransactionStatus?> = _transactionStatus.asStateFlow()


    val currentChainId: StateFlow<Int> = flow {
        while (true) {
            val chainId = walletSDK.getChainId()
            emit(chainId)
            delay(400)
        }
    }.flowOn(Dispatchers.IO) // Ensures the flow runs on the IO dispatcher
        .stateIn(
            scope = viewModelScope,
            initialValue = 1,
            started = SharingStarted.WhileSubscribed(5_000)
        )


    val tokenAssetState: StateFlow<AssetsUiState> = flow {
        // Continuously poll owned tokens; in future this can be replaced by a content-observer pattern
        while (true) {
            val ownedTokens = walletContentProvider.getAllOwnedTokens()
            emit(ownedTokens)
            delay(2_000) // refresh every 2s
        }
    }
        .map { ownedTokens ->
            // Convert OwnedToken -> TokenAsset with balance scaled by decimals
            val tokenAssets = ownedTokens.mapNotNull { ownedToken ->
                try {
                    val scaledBalance = ownedToken.balance.toDouble()
                    TokenAsset(
                        address = ownedToken.contractAddress,
                        chainId = ownedToken.chainId,
                        symbol = ownedToken.symbol,
                        name = ownedToken.name,
                        balance = scaledBalance,
                        decimals = ownedToken.decimals,
                        logoUrl = ownedToken.logo ?: "",
                        swappable = ownedToken.swappable,
                        price = ownedToken.price
                    )
                } catch (e: Exception) {
                    null // skip token if conversion fails
                }
            }

            val filteredTokens = tokenAssets
                .filter { it.balance > 0 }
                .filter { token ->
                    val name = token.name.lowercase()
                    val symbol = token.symbol.lowercase()
                    val urlPatterns = listOf(
                        "http://", "https://", "www.",
                        ".com", ".io", ".org", ".net", ".xyz",
                        "/", "t.me", "telegram", "twitter", "discord", "t.ly"
                    )
                    urlPatterns.none { pattern ->
                        name.contains(pattern) || symbol.contains(pattern)
                    }
                }

            if (filteredTokens.isEmpty()) {
                AssetsUiState.Empty
            } else {
                AssetsUiState.Success(filteredTokens)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetsUiState.Loading
        )



    override fun onCleared() {
        activeConversationManager.clearActiveConversation()
        // Ensure touch handler is cleaned up synchronously when ViewModel is cleared
        try {
            // Call destroyTouchHandlerSync directly without checking isAvailable
            // since it's a non-suspend function and will handle null cases internally
            terminalSDK?.destroyTouchHandlerSync()
            Log.d("ChatSendViewModel", "Touch handler cleaned up in onCleared")
        } catch (e: Exception) {
            Log.e("ChatSendViewModel", "Error cleaning up touch handler", e)
        }
    }

    /**
     * Function to trigger send transaction from secondary screen
     */
    fun triggerSendTransaction() {
        _sendTransactionTriggered.value = true
    }

    //-----------------------------SENDING--------------------------------

    /**
     * Call this function when the send screen is opened to secondary screen
     */
    fun onScreenOpened() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val result = terminalSDK?.isAvailable() == false
                println("TerminalSDK isAvailable: $result")

                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.displaySend(
                        sendTx = {
                            Log.d("ChatSendViewModel", "Send transaction touched on secondary screen - triggering send transaction")
                            triggerSendTransaction()
                        }
                    )
                    Log.d("ChatSendViewModel", "QR code displayed on secondary screen")
                } else {
                    Log.w("ChatSendViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("ChatSendViewModel", "Error displaying QR code", e)
            }
        }
    }

    /**
     * Call this function when the send screen is closed/navigated away to remove QR code from secondary screen
     */
    fun onScreenClosed() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.removeSend()
                    Log.d("ChatSendViewModel", "Removed send from secondary screen")
                } else {
                    Log.w("ChatSendViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("ChatSendViewModel", "Error removing Send", e)
            }
        }
    }



}


sealed interface AssetsUiState {
    object Loading : AssetsUiState
    object Error : AssetsUiState
    object Empty : AssetsUiState
    data class Success(
        val assets: List<TokenAsset>
    ) : AssetsUiState
}

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}

