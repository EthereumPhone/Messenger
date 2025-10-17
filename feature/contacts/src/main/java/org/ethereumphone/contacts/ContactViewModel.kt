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
                if (shouldPreResolve(normalized)) {
                    preResolveAddress(normalized)
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

    private fun shouldPreResolve(query: String): Boolean {
        // Don't pre-resolve if it's already a valid eth address or empty
        if (query.isBlank() || query.isValidEthAddress()) {
            return false
        }

        // Check if it looks like a potential ENS (has a dot and some characters)
        // e.g., "nceornea.e" or "jesse.b"
        val hasDot = query.contains('.')
        val parts = query.split('.')
        
        return when {
            // Potential Base ENS: at least 3 chars before .b or .base or .base.e, etc.
            parts.size >= 2 && parts[0].length >= 3 && 
                (parts[1].startsWith("b") || query.contains(".base", ignoreCase = true)) -> true
            
            // Potential ENS: has a dot and looks like it could be an ENS
            hasDot && parts.size >= 2 && parts[0].length >= 3 -> true
            
            else -> false
        }
    }

    private fun preResolveAddress(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Skip if already resolving/resolved
                if (preResolvedCache.containsKey(query)) {
                    return@launch
                }

                // Check if online
                val isOnline = networkManager.isOnline.first()
                if (!isOnline) {
                    preResolvedCache[query] = ResolvedResult(null, "Connect to the internet to resolve this name")
                    return@launch
                }

                // Determine what type of name we're resolving
                val result = when {
                    query.isValidBaseEns() -> {
                        val baseResult = baseNameResolver.resolve(query)
                        if (baseResult.error != null) {
                            ResolvedResult(null, "The provided Base Name is not valid")
                        } else if (baseResult.address.isNullOrEmpty()) {
                            ResolvedResult(null, "The provided Base Name could not be resolved")
                        } else {
                            ResolvedResult(baseResult.address!!.normalizedString(), null)
                        }
                    }
                    query.isValidEns() -> {
                        val ensAddress = ensResolver.getAddress(ENSName(query))
                        if (ensAddress == null) {
                            ResolvedResult(null, "The provided ENS is not valid")
                        } else {
                            ResolvedResult(ensAddress.toString().normalizedString(), null)
                        }
                    }
                    else -> {
                        // Not a valid ENS format yet, don't cache
                        return@launch
                    }
                }

                preResolvedCache[query] = result
                Log.d("ContactViewModel", "Pre-resolved $query -> ${result.address ?: result.error}")
            } catch (e: Exception) {
                preResolvedCache[query] = ResolvedResult(null, "Unable to resolve name: ${e.message}")
                Log.e("ContactViewModel", "Error pre-resolving $query", e)
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
                // Check pre-resolved results for errors, but pass original identifiers to repository
                // so it can preserve ENS/Base names as conversation titles
                val identifiers = contacts
                    .filter { it.isNotBlank() }
                    .map { contactIdentifier ->
                        val normalized = contactIdentifier.normalizedString()
                        
                        // Check if we have a pre-resolved result with an error
                        val preResolved = preResolvedCache[normalized]
                        if (preResolved != null && preResolved.error != null) {
                            _uiEvent.tryEmit(UiEvent.ShowError(preResolved.error))
                            return@launch
                        }
                        
                        // Validate the identifier format
                        when {
                            normalized.isValidEns() || normalized.isValidBaseEns() || normalized.isValidEthAddress() -> {
                                normalized
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

