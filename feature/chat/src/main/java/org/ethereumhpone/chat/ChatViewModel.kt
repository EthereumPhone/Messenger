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
import kotlinx.coroutines.flow.combine
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
import org.xmtp.android.library.codecs.ReactionAction
import org.xmtp.android.library.codecs.ReadReceipt
import org.xmtp.android.library.SendOptions
import org.ethereumphone.model.TransactionRequest
import org.ethereumphone.model.TransactionRequestStatus
import org.ethereumphone.model.TransactionReference
import org.ethereumphone.model.TransactionReferenceMetadata
import org.ethereumphone.model.TransactionTypes
import org.ethereumhpone.data.util.GasEstimationHelper
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


@HiltViewModel
class ChatViewModel @SuppressLint("StaticFieldLeak")
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conversationRepository: ConversationRepository,
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
    // conversation state - combines conversation data with XMTP client state
    val conversation = combine(
        conversationRepository.getConversation(threadId),
        xmtpClientManager.clientState
    ) { conversation, clientState ->
        when {
            conversation == null -> {
                // TODO add fallback if convo does not exist?
                ConversationUiState.Loading
            }
            clientState is XmtpClientManager.ClientState.Error -> {
                ConversationUiState.Error("XMTP client error: ${clientState.message}")
            }
            clientState is XmtpClientManager.ClientState.Unknown -> {
                // Client not initialized yet, keep loading state
                ConversationUiState.Loading
            }
            clientState is XmtpClientManager.ClientState.Ready -> {
                activeConversationManager.setActiveConversation(conversation.id)
                try {
                    xmtpConversation = xmtpClientManager.client.conversations.findConversation(threadId)!!
                    ConversationUiState.Success(conversation = conversation)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error finding conversation", e)
                    ConversationUiState.Error(e.message ?: "Error finding conversation")
                }
            }
            else -> ConversationUiState.Loading
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
    
    // Current user's inbox ID for reaction display
    val myInboxId: StateFlow<String> = xmtpClientManager.clientState
        .map { state ->
            if (state == XmtpClientManager.ClientState.Ready) {
                xmtpClientManager.client.inboxId
            } else {
                ""
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = "",
            started = SharingStarted.WhileSubscribed(5_000)
        )

    // Whether the current user can manage group members (is admin or super admin)
    val canManageMembers: StateFlow<Boolean> = xmtpClientManager.clientState
        .map { state ->
            if (state == XmtpClientManager.ClientState.Ready && threadId.isNotBlank()) {
                try {
                    val conv = xmtpClientManager.client.conversations.findConversation(threadId)
                    if (conv?.type == org.xmtp.android.library.Conversation.Type.GROUP) {
                        val group = (conv as org.xmtp.android.library.Conversation.Group).group
                        val myInbox = xmtpClientManager.client.inboxId
                        group.isAdmin(myInbox) || group.isSuperAdmin(myInbox)
                    } else false
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error checking admin status", e)
                    false
                }
            } else false
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            initialValue = false,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    val isSuperAdmin: StateFlow<Boolean> = xmtpClientManager.clientState
        .map { state ->
            if (state == XmtpClientManager.ClientState.Ready && threadId.isNotBlank()) {
                try {
                    val conv = xmtpClientManager.client.conversations.findConversation(threadId)
                    if (conv?.type == org.xmtp.android.library.Conversation.Type.GROUP) {
                        val group = (conv as org.xmtp.android.library.Conversation.Group).group
                        group.isSuperAdmin(xmtpClientManager.client.inboxId)
                    } else false
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error checking super admin status", e)
                    false
                }
            } else false
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            initialValue = false,
            started = SharingStarted.WhileSubscribed(5_000)
        )

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

    /**
     * Clear the transaction status, e.g., when the overlay is dismissed or navigation completes.
     */
    fun clearTransactionStatus() {
        _transactionStatus.value = null
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

    // set conversation
    init {
        viewModelScope.launch(Dispatchers.IO) {
            conversation.collect { state ->
                if (state is ConversationUiState.Success) {
                    activeConversationManager.setActiveConversation(state.conversation.id)
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
        7777777 -> "https://explorer.zora.energy"
        5 -> "https://goerli.etherscan.io"
        56 -> "https://bscscan.com"
        43114 -> "https://snowtrace.io"
        else -> ""
    }

    fun chainIdToName(chainId: Int): String = when(chainId) {
        1 -> "eth-mainnet"
        11155111 -> "eth-sepolia"
        10 -> "opt-mainnet"
        42161 -> "arb-mainnet"
        137 -> "polygon-mainnet"
        8453 -> "base-mainnet"
        7777777 -> "zora-mainnet"
        5 -> "eth-goerli"
        56 -> "bnb-mainnet"
        43114 -> "avax-mainnet"
        else -> {
            Log.w("ChatViewModel", "Unknown chainId: $chainId, falling back to eth-mainnet")
            "eth-mainnet"
        }
    }

    fun chainIdToReadableName(chainId: Int): String = when(chainId) {
        1 -> "Ethereum Mainnet"
        11155111 -> "Ethereum Sepolia"
        10 -> "Optimism Mainnet"
        42161 -> "Arbitrum Mainnet"
        137 -> "Polygon Mainnet"
        8453 -> "Base Mainnet"
        7777777 -> "Zora Mainnet"
        5 -> "Ethereum Goerli"
        56 -> "BNB Chain"
        43114 -> "Avalanche C-Chain"
        else -> "Chain $chainId"
    }

    fun chainIdToRPC(chainId: Int): String {
        return "https://${chainIdToName(chainId)}.g.alchemy.com/v2/${chainToApiKey(chainIdToName(chainId))}"
    }


    /**
     * Send a reaction to a message.
     * @param messageId The ID of the message to react to
     * @param emoji The emoji reaction (e.g., "👍", "❤️", "😂")
     */
    fun sendReaction(
        messageId: String,
        emoji: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!::xmtpConversation.isInitialized) {
                    Log.e("ChatViewModel", "Cannot send reaction: xmtpConversation not initialized")
                    return@launch
                }
                
                messageRepository.sendReaction(
                    xmtpConversation = xmtpConversation,
                    messageId = messageId,
                    emoji = emoji,
                    action = ReactionAction.Added
                )
                Log.d("ChatViewModel", "Reaction sent: $emoji to message $messageId")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send reaction", e)
            }
        }
    }
    
    /**
     * Remove a reaction from a message.
     * @param messageId The ID of the message to remove reaction from
     * @param emoji The emoji reaction to remove
     */
    fun removeReaction(
        messageId: String,
        emoji: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!::xmtpConversation.isInitialized) {
                    Log.e("ChatViewModel", "Cannot remove reaction: xmtpConversation not initialized")
                    return@launch
                }
                
                messageRepository.removeReaction(
                    xmtpConversation = xmtpConversation,
                    messageId = messageId,
                    emoji = emoji
                )
                Log.d("ChatViewModel", "Reaction removed: $emoji from message $messageId")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to remove reaction", e)
            }
        }
    }

    fun sendMessage(
        messageBody: String = "",
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sendMessageUseCase(
                    xmtpConversation = xmtpConversation,
                    threadId = threadId,
                    body = messageBody,
                    replyReference = if (selectedMessages.value.size == 1) selectedMessages.value.getOrNull(0)?.id else null,
                    attachments = attachments.value.toList(),
                    reaction = null
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }

    fun markSeenOnExit() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageRepository.markSeen(threadId)
                // Send a read receipt for latest message in this conversation if it's not authored by me
                val convo = (conversation.value as? ConversationUiState.Success)?.conversation
                val latestMessage = convo?.lastMessage
                if (latestMessage != null && !latestMessage.isMe && latestMessage.id != lastReadReceiptMessageId) {
                    sendReadReceipt(convo.id)
                    lastReadReceiptMessageId = latestMessage.id
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "markSeenOnExit failed", e)
            }
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
    
    // Group Management Functions
    
    fun updateGroupName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = conversationRepository.updateGroupName(threadId, name)
                if (result is org.ethereumhpone.common.util.Result.Error) {
                    Log.e("ChatViewModel", "Failed to update group name: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to update group name", e)
            }
        }
    }
    
    fun updateGroupDescription(description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = conversationRepository.updateGroupDescription(threadId, description)
                if (result is org.ethereumhpone.common.util.Result.Error) {
                    Log.e("ChatViewModel", "Failed to update group description: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to update group description", e)
            }
        }
    }
    
    fun addGroupMembers(addresses: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = conversationRepository.addGroupMembers(threadId, addresses)
                if (result is org.ethereumhpone.common.util.Result.Error) {
                    Log.e("ChatViewModel", "Failed to add group members: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to add group members", e)
            }
        }
    }
    
    fun removeGroupMember(inboxId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = conversationRepository.removeGroupMembers(threadId, listOf(inboxId))
                if (result is org.ethereumhpone.common.util.Result.Error) {
                    Log.e("ChatViewModel", "Failed to remove group member: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to remove group member", e)
            }
        }
    }
    
    fun leaveGroup(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = conversationRepository.leaveGroup(threadId)
                if (result is org.ethereumhpone.common.util.Result.Success) {
                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                } else if (result is org.ethereumhpone.common.util.Result.Error) {
                    Log.e("ChatViewModel", "Failed to leave group: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to leave group", e)
            }
        }
    }
    
    fun removeGroup(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = conversationRepository.removeGroup(threadId)
                if (result is org.ethereumhpone.common.util.Result.Success) {
                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                } else if (result is org.ethereumhpone.common.util.Result.Error) {
                    Log.e("ChatViewModel", "Failed to remove group: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to remove group", e)
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
    
    /**
     * Execute a transaction request received via XMTP message.
     * 
     * IMPORTANT: Creates a fresh WalletSDK instance for each transaction with the correct
     * chain's RPC and bundler URL. This matches how WalletManager handles transactions
     * and ensures proper web3j configuration for ERC20 transfers on different chains.
     */
    fun executeTransaction(transactionRequest: TransactionRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _transactionStatus.value = TransactionStatus.PENDING
                Log.d("ChatViewModel", "Executing transaction request: chainId=${transactionRequest.chainId}, calls=${transactionRequest.calls.size}")
                
                val targetChainId = transactionRequest.chainId.toInt()
                val rpcUrl = chainIdToRPC(targetChainId)
                val bundlerUrl = chainIdToBundler(targetChainId)
                
                Log.d("ChatViewModel", "Creating chain-specific WalletSDK for chainId=$targetChainId")
                Log.d("ChatViewModel", "RPC URL: $rpcUrl")
                Log.d("ChatViewModel", "Bundler URL: $bundlerUrl")
                
                // Create a fresh WalletSDK instance with the correct chain configuration
                // This is critical for ERC20 transfers - the web3j instance must match the target chain
                val chainWalletSDK = WalletSDK(
                    context = context,
                    web3jInstance = Web3j.build(HttpService(rpcUrl)),
                    bundlerRPCUrl = bundlerUrl
                )
                
                // Ensure the wallet is on the correct chain
                val currentChain = chainWalletSDK.getChainId()
                if (currentChain != targetChainId) {
                    Log.d("ChatViewModel", "Switching WalletSDK from chain $currentChain to $targetChainId")
                    val switchResult = chainWalletSDK.changeChain(targetChainId, rpcUrl, bundlerUrl)
                    if (switchResult == "decline") {
                        Log.e("ChatViewModel", "User declined chain switch")
                        _transactionStatus.value = TransactionStatus.FAILURE("Chain switch declined")
                        return@launch
                    }
                }
                
                // Build transaction params list
                val txParamsList = transactionRequest.calls.map { call ->
                    WalletSDK.TxParams(
                        to = call.to,
                        value = hexToDecimalString(call.value),
                        data = call.data
                    )
                }
                
                // Create gas provider using the target chain's RPC
                val gasProvider: suspend (WalletSDK.UserOperation) -> WalletSDK.GasEstimation = { userOp ->
                    GasEstimationHelper.estimateGas(userOp, rpcUrl)
                }
                
                // Send the transaction using the chain-specific WalletSDK
                val result = if (txParamsList.size == 1) {
                    val tx = txParamsList.first()
                    Log.d("ChatViewModel", "Sending single transaction: to=${tx.to}, value=${tx.value}, data=${tx.data.take(20)}...")
                    chainWalletSDK.sendTransaction(
                        to = tx.to,
                        value = tx.value,
                        data = tx.data,
                        callGas = null,
                        chainId = targetChainId,
                        gasProvider = gasProvider
                    )
                } else {
                    // Batched transaction
                    Log.d("ChatViewModel", "Sending batched transaction with ${txParamsList.size} calls")
                    chainWalletSDK.sendTransaction(
                        txParamsList = txParamsList,
                        callGas = null,
                        chainId = targetChainId,
                        gasProvider = gasProvider
                    )
                }
                
                Log.d("ChatViewModel", "Transaction result: $result")
                
                // Check result
                when {
                    result.startsWith("0x") -> {
                        Log.d("ChatViewModel", "Transaction submitted, checking inclusion: $result")
                        
                        // Prepare transaction details for confirmation message
                        val metadata = transactionRequest.metadata
                        val toAddress = transactionRequest.calls.firstOrNull()?.to ?: ""
                        val fromAddress = chainWalletSDK.getAddress()
                        
                        // Convert token amount to wei (base units)
                        val tokenDecimals = metadata?.tokenDecimals ?: 18
                        val tokenSymbol = metadata?.tokenSymbol ?: "ETH"
                        val amountWei = convertTokenAmountToWei(
                            metadata?.tokenAmount,
                            tokenDecimals
                        )
                        
                        // Check transaction inclusion via bundler before confirming success
                        checkTransactionInclusion(result, targetChainId) { hasBeenIncluded ->
                            if (hasBeenIncluded) {
                                Log.d("ChatViewModel", "Transaction confirmed on-chain: $result")
                                _transactionStatus.value = TransactionStatus.SUCCESS
                                
                                // Send a proper TransactionReference message
                                sendTransactionConfirmation(
                                    txHash = result,
                                    chainId = targetChainId.toLong(),
                                    fromAddress = fromAddress,
                                    toAddress = toAddress,
                                    amountWei = amountWei,
                                    tokenSymbol = tokenSymbol,
                                    tokenDecimals = tokenDecimals
                                )
                            } else {
                                Log.e("ChatViewModel", "Transaction not confirmed or failed: $result")
                                _transactionStatus.value = TransactionStatus.FAILURE("Transaction not confirmed")
                            }
                        }
                    }
                    result.equals("decline", ignoreCase = true) -> {
                        val errorMessage = parseAAErrorCode(result) ?: "Transaction declined"
                        _transactionStatus.value = TransactionStatus.FAILURE(errorMessage)
                        Log.d("ChatViewModel", "Transaction declined by user")
                    }
                    else -> {
                        // Parse AA error codes for user-friendly messages
                        val errorMessage = parseAAErrorCode(result)
                        _transactionStatus.value = TransactionStatus.FAILURE(errorMessage)
                        Log.e("ChatViewModel", "Transaction failed: $result, parsed: $errorMessage")
                    }
                }
                
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Transaction execution failed", e)
                // Parse exception message for AA error codes
                val errorMessage = parseAAErrorCode(e.message ?: "")
                _transactionStatus.value = TransactionStatus.FAILURE(errorMessage)
            }
        }
    }
    
    /**
     * Reject a transaction request.
     */
    fun rejectTransaction(message: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("ChatViewModel", "Transaction rejected for message: ${message.id}")
                // Optionally send a rejection message
                // sendMessage("Transaction request rejected")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to reject transaction", e)
            }
        }
    }
    
    /**
     * Send a transaction request to the current conversation.
     * This allows users to request a transaction from the recipient.
     */
    fun sendTransactionRequest(transactionRequest: TransactionRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!::xmtpConversation.isInitialized) {
                    Log.e("ChatViewModel", "Cannot send transaction request: xmtpConversation not initialized")
                    return@launch
                }
                
                messageRepository.sendTransactionRequest(
                    xmtpConversation = xmtpConversation,
                    threadId = threadId,
                    transactionRequest = transactionRequest
                )
                Log.d("ChatViewModel", "Transaction request sent: ${transactionRequest.metadata?.tokenAmount} ${transactionRequest.metadata?.tokenSymbol}")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send transaction request", e)
            }
        }
    }
    
    /**
     * Send a transaction reference (completed transaction) to the current conversation.
     * This allows users to share proof of a completed transaction.
     */
    fun sendTransactionReference(transactionReference: TransactionReference) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!::xmtpConversation.isInitialized) {
                    Log.e("ChatViewModel", "Cannot send transaction reference: xmtpConversation not initialized")
                    return@launch
                }
                
                messageRepository.sendTransactionReference(
                    xmtpConversation = xmtpConversation,
                    threadId = threadId,
                    transactionReference = transactionReference
                )
                Log.d("ChatViewModel", "Transaction reference sent: ${transactionReference.reference}")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send transaction reference", e)
            }
        }
    }
    
    /**
     * Create and send a transaction reference after a successful transaction execution.
     * @param amountWei Amount in base units as String (to handle values > Long.MAX_VALUE)
     */
    fun sendTransactionConfirmation(
        txHash: String,
        chainId: Long,
        fromAddress: String,
        toAddress: String,
        amountWei: String,
        tokenSymbol: String = "ETH",
        tokenDecimals: Int = 18
    ) {
        val txReference = TransactionReference(
            namespace = "eip155",
            networkId = chainId,
            reference = txHash,
            metadata = TransactionReferenceMetadata(
                transactionType = TransactionTypes.TRANSFER,
                currency = tokenSymbol,
                amount = amountWei,
                decimals = tokenDecimals,
                fromAddress = fromAddress,
                toAddress = toAddress,
                blockExplorerUrl = "${chainIdToEtherscan(chainId.toInt())}/tx/$txHash"
            )
        )
        sendTransactionReference(txReference)
    }
    
    private fun chainIdToBundler(chainId: Int): String {
        return "https://api.pimlico.io/v2/$chainId/rpc?apikey=${BuildConfig.BUNDLER_API}"
    }
    
    
    private fun hexToDecimalString(hex: String): String {
        return try {
            if (hex.startsWith("0x")) {
                BigInteger(hex.removePrefix("0x"), 16).toString()
            } else {
                hex
            }
        } catch (e: Exception) {
            "0"
        }
    }
    
    /**
     * Convert a human-readable token amount (e.g., "0.1") to wei/base units as a String.
     * This handles the conversion properly without floating point precision issues.
     */
    private fun convertTokenAmountToWei(tokenAmount: String?, decimals: Int): String {
        if (tokenAmount.isNullOrBlank()) return "0"
        
        return try {
            // Use BigDecimal for precise conversion
            val amount = BigDecimal(tokenAmount)
            // Multiply by 10^decimals and convert to integer string
            val weiAmount = amount.multiply(BigDecimal.TEN.pow(decimals))
            weiAmount.toBigInteger().toString()
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to convert token amount to wei: $tokenAmount", e)
            "0"
        }
    }
    
    /**
     * Parse ERC-4337 EntryPoint v0.6.0 error codes and return user-friendly messages.
     * Matches WalletManager's implementation for consistency.
     */
    private fun parseAAErrorCode(errorString: String): String? {
        return when {
            // AA1x: Errors during account creation/sender validation
            errorString.contains("AA10 sender already constructed", ignoreCase = true) || 
            errorString.contains("AA10", ignoreCase = true) -> 
                "Account already created"
                
            errorString.contains("AA13 initCode failed or OOG", ignoreCase = true) || 
            errorString.contains("AA13", ignoreCase = true) -> 
                "Account creation failed"
                
            errorString.contains("AA14 initCode must return sender", ignoreCase = true) || 
            errorString.contains("AA14", ignoreCase = true) -> 
                "Invalid account factory"
                
            errorString.contains("AA15 initCode must create sender", ignoreCase = true) || 
            errorString.contains("AA15", ignoreCase = true) -> 
                "Account creation error"
                
            // AA2x: Errors during account validation
            errorString.contains("AA20 account not deployed", ignoreCase = true) || 
            errorString.contains("AA20", ignoreCase = true) -> 
                "Account not deployed"
                
            errorString.contains("AA21 didn't pay prefund", ignoreCase = true) || 
            errorString.contains("AA21", ignoreCase = true) -> 
                "Not enough ETH for gas"
                
            errorString.contains("AA22 expired or not due", ignoreCase = true) || 
            errorString.contains("AA22", ignoreCase = true) -> 
                "Transaction expired"
                
            errorString.contains("AA23 reverted (or OOG)", ignoreCase = true) || 
            errorString.contains("AA23", ignoreCase = true) -> 
                "Validation failed"
                
            errorString.contains("AA24 signature error", ignoreCase = true) || 
            errorString.contains("AA24", ignoreCase = true) -> 
                "Invalid signature"
                
            errorString.contains("AA25 invalid account nonce", ignoreCase = true) || 
            errorString.contains("AA25", ignoreCase = true) -> 
                "Invalid nonce"
                
            // AA3x: Errors during paymaster validation
            errorString.contains("AA30 paymaster not deployed", ignoreCase = true) || 
            errorString.contains("AA30", ignoreCase = true) -> 
                "Paymaster not found"
                
            errorString.contains("AA31 paymaster deposit too low", ignoreCase = true) || 
            errorString.contains("AA31", ignoreCase = true) -> 
                "Paymaster funds too low"
                
            errorString.contains("AA32 paymaster expired", ignoreCase = true) || 
            errorString.contains("AA32", ignoreCase = true) -> 
                "Paymaster expired"
                
            errorString.contains("AA33 reverted (or OOG)", ignoreCase = true) || 
            errorString.contains("AA33", ignoreCase = true) -> 
                "Paymaster rejected"
                
            errorString.contains("AA34 signature error", ignoreCase = true) || 
            errorString.contains("AA34", ignoreCase = true) -> 
                "Paymaster signature invalid"
                
            // AA4x: Errors related to verification gas and execution
            errorString.contains("AA40 over verificationGasLimit", ignoreCase = true) || 
            errorString.contains("AA40", ignoreCase = true) -> 
                "Gas limit exceeded"
                
            errorString.contains("AA41 too little verificationGas", ignoreCase = true) || 
            errorString.contains("AA41", ignoreCase = true) -> 
                "Verification gas too low"
                
            // AA5x: Errors related to gas calculation
            errorString.contains("AA50 postOp revert", ignoreCase = true) || 
            errorString.contains("AA50", ignoreCase = true) -> 
                "Post-operation failed"
                
            errorString.contains("AA51 prefund below actualGasCost", ignoreCase = true) || 
            errorString.contains("AA51", ignoreCase = true) -> 
                "Insufficient gas payment"
                
            // AA9x: Bundler/Validation errors
            errorString.contains("AA90 invalid beneficiary", ignoreCase = true) || 
            errorString.contains("AA90", ignoreCase = true) -> 
                "Invalid beneficiary"
                
            errorString.contains("AA91 failed send to beneficiary", ignoreCase = true) || 
            errorString.contains("AA91", ignoreCase = true) -> 
                "Payment transfer failed"
                
            errorString.contains("AA92 internal call only", ignoreCase = true) || 
            errorString.contains("AA92", ignoreCase = true) -> 
                "Invalid call method"
                
            errorString.contains("AA93 invalid paymasterAndData", ignoreCase = true) || 
            errorString.contains("AA93", ignoreCase = true) -> 
                "Invalid paymaster data"
                
            errorString.contains("AA94 gas values overflow", ignoreCase = true) || 
            errorString.contains("AA94", ignoreCase = true) -> 
                "Gas calculation error"
                
            errorString.contains("AA95 out of gas", ignoreCase = true) || 
            errorString.contains("AA95", ignoreCase = true) -> 
                "Transaction out of gas"
                
            errorString.contains("AA96 invalid aggregator", ignoreCase = true) || 
            errorString.contains("AA96", ignoreCase = true) -> 
                "Invalid signature aggregator"
                
            // Other common errors
            errorString.contains("insufficient funds", ignoreCase = true) -> 
                "Insufficient funds"
            errorString.contains("decline", ignoreCase = true) -> 
                "Transaction declined"
            errorString.contains("reverted", ignoreCase = true) -> 
                "Transaction reverted"
            errorString.contains("gas too low", ignoreCase = true) -> 
                "Gas limit too low"
            errorString.contains("nonce too low", ignoreCase = true) -> 
                "Nonce too low"
            errorString.contains("replacement transaction underpriced", ignoreCase = true) -> 
                "Gas price too low"
            errorString.contains("already known", ignoreCase = true) -> 
                "Transaction already submitted"
            errorString.contains("FailedOp", ignoreCase = true) -> 
                "Operation failed"
            
            // Generic error for unrecognized messages
            errorString.contains("error", ignoreCase = true) && errorString.length > 10 ->
                "Transaction error occurred"
                
            else -> null // Use default message
        }
    }
    
    /**
     * Check if a transaction has been included on-chain via the bundler.
     * Polls the Pimlico bundler's pimlico_getUserOperationStatus RPC method.
     * 
     * @param txHash The transaction/userOp hash to check
     * @param chainId The chain ID for the transaction
     * @param callback Called with true if "included", false if "failed", "rejected", or timeout
     */
    private fun checkTransactionInclusion(txHash: String, chainId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val contentType = "application/json; charset=utf-8".toMediaType()
            
            val startTime = System.currentTimeMillis()
            val timeoutMillis = 60000L // 1 minute timeout
            
            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                val bodyJson = """
                    {
                        "jsonrpc": "2.0",
                        "method": "pimlico_getUserOperationStatus",
                        "params": ["$txHash"],
                        "id": 1
                    }
                """.trimIndent()
                
                val request = Request.Builder()
                    .url(chainIdToBundler(chainId))
                    .post(bodyJson.toRequestBody(contentType))
                    .build()
                
                val status = try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Log.w("ChatViewModel", "Bundler request failed: ${response.code}")
                            null
                        } else {
                            val jsonString = response.body?.string() ?: return@use null
                            val json = JSONObject(jsonString)
                            val resultObj = json.optJSONObject("result")
                            val statusValue = resultObj?.optString("status")
                            Log.d("ChatViewModel", "Transaction status: $statusValue")
                            statusValue
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error checking transaction status", e)
                    null
                }
                
                when (status) {
                    "included" -> {
                        Log.d("ChatViewModel", "Transaction included on-chain: $txHash")
                        withContext(Dispatchers.Main) { callback(true) }
                        return@launch
                    }
                    "failed", "rejected" -> {
                        Log.e("ChatViewModel", "Transaction $status: $txHash")
                        withContext(Dispatchers.Main) { callback(false) }
                        return@launch
                    }
                }
                
                // Wait 4 seconds before next poll
                delay(4000)
            }
            
            // Timeout reached
            Log.e("ChatViewModel", "Transaction inclusion check timed out: $txHash")
            withContext(Dispatchers.Main) {
                callback(false)
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
    data class Error(val message: String) : ConversationUiState
}

sealed interface RecipientUiState {
    object Loading : RecipientUiState
    object Error : RecipientUiState
    data class Success(val recipients: List<Recipient>): RecipientUiState

}





