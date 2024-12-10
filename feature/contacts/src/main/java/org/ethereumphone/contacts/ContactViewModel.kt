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
import org.ethereumhpone.domain.repository.ContactRepository
import javax.inject.Inject

class ContactViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
): ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")
    private val contacts: Flow<List<Contact>> = contactRepository.getContacts()

    val contactsUiState: StateFlow<QueryResultUiState> =
        combine(
            contacts,
            searchQuery
        ) { contacts, query ->
            if (query.isEmpty()) {
                QueryResultUiState.Success(contacts)
            }

            //TODO add first list element to initiate chat via direct number insertion
            QueryResultUiState.Success(contacts)



        }.stateIn(
            scope = viewModelScope,
            initialValue = QueryResultUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )




    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }


    fun isPossibleQuery(query: String): Boolean {
        //phoneNumberUtils.isPossibleNumber(query)
        TODO()
    }
}


sealed interface QueryResultUiState {
    object Loading : QueryResultUiState
    object Empty : QueryResultUiState
    data class Success(val contacts: List<Contact> = emptyList()): QueryResultUiState {
        fun isEmpty(): Boolean = contacts.isEmpty()
    }
}




private const val SEARCH_QUERY = "searchQuery"
