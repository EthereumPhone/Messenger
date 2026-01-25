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
     */
    fun executeTransaction(transactionRequest: TransactionRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _transactionStatus.value = TransactionStatus.PENDING
                Log.d("ChatViewModel", "Executing transaction request: chainId=${transactionRequest.chainId}, calls=${transactionRequest.calls.size}")
                
                // Switch chain if necessary
                val targetChainId = transactionRequest.chainId.toInt()
                val currentChain = walletSDK.getChainId()
                
                if (currentChain != targetChainId) {
                    Log.d("ChatViewModel", "Switching chain from $currentChain to $targetChainId")
                    val rpcUrl = chainIdToRPC(targetChainId)
                    val bundlerUrl = chainIdToBundler(targetChainId)
                    val switchResult = walletSDK.changeChain(targetChainId, rpcUrl, bundlerUrl)
                    if (switchResult == "decline") {
                        Log.e("ChatViewModel", "User declined chain switch")
                        _transactionStatus.value = TransactionStatus.FAILURE
                        return@launch
                    }
                }
                
                val rpcUrl = chainIdToRPC(targetChainId)
                val bundlerUrl = chainIdToBundler(targetChainId)
                
                // Build transaction params list
                val txParamsList = transactionRequest.calls.map { call ->
                    WalletSDK.TxParams(
                        to = call.to,
                        value = hexToDecimalString(call.value),
                        data = call.data
                    )
                }
                
                // Create gas provider
                val gasProvider: suspend (WalletSDK.UserOperation) -> WalletSDK.GasEstimation = { userOp ->
                    GasEstimationHelper.estimateGas(userOp, rpcUrl)
                }
                
                // Send the transaction
                val result = if (txParamsList.size == 1) {
                    val tx = txParamsList.first()
                    walletSDK.sendTransaction(
                        to = tx.to,
                        value = tx.value,
                        data = tx.data,
                        callGas = null,
                        chainId = targetChainId,
                        gasProvider = gasProvider
                    )
                } else {
                    // Batched transaction
                    walletSDK.sendTransaction(
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
                        _transactionStatus.value = TransactionStatus.SUCCESS
                        // Send confirmation message
                        val confirmationMessage = buildTransactionConfirmationMessage(transactionRequest, result, targetChainId)
                        sendMessage(confirmationMessage)
                        Log.d("ChatViewModel", "Transaction successful: $result")
                    }
                    result.equals("decline", ignoreCase = true) -> {
                        _transactionStatus.value = TransactionStatus.FAILURE
                        Log.d("ChatViewModel", "Transaction declined by user")
                    }
                    else -> {
                        _transactionStatus.value = TransactionStatus.FAILURE
                        Log.e("ChatViewModel", "Transaction failed: $result")
                    }
                }
                
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Transaction execution failed", e)
                _transactionStatus.value = TransactionStatus.FAILURE
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
    
    private fun buildTransactionConfirmationMessage(
        txRequest: TransactionRequest,
        txHash: String,
        chainId: Int
    ): String {
        val etherscanUrl = chainIdToEtherscan(chainId)
        val metadata = txRequest.metadata
        
        return if (metadata?.tokenAmount != null && metadata.tokenSymbol != null) {
            "Sent ${metadata.tokenAmount} ${metadata.tokenSymbol}: $etherscanUrl/tx/$txHash"
        } else {
            "Transaction executed: $etherscanUrl/tx/$txHash"
        }
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





