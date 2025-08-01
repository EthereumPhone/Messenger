package org.ethereumhpone.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.navigation.AddressesArgs
import org.ethereumhpone.chat.navigation.ContactNameArgs
import org.ethereumhpone.chat.navigation.navigateToChatByThreadId
import org.ethereumhpone.common.util.Result
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.showDgenToast
import javax.inject.Inject

@HiltViewModel
class ChatLoadingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conversationRepository: ConversationRepository
) : ViewModel() {
    
    private val addresses = AddressesArgs(savedStateHandle).addresses ?: emptyList()
    private val contactName = ContactNameArgs(savedStateHandle).contactName
    
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(replay = 0)
    val navigationEvent = _navigationEvent.asSharedFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var hasNavigated = false
    private var hasStartedConversationCreation = false
    
    init {
        createAndNavigateToConversation()
    }
    
    private fun createAndNavigateToConversation() {
        viewModelScope.launch {
            // Prevent multiple instances of the same operation
            if (hasStartedConversationCreation) return@launch
            hasStartedConversationCreation = true
            
            // Add small delay to ensure smooth animation
            delay(100)
            
            conversationRepository.createConversation(addresses)
                .collect { result ->
                    // Prevent multiple navigations
                    if (hasNavigated) return@collect
                    
                    when (result) {
                        is Result.Success -> {
                            hasNavigated = true
                            _isLoading.value = false
                            // Small delay for smooth transition
                            delay(150)
                            _navigationEvent.emit(
                                NavigationEvent.NavigateToChat(
                                    conversationId = result.data.id,
                                    contactName = contactName
                                )
                            )
                        }
                        is Result.Error -> {
                            hasNavigated = true
                            
                            if (result.message == "NOT_REGISTERED_WITH_XMTP") {
                                // Show loading matrix for exactly 3 seconds for XMTP error
                                delay(3000)
                                _isLoading.value = false
                                _navigationEvent.emit(NavigationEvent.NavigateToContacts)
                            } else {
                                _isLoading.value = false
                                // Toast is already shown in repository
                                // Small delay to ensure toast is visible
                                delay(300)
                                _navigationEvent.emit(NavigationEvent.NavigateBack)
                            }
                        }
                    }
                }
        }
    }
}

sealed class NavigationEvent {
    data class NavigateToChat(val conversationId: String, val contactName: String?) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    object NavigateToContacts : NavigationEvent()
}

@Composable
fun ChatLoadingRoute(
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToContacts: () -> Unit,
    viewModel: ChatLoadingViewModel = hiltViewModel()
) {
    // Remember colors to avoid recomposition on color changes
    val primaryColor = remember { SystemColorManager.primaryColor }
    val secondaryColor = remember { SystemColorManager.secondaryColor }
    
    // Track animation state
    var showContent by remember { mutableStateOf(false) }
    
    // Track if navigation has been handled to prevent multiple calls
    var hasHandledNavigation by remember { mutableStateOf(false) }
    
    // Trigger fade-in animation
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    // Handle navigation events
    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collect { event ->
            // Prevent handling the same navigation event multiple times
            if (hasHandledNavigation) return@collect
            hasHandledNavigation = true
            
            when (event) {
                is NavigationEvent.NavigateToChat -> {
                    onNavigateToChat(event.conversationId)
                }
                is NavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is NavigationEvent.NavigateToContacts -> {
                    onNavigateToContacts()
                }
            }
        }
    }
    
    // Full screen with centered loading indicator
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
        contentAlignment = Alignment.Center
    ) {
        // Animated fade-in for smooth appearance
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 50
                )
            )
        ) {
            // Wrap in a Box to ensure stable positioning
            Box(
                modifier = Modifier.wrapContentSize(Alignment.Center)
            ) {
                DgenLoadingMatrix(
                    unactiveLEDColor = secondaryColor,
                    activeLEDColor = primaryColor
                )
            }
        }
    }
} 