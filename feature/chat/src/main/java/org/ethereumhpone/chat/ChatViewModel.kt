package org.ethereumhpone.chat


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumhpone.chat.navigation.AddressesArgs
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.database.util.Converters
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MediaRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.usecase.SendMessage
import org.ethereumphone.walletsdk.WalletSDK
import org.kethereum.model.Address
import org.kethereum.rpc.EthereumRPC
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.lang.Thread.State
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URLDecoder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @SuppressLint("StaticFieldLeak")
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    private val sendMessageUseCase: SendMessage,
    private var walletSDK: WalletSDK,
    private val mediaRepository: MediaRepository,
    private val permissionManager: PermissionManager,

    private val context: Context
): ViewModel() {

    private val threadId = ThreadIdArgs(savedStateHandle).threadId.toLong()
    private val addresses = AddressesArgs(savedStateHandle).addresses

    val conversationState = merge(
        conversationRepository.getConversation(threadId), // initial Conversation
        selectedConversationState(addresses, conversationRepository)
    ).stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val messagesState = conversationState
        .filterNotNull()
        .flatMapLatest {
            messageRepository.getMessages(it.id).map(MessagesUiState::Success)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MessagesUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val attachments: StateFlow<List<Attachment>> = attachmentState(mediaRepository)
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5_000)
        )

    private val _selectedAttachments = MutableStateFlow<Set<Attachment>>(emptySet())
    val selectedAttachments: StateFlow<Set<Attachment>> = _selectedAttachments

    // Write a piece of code that gets the eth balance of address "0x0" and saves it to a state variable
    val currentChainId: StateFlow<Int> = flow {
        while (true) {
            val chainId = walletSDK.getChainId()
            emit(chainId)
            delay(400)
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = 1,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val ethBalance: StateFlow<Double> = currentChainId
        .flatMapLatest { chainId ->
            flow {
                val balance = getBalance(chainId)
                emit(balance)
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = 0.0,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val chainName: StateFlow<String> = currentChainId
        .flatMapLatest {
            flow {
                emit(chainIdToReadableName(currentChainId.value))
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = "",
            started = SharingStarted.WhileSubscribed(5_000)
        )
    /*

     */

    suspend fun getBalance(chainId: Int): Double {
//        return withContext(Dispatchers.IO) {
//            while(walletSDK.getAddress() == "") {
//                delay(20)
//            }
//            if (walletSDK.getAddress() != "") {
//                val rpcUrl = chainIdToRPC(chainId)
//                val ethereumRPC: EthereumRPC = HttpEthereumRPC(rpcUrl)
//                val weiBalance = ethereumRPC.getBalance(Address(walletSDK.getAddress()))
//                weiBalance?.toBigDecimal()?.divide(BigDecimal.TEN.pow(18))?.toDouble() ?: 0.0
//            } else {
//                0.0
//            }
//        }
        return 1.123
    }

    fun increaseByFivePercent(value: BigInteger): BigInteger {
        // Create a BigInteger representation of 105
        val multiplier = BigInteger.valueOf(105)

        // Create a BigInteger representation of 100 for the divisor
        val divisor = BigInteger.valueOf(100)

        // Increase value by 5%
        // Equivalent to: value * 105 / 100
        return value.multiply(multiplier).divide(divisor)
    }

    fun sendEth(amount: Double) {
        val chainIdLocked = currentChainId.value
        val decimalFormat = DecimalFormat("#.###########", DecimalFormatSymbols(Locale.US).apply {
            decimalSeparator = '.'
        }
        ) // Adjust the pattern as needed
        println("Sending ${decimalFormat.format(amount)} ETH on Chain ${chainIdToReadableName(chainIdLocked)}")
        walletSDK = WalletSDK(context, Web3j.build(HttpService(chainIdToRPC(chainIdLocked))))
        recipientState.value?.contact?.ethAddress?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val web3j = Web3j.build(HttpService(chainIdToRPC(chainIdLocked)))

                val gasPrice = increaseByFivePercent(web3j.ethGasPrice().send().gasPrice)

                val hash = walletSDK.sendTransaction(
                    to = it,
                    value = BigDecimal.valueOf(amount).times(BigDecimal.TEN.pow(18)).toString(),
                    data = "",
                    gasAmount = "21000",
                    gasPrice = gasPrice.toString()
                )

                Log.d("ChatViewModel", "Transaction Hash: $hash")
                if (hash.startsWith("0x")) {
                    sendMessage("Sent ${decimalFormat.format(amount)} ETH: ${chainIdToEtherscan(chainIdLocked)}/tx/$hash")
                }
            }
        }
    }

    fun chainToApiKey(networkName: String): String = when(networkName) {
        "eth-mainnet" -> BuildConfig.ETHEREUM_API
        "eth-sepolia" -> BuildConfig.SEPOLIA_API
        "opt-mainnet" -> BuildConfig.OPTIMISM_API
        "arb-mainnet" -> BuildConfig.ARBITRUM_API
        "polygon-mainnet" -> BuildConfig.POLYGON_API
        "base-mainnet" -> BuildConfig.BASE_API
        "eth-goerli" -> BuildConfig.BASE_API
        else -> ""
    }

    fun chainIdToEtherscan(chainId: Int): String = when(chainId) {
        1 -> "https://etherscan.io"
        11155111 -> "https://sepolia.etherscan.io"
        10 -> "https://optimistic.etherscan.io"
        42161 -> "https://arbiscan.io"
        137 -> "https://polygonscan.com"
        8453 -> "https://basescan.org"
        5 -> "https://goerli.etherscan.io"
        else -> ""
    }

    fun chainIdToName(chainId: Int): String = when(chainId) {
        1 -> "eth-mainnet"
        11155111 -> "eth-sepolia"
        10 -> "opt-mainnet"
        42161 -> "arb-mainnet"
        137 -> "polygon-mainnet"
        8453 -> "base-mainnet"
        5 -> "eth-goerli"
        else -> ""
    }

    fun chainIdToReadableName(chainId: Int): String = when(chainId) {
        1 -> "Ethereum Mainnet"
        11155111 -> "Ethereum Sepolia"
        10 -> "Optimism Mainnet"
        42161 -> "Arbitrum Mainnet"
        137 -> "Polygon Mainnet"
        8453 -> "Base Mainnet"
        5 -> "Ethereum Goerli"
        else -> ""
    }

    fun chainIdToRPC(chainId: Int): String {
        return "https://${chainIdToName(chainId)}.g.alchemy.com/v2/${chainToApiKey(chainIdToName(chainId))}"
    }


    val recipientState = conversationState
        .filterNotNull()
        .map {
            if (it.recipients.isNotEmpty()) {
                it.recipients[0]
            } else {

                Recipient(address = addresses[0])
            }
    }.stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5_000)
        )




    fun sendMessage(messageBody: String) {
        //if(!permissionManager.isDefaultSms()) return
        if(!permissionManager.hasSendSms()) {
            //TODO: add request permission
            return
        }

        val subId = -1 //TODO: Add sunscroptionId logic


        // this sends a message for an existing conversation
        conversationState.value?.let { convo ->
            // send message to convo with only one recipient
            if(convo.recipients.size == 1) {
                val address = convo.recipients.map { it.address }

                viewModelScope.launch {
                    sendMessageUseCase(subId, convo.id, address, messageBody, _selectedAttachments.value.toList())
                }
            }
        }

        //TODO: Create a new conversation with one address


    }

    fun toggleSelection(attachment: Attachment) {
        _selectedAttachments.update { curr ->
            if (curr.contains(attachment)) {
                curr - attachment
            } else {
                curr + attachment
            }
        }
    }
}

/**
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun selectedConversationState(
    addresses: List<String>,
    conversationRepository: ConversationRepository
): Flow<Conversation?> {
    if (addresses.isEmpty()) return flowOf(null)

    return conversationRepository.getOrCreateConversation(addresses).flatMapLatest { convo ->
        val threadId = convo?.id ?: 0

        if (threadId > 0) {
            // If the threadID exists in roomDB or ContentProvider
            conversationRepository.getConversation(threadId)
        } else {
            // Otherwise, monitor conversations until one is created
            conversationRepository.getConversations().map {
                val actualThreadId =
                    conversationRepository.getOrCreateConversation(addresses).first()?.id ?: 0

                when (actualThreadId) {
                    0L -> Conversation()
                    else -> conversationRepository.getConversation(actualThreadId).first()
                }
            }
        }
    }
}


private fun attachmentState(
    mediaRepository: MediaRepository,
): Flow<List<Attachment>> {
    return mediaRepository.getImages()
}

sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(val messages: List<Message>): MessagesUiState
}
