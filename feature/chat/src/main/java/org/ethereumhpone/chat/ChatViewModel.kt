package org.ethereumhpone.chat


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ezvcard.Ezvcard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumhpone.chat.components.isEthereumAddress
import org.ethereumhpone.chat.navigation.AddressesArgs
import org.ethereumhpone.chat.navigation.ThreadIdArgs
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.domain.manager.ActiveConversationManager
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MediaRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.domain.usecase.SendMessage
import org.ethereumhpone.domain.usecase.SendMessageWithConversation
import org.ethereumphone.dgenlibrary.components.TransactionStatus
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
import org.xmtp.android.library.codecs.ContentTypeReadReceipt
import org.xmtp.android.library.codecs.ReadReceipt
import org.xmtp.android.library.SendOptions


@HiltViewModel
class ChatViewModel @SuppressLint("StaticFieldLeak")
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    private val activeConversationManager: ActiveConversationManager,
    mediaRepository: MediaRepository,
    private val messageRepository: MessageRepository,
    private val sendMessageUseCase: SendMessageWithConversation,
    private var walletSDK: WalletSDK,
    private val context: Context,
    private val xmtpClientManager: XmtpClientManager
): ViewModel() {


    // nav arguments
    private val threadId = ThreadIdArgs(savedStateHandle).threadId ?: ""
    private val addresses = AddressesArgs(savedStateHandle).addresses ?: emptyList()
    private lateinit var xmtpConversation: org.xmtp.android.library.Conversation

    // Track the last message id for which we have already sent a read-receipt. This prevents
    // duplicate receipts when the same message (or the user’s own message) triggers multiple
    // DB updates / emissions.
    private var lastReadReceiptMessageId: String? = null
    // conversation state
    val conversation = conversationRepository.getConversation(threadId)
        .map { conversation ->
            if (conversation == null) {
                // TODO add fallback if convo does not exist?
                ConversationUiState.Loading
            } else {
                activeConversationManager.setActiveConversation(conversation.id)
                xmtpConversation = xmtpClientManager.client.conversations.findConversation(threadId)!!
                ConversationUiState.Success(conversation = conversation)
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
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
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
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


    private val _selectedMessages = MutableStateFlow<List<Message>>(emptyList())
    val selectedMessages: StateFlow<List<Message>> = _selectedMessages

    private val _selectMode = MutableStateFlow(false)
    val selectMode: StateFlow<Boolean> = _selectMode

    fun toggleSelection(message: Message) {
        _selectedMessages.update { current ->
            if (current.contains(message)) current - message else current + message
        }
        _selectMode.value = _selectedMessages.value.isNotEmpty()
    }

    fun clearSelection() {
        _selectedMessages.value = emptyList()
        _selectMode.value = false
    }

    // Updated individual add/remove helpers to reflect new list type
    fun removeSelectedMessage(message: Message) {
        _selectedMessages.update { it - message }
        _selectMode.value = _selectedMessages.value.isNotEmpty()
    }
    fun addSelectedMessage(message: Message) {
        _selectedMessages.update { it + message }
        _selectMode.value = true
    }

    // Add send transaction trigger state
    private val _sendTransactionTriggered = MutableStateFlow(false)
    val sendTransactionTriggered: StateFlow<Boolean> = _sendTransactionTriggered.asStateFlow()

    private val _transactionStatus = MutableStateFlow<TransactionStatus?>(null)
    val transactionStatus: StateFlow<TransactionStatus?> = _transactionStatus.asStateFlow()




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

    // set conversation
    init {
        viewModelScope.launch(Dispatchers.IO) {
            conversation.collect { state ->
                if (state is ConversationUiState.Success) {
                    activeConversationManager.setActiveConversation(state.conversation.id)
                    // Only send a read-receipt for NEW incoming messages (i.e. messages that are
                    // not authored by the current user) and skip duplicates.
                    val latestMessage = state.conversation.lastMessage
                    if (latestMessage != null && !latestMessage.isMe && latestMessage.id != lastReadReceiptMessageId) {
                        sendReadReceipt(state.conversation.id)
                        lastReadReceiptMessageId = latestMessage.id
                    }
                }
            }
        }

        // Initialise the XMTP conversation once in a background thread so the UI thread stays free.
        viewModelScope.launch(Dispatchers.IO) {
            // Ensure the XMTP client is ready before attempting to find a conversation
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }

            xmtpClientManager.client.conversations.findConversation(threadId)?.let { convo ->
                xmtpConversation = convo
            }
        }
    }

    override fun onCleared() {
        activeConversationManager.clearActiveConversation()
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
        viewModelScope.launch(Dispatchers.IO) {
            sendMessageUseCase(
                xmtpConversation = xmtpConversation,
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

    private fun sendReadReceipt(conversationId: String) {
        // ANR fix
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Wait for client to be ready
                xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }
                
                val conversation = xmtpClientManager.client.conversations.findConversation(conversationId)
                if (conversation != null) {
                    // Send read receipt with current timestamp
                    conversation.send(
                        content = ReadReceipt,
                        options = SendOptions(contentType = ContentTypeReadReceipt)
                    )
                    Log.d("ChatViewModel", "Sent read receipt for conversation: $conversationId")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send read receipt", e)
            }
        }
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





