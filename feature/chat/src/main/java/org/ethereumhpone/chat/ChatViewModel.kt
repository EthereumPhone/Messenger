package org.ethereumhpone.chat


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ezvcard.Ezvcard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumhpone.chat.components.attachments.getDisplayName
import org.ethereumhpone.chat.navigation.AddressesArgs
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.common.compat.TelephonyCompat
import org.ethereumhpone.common.extensions.map
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.mapper.ContactCursor
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
//import org.kethereum.ens.ENS
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.CompletableFuture
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @SuppressLint("StaticFieldLeak")
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    mediaRepository: MediaRepository,
    private val messageRepository: MessageRepository,
    private val sendMessageUseCase: SendMessage,
    private var walletSDK: WalletSDK,
    private val permissionManager: PermissionManager,
    private val context: Context
): ViewModel() {

    // nav arguments
    private val threadId = ThreadIdArgs(savedStateHandle).threadId.toLong()
    private val addresses = AddressesArgs(savedStateHandle).addresses

    // conversation state
    private val conversationState = merge(
        conversationRepository.getConversation(threadId), // initial Conversation
        selectedConversationState(addresses, conversationRepository)
    ).stateIn(
        scope = viewModelScope,
        initialValue = Conversation(),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    // recipients state
    val recipientState = conversationState
        .filterNotNull()
        .map {
            if (it.recipients.isNotEmpty()) {
                it.recipients[0]
            } else { Recipient(address = addresses[0]) }
        }.stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    // message state
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

    // contacts state
    val contacts: StateFlow<List<Contact>> = contactRepository.getContacts()
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5_000)
        )

    // media
    val media: StateFlow<List<Uri>> = mediaRepository.getImages()
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5_000)
        )


    private val _attachments = MutableStateFlow<Set<Attachment>>(emptySet())
    val attachments: StateFlow<Set<Attachment>> = _attachments



    // Write a piece of code that gets the eth balance of address "0x0" and saves it to a state variable
    private val _focusedMessage = MutableStateFlow<Message?>(null)
    val focusedMessage: StateFlow<Message?> = _focusedMessage


    fun parseContact(contact: Contact) {
        toggleAttachment(Attachment.Contact(contact.lookupKey, contact.photoUri?.toUri(), getVCard(contact.lookupKey)!!))
    }


    fun toggleAttachment(attachment: Attachment) {
        _attachments.update { curr ->
            if(curr.contains(attachment)) {
                curr - attachment
            } else {
                curr + attachment
            }
        }
    }


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




    fun callPhone() {
        recipientState.value?.contact?.numbers?.firstOrNull()?.let {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${it.address}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }






    suspend fun getBalance(chainId: Int): Double {
        return withContext(Dispatchers.IO) {
            while(walletSDK.getAddress() == "") {
                delay(20)
            }
            if (walletSDK.getAddress() != "") {
                val rpcUrl = chainIdToRPC(chainId)
                val ethereumRPC: EthereumRPC = HttpEthereumRPC(rpcUrl)
                val weiBalance = ethereumRPC.getBalance(Address(walletSDK.getAddress()))
                weiBalance?.toBigDecimal()?.divide(BigDecimal.TEN.pow(18))?.toDouble() ?: 0.0
            } else {
                0.0
            }
        }
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

    fun updatefocusedMessage(newMessage: Message) {
        _focusedMessage.value = newMessage
    }

    fun deleteMessage(id: Long){
        viewModelScope.launch {
            messageRepository.deleteMessage(id)
        }
    }



    //TODO: Make suspend
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


    //TODO: Make suspend
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

    //TODO: move all to own helper class
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
        else -> "eth-mainnet"
    }

    fun chainIdToReadableName(chainId: Int): String = when(chainId) {
        1 -> "Ethereum Mainnet"
        11155111 -> "Ethereum Sepolia"
        10 -> "Optimism Mainnet"
        42161 -> "Arbitrum Mainnet"
        137 -> "Polygon Mainnet"
        8453 -> "Base Mainnet"
        5 -> "Ethereum Goerli"
        else -> "Ethereum Mainnet"
    }

    fun chainIdToRPC(chainId: Int): String {
        return "https://${chainIdToName(chainId)}.g.alchemy.com/v2/${chainToApiKey(chainIdToName(chainId))}"
    }



    fun sendMessage(messageBody: String) {
        if(!permissionManager.isDefaultSms()) {
            // TODO: add request permission
            return
        }
        if(!permissionManager.hasSendSms()) {
            //TODO: add request permission
            return
        }

        val subId = -1 //TODO: Add sunscroptionId logic

        conversationState.value?.let { convo ->

            val addresses = convo.recipients.map { it.address }

            viewModelScope.launch {
                sendMessageUseCase(subId, convo.id, addresses, messageBody, _attachments.value.toList())

                // remove attached items
                _attachments.value = emptySet()
            }
        }
    }



    @SuppressLint("Range")
    private fun getVCard(lookupKey: String): String? {
        val contentResolver: ContentResolver = context.contentResolver
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.DATA15)
        val selection = "${ContactsContract.Data.LOOKUP_KEY} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(lookupKey, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

        val data = contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA15))
            } else {
                null
            }
        }

        val vCardUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
        val inputStream = context.contentResolver.openAssetFileDescriptor(vCardUri, "r")?.createInputStream()
        inputStream?.use { stream ->
            val vcard = Ezvcard.parse(stream).first() // Parse vCard
            vcard.addExtendedProperty("ens", data!!) // Add data15 property
            return Ezvcard.write(vcard).go() // Convert vCard to string
        }
        return null
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
                    0L -> Conversation(0)
                    else -> conversationRepository.getConversation(actualThreadId).first()
                }
            }
        }
    }
}

sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(val messages: List<Message>): MessagesUiState
}





