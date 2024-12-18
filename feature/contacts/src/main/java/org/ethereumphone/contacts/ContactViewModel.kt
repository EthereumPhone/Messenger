package org.ethereumphone.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.domain.repository.ContactRepository
import javax.inject.Inject

class ContactViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
): ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")
    private val contacts: Flow<List<Contact>> = contactRepository.getContacts()

    val queryResultUiState: StateFlow<QueryResultUiState> =
        combine(
            contacts,
            searchQuery
        ) { contacts, query ->

            if (query.isEmpty()) {
                QueryResultUiState.Success(null, contacts)
            } else {
                // Generate a manual contact based on the query
                val manualContact: Contact? = when {
                    phoneNumberUtils.isPossibleNumber(query) -> Contact(numbers = listOf(PhoneNumber(address = query)))
                    query.isValidEns() || query.isValidEthAddress() -> Contact(ethAddress = query)
                    else -> null
                }

                // Filter contacts based on the query and add manual contact if present
                val filteredContacts = contacts.filter { filterContact(it, query) }

                // Return appropriate UI state
                QueryResultUiState.Success(
                    manualContact = manualContact,
                    contacts = filteredContacts
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
}

private fun filterContact(contact: Contact, query: String): Boolean {
    val normalizedQuery = query.normalizedString()

    return contact.name.contains(query) || // Check name
            contact.lookupKey.contains(query) || // Check lookupKey
            contact.numbers.any { it.address.contains(normalizedQuery) } || // Check normalized numbers
            (contact.ethAddress?.contains(query) ?: false) // Check ethAddress
}


private fun String.normalizedString(): String = this.replace(" ", "").lowercase()

private fun String.isValidEthAddress(): Boolean = this.matches(Regex("^0x[a-fA-F0-9]{40}$"))

private fun String.isValidEns(): Boolean = this.matches(Regex("^[a-zA-Z0-9-_$]{3,}\\.eth$"))


sealed interface QueryResultUiState {
    object Loading : QueryResultUiState
    data class Success(
        val manualContact: Contact? = null, // for the "write to ..." first list item
        val contacts: List<Contact> = emptyList()
    ): QueryResultUiState {
        fun isEmpty(): Boolean = contacts.isEmpty()
    }
}




private const val SEARCH_QUERY = "searchQuery"
