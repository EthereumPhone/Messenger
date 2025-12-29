package org.ethereumphone.contacts

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import javax.inject.Inject

/**
 * Represents a member in a group with their role
 */
data class GroupMember(
    val ethAddress: String,
    val name: String,
    val role: MemberRole = MemberRole.MEMBER
)

/**
 * Member roles in a group
 */
enum class MemberRole {
    MEMBER,
    ADMIN,
    SUPERADMIN
}

/**
 * UI events emitted by GroupCreationViewModel
 */
sealed interface GroupCreationUiEvent {
    data class NavigateToConversation(val conversationId: String) : GroupCreationUiEvent
    data class ShowError(val message: String) : GroupCreationUiEvent
    object NavigateToEditGroup : GroupCreationUiEvent
    object GroupCreationStarted : GroupCreationUiEvent
}

/**
 * UI state for group creation flow
 */
data class GroupCreationUiState(
    val groupName: String = "",
    val groupImageUri: Uri? = null,
    val members: List<GroupMember> = emptyList(),
    val isCreating: Boolean = false,
    val currentScreen: GroupCreationScreen = GroupCreationScreen.ADD_MEMBERS
)

enum class GroupCreationScreen {
    ADD_MEMBERS,
    EDIT_GROUP_INFO
}

@HiltViewModel
class GroupCreationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupCreationUiState())
    val uiState: StateFlow<GroupCreationUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GroupCreationUiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<GroupCreationUiEvent> = _uiEvent

    // Selected eth addresses from AddGroupMembersSheet
    private val _selectedAddresses = MutableStateFlow<List<String>>(emptyList())
    val selectedAddresses: StateFlow<List<String>> = _selectedAddresses.asStateFlow()

    /**
     * Updates the selected member addresses
     */
    fun updateSelectedAddresses(addresses: List<String>) {
        _selectedAddresses.value = addresses
        viewModelScope.launch {
            // Convert addresses to GroupMembers with contact info
            val contacts = contactRepository.getContacts().first()
            val members = addresses.mapNotNull { address ->
                val contact = contacts.find { 
                    it.ethAddress?.equals(address, ignoreCase = true) == true 
                }
                GroupMember(
                    ethAddress = address,
                    name = contact?.name ?: formatAddress(address),
                    role = MemberRole.MEMBER
                )
            }
            _uiState.value = _uiState.value.copy(members = members)
        }
    }

    /**
     * Navigate to EditGroupInfoSheet
     */
    fun navigateToEditGroup() {
        _uiState.value = _uiState.value.copy(currentScreen = GroupCreationScreen.EDIT_GROUP_INFO)
        _uiEvent.tryEmit(GroupCreationUiEvent.NavigateToEditGroup)
    }

    /**
     * Navigate back to AddMembersSheet
     */
    fun navigateBackToAddMembers() {
        _uiState.value = _uiState.value.copy(currentScreen = GroupCreationScreen.ADD_MEMBERS)
    }

    /**
     * Updates the group name
     */
    fun updateGroupName(name: String) {
        _uiState.value = _uiState.value.copy(groupName = name)
    }

    /**
     * Updates the group image URI
     */
    fun updateGroupImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(groupImageUri = uri)
    }

    /**
     * Removes a member from the group by their eth address
     */
    fun removeMember(ethAddress: String) {
        val currentMembers = _uiState.value.members.toMutableList()
        currentMembers.removeAll { it.ethAddress.equals(ethAddress, ignoreCase = true) }
        _uiState.value = _uiState.value.copy(members = currentMembers)
        
        // Also update selectedAddresses
        _selectedAddresses.value = _selectedAddresses.value.filter { 
            !it.equals(ethAddress, ignoreCase = true) 
        }
    }

    /**
     * Updates a member's role
     */
    fun updateMemberRole(ethAddress: String, role: MemberRole) {
        val currentMembers = _uiState.value.members.toMutableList()
        val index = currentMembers.indexOfFirst { it.ethAddress.equals(ethAddress, ignoreCase = true) }
        if (index != -1) {
            currentMembers[index] = currentMembers[index].copy(role = role)
            _uiState.value = _uiState.value.copy(members = currentMembers)
        }
    }

    /**
     * Creates the group in XMTP and navigates to the conversation
     */
    fun createGroup() {
        val state = _uiState.value
        
        if (state.members.isEmpty()) {
            _uiEvent.tryEmit(GroupCreationUiEvent.ShowError("Add at least one member to create a group"))
            return
        }

        if (state.groupName.isBlank()) {
            _uiEvent.tryEmit(GroupCreationUiEvent.ShowError("Please enter a group name"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isCreating = true)
            _uiEvent.tryEmit(GroupCreationUiEvent.GroupCreationStarted)

            try {
                val addresses = state.members.map { it.ethAddress }
                
                // Call the repository to create the group
                conversationRepository.createGroupConversation(
                    addresses = addresses,
                    groupName = state.groupName,
                    groupImageUrl = state.groupImageUri?.toString()
                ).collect { result ->
                    when (result) {
                        is org.ethereumhpone.common.util.Result.Success -> {
                            _uiState.value = _uiState.value.copy(isCreating = false)
                            _uiEvent.tryEmit(GroupCreationUiEvent.NavigateToConversation(result.data.id))
                            // Reset state after successful creation
                            resetState()
                        }
                        is org.ethereumhpone.common.util.Result.Error -> {
                            _uiState.value = _uiState.value.copy(isCreating = false)
                            _uiEvent.tryEmit(GroupCreationUiEvent.ShowError(result.message))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GroupCreationViewModel", "Error creating group", e)
                _uiState.value = _uiState.value.copy(isCreating = false)
                _uiEvent.tryEmit(GroupCreationUiEvent.ShowError(e.message ?: "Failed to create group"))
            }
        }
    }

    /**
     * Resets the state for a fresh group creation
     */
    fun resetState() {
        _uiState.value = GroupCreationUiState()
        _selectedAddresses.value = emptyList()
    }

    /**
     * Get members as ContactEntity list (for compatibility with EditGroupInfoSheet)
     */
    fun getMembersAsContacts(): List<ContactEntity> {
        return _uiState.value.members.map { member ->
            ContactEntity(
                lookupKey = member.ethAddress,
                name = member.name,
                ethAddress = member.ethAddress
            )
        }
    }

    private fun formatAddress(address: String): String {
        return if (address.endsWith(".eth")) {
            address
        } else if (address.length > 10) {
            "${address.take(6)}...${address.takeLast(4)}"
        } else {
            address
        }
    }
}
