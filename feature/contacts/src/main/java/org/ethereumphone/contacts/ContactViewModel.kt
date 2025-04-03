package org.ethereumphone.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.domain.repository.ContactRepository
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
): ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")
    private val contacts: Flow<List<ContactEntity>> = contactRepository.getContacts()

    val queryResultUiState: StateFlow<QueryResultUiState> =
        combine(
            contacts,
            searchQuery
        ) { contacts, query ->

            if (query.isEmpty()) {
                QueryResultUiState.Success(null, contacts)
            } else {
                // Generate a manual contact based on the query

                //TODO: only show if possible ens or ethAddress?
                //val manualContact = query.takeIf { it.isValidEns() || it.isValidEthAddress() }?.let { Contact(lookupKey = it, ethAddress = it) }
                val manualContactEntity = ContactEntity(lookupKey = query, ethAddress = query)

                // Filter contacts based on the query and add manual contact if present
                val filteredContacts = contacts.filter { filterContact(it, query) }

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
}

private fun filterContact(contactEntity: ContactEntity, query: String): Boolean {
    val normalizedQuery = query.normalizedString()

    return contactEntity.name.contains(query) || // Check name
            contactEntity.lookupKey.contains(query) || // Check lookupKey
            contactEntity.numbers.any { it.address.contains(normalizedQuery) } || // Check normalized numbers
            (contactEntity.ethAddress?.contains(query) ?: false) // Check ethAddress
}


private fun String.normalizedString(): String = this.replace(" ", "").lowercase()

private fun String.isValidEthAddress(): Boolean = this.matches(Regex("^0x[a-fA-F0-9]{40}$"))

private fun String.isValidEns(): Boolean = this.matches(Regex("^[a-zA-Z0-9-_$]{3,}\\.eth$"))

private fun String.isPossibleENS(): Boolean = this.matches(Regex("^[a-zA-Z0-9-_\$]{3,}$"))


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
