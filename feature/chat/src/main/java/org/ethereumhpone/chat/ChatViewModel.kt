package org.ethereumhpone.chat


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ezvcard.Ezvcard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumhpone.chat.components.isEthereumAddress
import org.ethereumhpone.chat.navigation.AddressesArgs
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MediaRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.usecase.SendMessage
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.Message
import org.ethereumphone.model.Reaction
import org.ethereumphone.model.Recipient
import org.ethereumphone.walletsdk.WalletSDK
import org.kethereum.model.Address
import org.kethereum.rpc.EthereumRPC
import org.kethereum.rpc.HttpEthereumRPC
//import org.kethereum.ens.ENS
import java.math.BigDecimal
import java.math.BigInteger
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
    private val context: Context
): ViewModel() {

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
                ConversationUiState.Success(conversation = conversation)
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = ConversationUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    val messagesState = messageRepository.getMessages(threadId = threadId)
        .map {
            if (it.isEmpty()) {
                MessageUiState.Success(emptyList())
            } else {
                MessageUiState.Success(it)
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = MessageUiState.Loading,
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

    // media
    val media: StateFlow<List<Uri>> = mediaRepository.getImages()
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5_000)
        )


    private val _attachments = MutableStateFlow<Set<Attachment>>(emptySet())
    val attachments: StateFlow<Set<Attachment>> = _attachments


    private val _selectedMessages = MutableStateFlow<MutableList<Message?>>(mutableListOf())
    val selectedMessages: StateFlow<MutableList<Message?>> = _selectedMessages




    fun removeSelectedMessage(message: Message) {
        _selectedMessages.value.remove(message)
    }
    fun addSelectedMessage(message: Message) {
        _selectedMessages.value.add(message)
    }


    fun parseContact(contactEntity: ContactEntity) {
        toggleAttachment(Attachment.Contact(contactEntity.lookupKey, contactEntity.photoUri?.toUri(), getVCard(contactEntity.lookupKey)!!))
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



    fun deleteMessage(id: String){
        viewModelScope.launch(Dispatchers.IO) {
            messageRepository.deleteMessage(id)
        }
    }



    //TODO: Make suspend

    /*
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
     */


    fun isAddress(address: String): Boolean {
        return isEthereumAddress(address)
    }


    //TODO: Make suspend
    fun chainToApiKey(networkName: String): String = BuildConfig.ALCHEMY_API

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


    fun sendReaction(
        reactionUri: String
    ) {
        viewModelScope.launch { 
            val reaction = Reaction(
                id = TODO(),
                messageId = TODO(),
                reactionSchema = TODO(),
                content = TODO(),
                senderInboxId = TODO()
            )
            
        }
    }

    fun sendMessage(
        messageBody: String = "",
    ) {
        viewModelScope.launch {
            sendMessageUseCase(
                threadId = threadId,
                body = messageBody,
                replyReference = if (selectedMessages.value.size == 1) selectedMessages.value.getOrNull(0)?.id else null,
                attachments = attachments.value.toList(),
                reaction = null
            )
        }
    }

    //TODO: add specific contact selection
    fun onOpenContact() {
        /*
        recipientState.value?.contact?.lookupKey?.let {
            println("Opening contact with lookup key: $it")
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                it
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = lookupUri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
         */

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

sealed interface MessageUiState {
    object Loading : MessageUiState
    data class Success(val messageEntities: List<Message>): MessageUiState
}

sealed interface ConversationUiState {
    object Loading : ConversationUiState
    data class Success(val conversation: Conversation): ConversationUiState
}

sealed interface RecipientUiState {
    object Loading : RecipientUiState
    object Error : RecipientUiState
    data class Success(val recipients: List<Recipient>): RecipientUiState

}





