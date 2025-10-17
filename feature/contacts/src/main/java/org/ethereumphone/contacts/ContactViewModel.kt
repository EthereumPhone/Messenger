package org.ethereumphone.contacts

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.basenameservice.BaseNameResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.common.util.Result
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.manager.NetworkManager
import org.ethereumphone.dgenlibrary.showDgenToast
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val conversationRepository: ConversationRepository,
    private val ensResolver: ENS,
    private val baseNameResolver: BaseNameResolver,
    private val networkManager: NetworkManager
): ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")
    private val contacts: Flow<List<ContactEntity>> = contactRepository.getContacts()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    // Cache for pre-resolved addresses
    private data class ResolvedResult(val address: String?, val error: String?)
    private val preResolvedCache = mutableMapOf<String, ResolvedResult>()

    init {
        // Monitor search query and pre-resolve when it looks like ENS or Base name
        viewModelScope.launch(Dispatchers.IO) {
            searchQuery.collectLatest { query ->
                val normalized = query.normalizedString()
                
                // Check if it looks like a potential ENS or Base name being typed
                val predictedName = getPredictedFullName(normalized)
                if (predictedName != null) {
                    Log.d("ContactViewModel", "🔍 Query '$normalized' -> predicting '$predictedName', triggering pre-resolution")
                    preResolveAddress(predictedName)
                } else if (normalized.isNotBlank()) {
                    Log.d("ContactViewModel", "🔍 Query '$normalized' doesn't match pre-resolve criteria yet")
                }
            }
        }
    }

    val queryResultUiState: StateFlow<QueryResultUiState> =
        combine(
            contacts,
            searchQuery
        ) { contacts, query ->
            // Filter contacts to only show those with valid eth addresses (not null or empty)
            val contactsWithEthAddress = contacts.filter { 
                !it.ethAddress.isNullOrBlank() 
            }

            if (query.isEmpty()) {
                QueryResultUiState.Success(null, contactsWithEthAddress)
            } else {
                // Generate a manual contact based on the query
                val manualContactEntity = if (query.isValidEns() || query.isValidEthAddress()) {
                    ContactEntity(lookupKey = query, ethAddress = query, name = query)
                } else {
                    null
                }

                // Filter contacts based on the query (filterContact already checks for ethAddress)
                val filteredContacts = contactsWithEthAddress.filter { filterContact(it, query) }

                // Return appropriate UI state
                QueryResultUiState.Success(
                    manualContactEntity = manualContactEntity,
                    contactEntities = filteredContacts
                )
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = QueryResultUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    /**
     * Predicts the full ENS/Base name based on partial input
     * Returns null if the input doesn't look like it could be an ENS/Base name
     * 
     * Examples:
     * - "nceornea.e" -> "nceornea.eth"
     * - "jesse.base.e" -> "jesse.base.eth"
     * - "dgen1.markus.e" -> "dgen1.markus.eth"
     * - "vitalik.et" -> "vitalik.eth"
     */
    private fun getPredictedFullName(query: String): String? {
        // Don't predict if it's already a valid eth address or empty
        if (query.isBlank() || query.isValidEthAddress()) {
            return null
        }

        // If it's already a COMPLETE Base name, return as-is
        if (query.isValidBaseEns()) {
            return query
        }
        
        // If it ends with .eth (complete ENS), return as-is
        if (query.endsWith(".eth", ignoreCase = true)) {
            return query
        }

        val parts = query.split('.')
        val lastPart = parts.lastOrNull()?.lowercase() ?: ""
        
        // Check if the last part looks like a partial "eth" or "base"
        val looksLikePartialEth = lastPart.isNotEmpty() && "eth".startsWith(lastPart) && lastPart != "eth"
        val looksLikePartialBase = lastPart.isNotEmpty() && "base".startsWith(lastPart)
        
        return when {
            // Case 1: Ends with partial "base" -> predict ".base.eth"
            // e.g., "name.b", "name.ba", "name.bas", "name.base"
            parts.size == 2 && parts[0].length >= 3 && looksLikePartialBase -> {
                "${parts[0]}.base.eth"
            }
            
            // Case 2: Second-to-last is "base" and ends with partial or no "eth"
            // e.g., "name.base", "name.base.", "name.base.e", "name.base.et"
            parts.size >= 2 && parts[parts.size - 2].equals("base", ignoreCase = true) -> {
                val prefix = parts.dropLast(1).joinToString(".")
                "$prefix.eth"
            }
            
            // Case 3: Multiple parts ending with partial "eth"
            // e.g., "dgen1.markus.e", "sub.domain.et"
            parts.size >= 2 && parts[0].length >= 1 && looksLikePartialEth -> {
                val prefix = parts.dropLast(1).joinToString(".")
                "$prefix.eth"
            }
            
            // Case 4: Ends with just a dot (e.g., "name.")
            // Predict .eth
            parts.size >= 2 && lastPart.isEmpty() && parts.dropLast(1).all { it.isNotEmpty() } -> {
                val prefix = parts.dropLast(1).joinToString(".")
                "$prefix.eth"
            }
            
            else -> null
        }
    }

    private fun preResolveAddress(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Skip if already resolving/resolved
                if (preResolvedCache.containsKey(query)) {
                    Log.d("ContactViewModel", "⏭️ Skipping pre-resolve for '$query' - already cached")
                    return@launch
                }

                // Check if online
                val isOnline = networkManager.isOnline.first()
                if (!isOnline) {
                    Log.d("ContactViewModel", "📡 Offline: Cannot pre-resolve '$query'")
                    preResolvedCache[query] = ResolvedResult(null, "Connect to the internet to resolve this name")
                    return@launch
                }

                // Determine what type of name we're resolving
                val result = when {
                    query.isValidBaseEns() -> {
                        Log.d("ContactViewModel", "🔵 Starting Base name pre-resolution for '$query'")
                        val startTime = System.currentTimeMillis()
                        val baseResult = baseNameResolver.resolve(query)
                        val duration = System.currentTimeMillis() - startTime
                        
                        if (baseResult.error != null) {
                            Log.d("ContactViewModel", "❌ Base name resolution failed for '$query' in ${duration}ms: ${baseResult.error}")
                            ResolvedResult(null, "The provided Base Name is not valid")
                        } else if (baseResult.address.isNullOrEmpty()) {
                            Log.d("ContactViewModel", "❌ Base name resolution returned empty for '$query' in ${duration}ms")
                            ResolvedResult(null, "The provided Base Name could not be resolved")
                        } else {
                            Log.d("ContactViewModel", "✅ Base name pre-resolved '$query' -> ${baseResult.address} in ${duration}ms")
                            ResolvedResult(baseResult.address!!.normalizedString(), null)
                        }
                    }
                    query.isValidEns() -> {
                        Log.d("ContactViewModel", "🟢 Starting ENS pre-resolution for '$query'")
                        val startTime = System.currentTimeMillis()
                        val ensAddress = ensResolver.getAddress(ENSName(query))
                        val duration = System.currentTimeMillis() - startTime
                        
                        if (ensAddress == null) {
                            Log.d("ContactViewModel", "❌ ENS resolution failed for '$query' in ${duration}ms")
                            ResolvedResult(null, "The provided ENS is not valid")
                        } else {
                            Log.d("ContactViewModel", "✅ ENS pre-resolved '$query' -> ${ensAddress} in ${duration}ms")
                            ResolvedResult(ensAddress.toString().normalizedString(), null)
                        }
                    }
                    else -> {
                        // Not a valid ENS format yet, don't cache
                        Log.d("ContactViewModel", "⚠️ Query '$query' is not a valid ENS/Base name format yet")
                        return@launch
                    }
                }

                preResolvedCache[query] = result
            } catch (e: Exception) {
                preResolvedCache[query] = ResolvedResult(null, "Unable to resolve name: ${e.message}")
                Log.e("ContactViewModel", "💥 Exception pre-resolving '$query': ${e.message}", e)
            }
        }
    }


    fun getOrCreateConversation(contacts: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isOnline = networkManager.isOnline.first()

                // If offline, only allow navigation to an existing local conversation.
                if (!isOnline) {
                    val contactIdentifier = contacts.firstOrNull()?.normalizedString()

                    if (contactIdentifier.isNullOrBlank()) {
                        _uiEvent.tryEmit(UiEvent.ShowError("Connect to the internet to start a new conversation"))
                        return@launch
                    }

                    val existingConversation = tryFindExistingLocalConversation(contactIdentifier)

                    if (existingConversation != null) {
                        _uiEvent.tryEmit(UiEvent.NavigateToConversation(existingConversation))
                    } else {
                        _uiEvent.tryEmit(UiEvent.ShowError("Connect to the internet to start a new conversation"))
                    }
                    return@launch
                }

                // Online: proceed with normal flow
                // Use cached resolved addresses when available to avoid re-resolution
                val identifiers = contacts
                    .filter { it.isNotBlank() }
                    .map { contactIdentifier ->
                        val normalized = contactIdentifier.normalizedString()
                        
                        // Check if the input is a partial name and get the predicted full name
                        val predictedName = getPredictedFullName(normalized)
                        
                        // Only use pre-cached results if the actual input matches what we predicted
                        // This ensures typos like "jesse.base.rth" don't incorrectly use "jesse.base.eth" cache
                        val shouldUseCachedResult = predictedName != null && normalized == predictedName
                        
                        if (shouldUseCachedResult && predictedName != null) {
                            // Check if we have a pre-resolved result
                            val preResolved = preResolvedCache[predictedName]
                            if (preResolved != null) {
                                if (preResolved.error != null) {
                                    Log.d("ContactViewModel", "🚫 Using cached error for '$predictedName': ${preResolved.error}")
                                    _uiEvent.tryEmit(UiEvent.ShowError(preResolved.error))
                                    return@launch
                                } else if (preResolved.address != null) {
                                    // Use the cached resolved address instead of the ENS name
                                    // This avoids re-resolution in the repository
                                    Log.d("ContactViewModel", "⚡ Using cached pre-resolved address for '$predictedName': ${preResolved.address}")
                                    return@map preResolved.address
                                }
                            }
                        } else if (predictedName != null && normalized != predictedName) {
                            Log.d("ContactViewModel", "⚠️ Input '$normalized' doesn't match prediction '$predictedName' - will resolve as-is")
                        }
                        
                        // Use the normalized input (what the user actually typed)
                        // The repository will need to resolve this
                        val finalIdentifier = normalized
                        
                        // Validate the identifier format
                        when {
                            finalIdentifier.isValidEns() || finalIdentifier.isValidBaseEns() || finalIdentifier.isValidEthAddress() -> {
                                finalIdentifier
                            }
                            else -> {
                                _uiEvent.tryEmit(UiEvent.ShowError("Invalid Ethereum address or ENS name"))
                                return@launch
                            }
                        }
                    }

                // Guard against empty identifier list to prevent crashes
                if (identifiers.isEmpty()) {
                    return@launch
                }

                // Only log if there is at least one identifier
                identifiers.firstOrNull()?.let { firstIdentifier ->
                    Log.d("CURRENT IDENTIFIER", firstIdentifier)
                }

                conversationRepository.createConversation(identifiers).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiEvent.tryEmit(UiEvent.NavigateToConversation(result.data.id))
                        }

                        is Result.Error -> {
                            // Suppress internal code from surfacing in UI; repository already shows a user-friendly toast
                            if (result.message != "NOT_REGISTERED_WITH_XMTP") {
                                _uiEvent.tryEmit(UiEvent.ShowError(result.message))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Any unexpected errors (including network) should prompt the user to connect.
                _uiEvent.tryEmit(UiEvent.ShowError("Connect to the internet to start a new conversation"))
            }
        }
    }

    private suspend fun tryFindExistingLocalConversation(contactIdentifier: String): String? {
        // Try to find an existing conversation locally by ETH address or ENS title/recipient.
        val conversations = conversationRepository.getConversations().first()

        return when {
            contactIdentifier.isValidEthAddress() -> {
                conversations.firstOrNull { conv ->
                    conv.getOtherRecipientAddress()?.equals(contactIdentifier, ignoreCase = true) == true
                }?.id
            }
            contactIdentifier.isValidEns() -> {
                conversations.firstOrNull { conv ->
                    conv.title?.equals(contactIdentifier, ignoreCase = true) == true ||
                            conv.getOtherRecipients().any { r -> r.ens?.equals(contactIdentifier, ignoreCase = true) == true }
                }?.id
            }
            else -> null
        }
    }
}

private fun filterContact(contactEntity: ContactEntity, query: String): Boolean {
    // Only return contacts with valid eth addresses (not null or empty)
    if (contactEntity.ethAddress.isNullOrBlank()) return false

    return contactEntity.name.contains(query, ignoreCase = true) || // Check name
            contactEntity.lookupKey.contains(query, ignoreCase = true) || // Check lookupKey
            (contactEntity.ethAddress?.contains(query, ignoreCase = true) == true) // Check ethAddress
}


private fun String.normalizedString(): String = this.replace("\\s".toRegex(), "").lowercase()

private fun String.isValidEthAddress(): Boolean = this.matches(Regex("^0x[a-fA-F0-9]{40}$"))

private fun String.isValidEns(): Boolean {
    val ens = ENSName(this)
    return ens.isPotentialENSDomain()
}

private fun String.isValidBaseEns(): Boolean = this.contains(Regex("^[a-z0-9]{3,}\\.base\\.eth$", RegexOption.IGNORE_CASE))


private fun String.isPossibleENS(): Boolean = this.matches(Regex("^[a-zA-Z0-9-_\$]{3,}$"))


sealed interface UiEvent {
    data class NavigateToConversation(val id: String) : UiEvent
    data class ShowError(val message: String) : UiEvent
}



sealed interface QueryResultUiState {
    object Loading : QueryResultUiState
    data class Success(
        val manualContactEntity: ContactEntity? = null, // for the "write to ..." first list item
        val contactEntities: List<ContactEntity> = emptyList()
    ): QueryResultUiState {
        fun isEmpty(): Boolean = contactEntities.isEmpty()
    }
}




private const val SEARCH_QUERY = "searchQuery"

