package org.ethereumphone.contacts

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val ensResolver: ENS
): ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")
    private val contacts: Flow<List<ContactEntity>> = contactRepository.getContacts()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent



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


    fun getOrCreateConversation(contacts: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val addresses = contacts
                .filter { it.isNotBlank() }
                .map { contactIdentifier ->
                    when {
                        contactIdentifier.normalizedString().isValidEns() -> {
                            val result = ensResolver.getAddress(ENSName(contactIdentifier.normalizedString()))

                            if (result == null) {
                                _uiEvent.tryEmit(UiEvent.ShowError("The provided ENS is not valid"))
                                return@launch
                            }
                            Log.d("TEST", result.toString())
                            result.toString().normalizedString()
                        }
                        contactIdentifier.normalizedString().isValidEthAddress() -> contactIdentifier.normalizedString()
                        else -> {
                            _uiEvent.tryEmit(UiEvent.ShowError("Invalid Ethereum address or ENS name"))
                            return@launch
                        }
                    }

                }

            // Guard against empty address list to prevent crashes
            if (addresses.isEmpty()) {
                _uiEvent.tryEmit(UiEvent.ShowError("No valid Ethereum address found for the selected contact"))
                return@launch
            }

            // Only log if there is at least one address
            addresses.firstOrNull()?.let { firstAddress ->
                Log.d("CURRENT ADDRESS", firstAddress)
            }

            conversationRepository.createConversation(addresses).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiEvent.tryEmit(UiEvent.NavigateToConversation(result.data.id))
                    }

                    is Result.Error -> {
                        _uiEvent.tryEmit(UiEvent.ShowError(result.message))
                    }
                }
            }
        }
    }
}

private fun filterContact(contactEntity: ContactEntity, query: String): Boolean {
    val normalizedQuery = query.normalizedString()

    // Only return contacts with valid eth addresses (not null or empty)
    if (contactEntity.ethAddress.isNullOrBlank()) return false

    return contactEntity.name.contains(query, ignoreCase = true) || // Check name
            contactEntity.lookupKey.contains(query, ignoreCase = true) || // Check lookupKey
            contactEntity.ethAddress!!.contains(query, ignoreCase = true) // Check ethAddress (we already know it's not null)
}


private fun String.normalizedString(): String = this.replace("\\s".toRegex(), "").lowercase()

private fun String.isValidEthAddress(): Boolean = this.matches(Regex("^0x[a-fA-F0-9]{40}$"))

private fun String.isValidEns(): Boolean {
    val ens = ENSName(this)
    return ens.isPotentialENSDomain()
}

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

