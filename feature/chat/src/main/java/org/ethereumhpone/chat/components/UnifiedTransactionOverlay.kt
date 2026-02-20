package org.ethereumhpone.chat.components

import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.dgenlibrary.InfoScreen
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.pulseOpacity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.ethereumhpone.chat.AssetsUiState
import org.ethereumhpone.chat.ChatSendViewModel
import org.ethereumhpone.chat.ConversationUiState
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.TransactionCall
import org.ethereumphone.model.TransactionMetadata
import org.ethereumphone.model.TransactionRequest
import org.ethosmobile.contacts.ui.components.DgenCursorSearchTextfield
import org.ethereumphone.dgenlibrary.components.AmountTextFieldBasic
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Transaction mode enum to distinguish between send and request flows
 */
enum class TransactionMode {
    SEND,
    REQUEST
}

/**
 * Screen state for the unified transaction overlay
 */
private enum class TransactionScreenState {
    AMOUNT_INPUT,
    TOKEN_SEARCH
}

/**
 * Route composable for UnifiedTransactionOverlay with ViewModel injection
 */
@Composable
fun UnifiedTransactionOverlayRoute(
    mode: TransactionMode,
    onDismiss: () -> Unit,
    onSendTransaction: (TransactionRequest) -> Unit = {},
    onSendRequest: (TransactionRequest) -> Unit = {},
    primaryColor: Color,
    secondaryColor: Color,
    viewModel: ChatSendViewModel = hiltViewModel()
) {
    val assetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val sendTransactionTriggered by viewModel.sendTransactionTriggered.collectAsStateWithLifecycle()
    val sendRequestTriggered by viewModel.sendRequestTriggered.collectAsStateWithLifecycle()
    val conversationState by viewModel.conversation.collectAsStateWithLifecycle()
    
    val recipientDisplay = remember(conversationState) {
        if (conversationState is ConversationUiState.Success) {
            (conversationState as ConversationUiState.Success).conversation.getHeader()
        } else ""
    }
    
    // Use getOtherRecipientAddress() to get the contact's address, not the user's own address
    val recipientAddress = remember(conversationState) {
        if (conversationState is ConversationUiState.Success) {
            (conversationState as ConversationUiState.Success).conversation.getOtherRecipientAddress() ?: ""
        } else ""
    }
    
    // Track the completion callback to be triggered by terminal button
    var pendingTransactionRequest by remember { mutableStateOf<TransactionRequest?>(null) }
    var terminalActionTriggered by remember { mutableStateOf(false) }
    
    // Track if terminal button was pressed to trigger action for SEND mode
    LaunchedEffect(sendTransactionTriggered) {
        if (sendTransactionTriggered && mode == TransactionMode.SEND) {
            // Terminal button was pressed for send - trigger the action
            terminalActionTriggered = true
            viewModel.resetSendTransactionTrigger()
        }
    }
    
    // Track if terminal button was pressed to trigger action for REQUEST mode
    LaunchedEffect(sendRequestTriggered) {
        if (sendRequestTriggered && mode == TransactionMode.REQUEST) {
            // Terminal button was pressed for request - trigger the action
            terminalActionTriggered = true
            viewModel.resetSendRequestTrigger()
        }
    }
    
    // Display terminal button when overlay opens (like WalletManager pattern)
    // Terminal button is shown immediately when overlay appears
    // Also display the send LED matrix pattern on secondary screen
    LaunchedEffect(mode) {
        when (mode) {
            TransactionMode.SEND -> {
                viewModel.onScreenOpened()
                viewModel.displaySendLedPattern()
            }
            TransactionMode.REQUEST -> viewModel.onRequestScreenOpened()
        }
    }
    
    // Ensure terminal button is removed and LED matrix is cleared when composable is disposed
    DisposableEffect(mode) {
        onDispose {
            when (mode) {
                TransactionMode.SEND -> {
                    viewModel.onScreenClosed()
                    viewModel.clearLedMatrix()
                }
                TransactionMode.REQUEST -> viewModel.onRequestScreenClosed()
            }
        }
    }
    
    UnifiedTransactionOverlay(
        mode = mode,
        onDismiss = onDismiss,
        onComplete = { request ->
            when (mode) {
                TransactionMode.SEND -> onSendTransaction(request)
                TransactionMode.REQUEST -> onSendRequest(request)
            }
        },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        assetsUiState = assetsUiState,
        recipientDisplay = recipientDisplay,
        recipientAddress = recipientAddress,
        terminalActionTriggered = terminalActionTriggered,
        onTerminalActionConsumed = { terminalActionTriggered = false },
        onReadyToSendChanged = { ready ->
            // This callback can be used for additional state tracking if needed
            // Terminal button display is now handled by LaunchedEffect above
        }
    )
}

/**
 * Unified overlay for sending and requesting transactions.
 * Uses GlobeBackground with token search and amount input.
 * 
 * @param mode Whether this is a SEND or REQUEST transaction
 * @param onDismiss Callback when overlay is dismissed
 * @param onComplete Callback when transaction is completed
 * @param primaryColor Primary color for styling
 * @param secondaryColor Secondary color for styling
 * @param assetsUiState Current state of user's assets
 * @param recipientDisplay Display name/address for recipient
 * @param recipientAddress Actual address for recipient
 * @param terminalActionTriggered Whether the terminal button was pressed
 * @param onTerminalActionConsumed Callback when the terminal action has been consumed
 * @param onReadyToSendChanged Callback when ready state changes
 */
@Composable
fun UnifiedTransactionOverlay(
    mode: TransactionMode,
    onDismiss: () -> Unit,
    onComplete: (TransactionRequest) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    assetsUiState: AssetsUiState,
    recipientDisplay: String,
    recipientAddress: String,
    terminalActionTriggered: Boolean = false,
    onTerminalActionConsumed: () -> Unit = {},
    onReadyToSendChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    
    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()
    }
    
    // Current screen state - start with AMOUNT_INPUT
    var currentScreen by remember { mutableStateOf(TransactionScreenState.AMOUNT_INPUT) }
    
    // Token search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var selectedChainId by remember { mutableStateOf<Int?>(null) }
    var isChainSelectorVisible by remember { mutableStateOf(false) }
    
    // Token loading state
    var ownedTokens by remember { mutableStateOf<List<OwnedTokenProviderContract.OwnedTokenData>>(emptyList()) }
    var isLoadingTokens by remember { mutableStateOf(true) }
    
    // Form state
    var amount by remember { mutableStateOf("") }
    var selectedToken by remember { mutableStateOf<OwnedTokenProviderContract.OwnedTokenData?>(null) }
    var description by remember { mutableStateOf("") }
    var useMaxAmount by remember { mutableStateOf(false) }
    
    val isReadyToComplete = amount.isNotEmpty() && selectedToken != null
    
    // Notify parent when ready state changes
    LaunchedEffect(isReadyToComplete) {
        onReadyToSendChanged(isReadyToComplete)
    }
    
    // Handle terminal button action - trigger complete when terminal button is pressed
    LaunchedEffect(terminalActionTriggered) {
        if (terminalActionTriggered && isReadyToComplete && selectedToken != null) {
            // Terminal button was pressed and we're ready to complete
            val request = buildTransactionRequest(
                amount = amount,
                token = selectedToken!!.toTokenOption(),
                recipientAddress = recipientAddress,
                description = description.ifEmpty { null },
                mode = mode
            )
            onComplete(request)
            onTerminalActionConsumed()
            // For SEND mode, don't dismiss - let the parent handle showing TransactionStatusOverlay
            // For REQUEST mode, dismiss immediately as there's no transaction to wait for
            if (mode == TransactionMode.REQUEST) {
                onDismiss()
            }
        } else if (terminalActionTriggered) {
            // Terminal button pressed but not ready - consume the action
            onTerminalActionConsumed()
        }
    }
    
    // Load tokens from WalletManager
    LaunchedEffect(Unit) {
        isLoadingTokens = true
        withContext(Dispatchers.IO) {
            try {
                val tokens = OwnedTokenProviderContract.getAllOwnedTokens(context.contentResolver)
                // Sort by adjusted fiat value (balance adjusted for decimals * price)
                ownedTokens = tokens.sortedByDescending { token ->
                    val adjustedBalance = adjustBalanceForDecimals(token.balance, token.decimals, token)
                    adjustedBalance.toDouble() * token.price
                }
                Log.d("UnifiedTransactionOverlay", "Loaded ${tokens.size} tokens")
                
                // Set default token to first available token
                if (selectedToken == null && tokens.isNotEmpty()) {
                    selectedToken = tokens.first()
                }
            } catch (e: Exception) {
                Log.e("UnifiedTransactionOverlay", "Error loading tokens", e)
            }
        }
        isLoadingTokens = false
    }
    
    
    // Filter tokens based on search query
    val filteredTokens = remember(ownedTokens, searchQuery, mode, selectedChainId) {
        // For SEND mode, only show owned tokens (adjusted balance > 0)
        val baseTokens = when (mode) {
            TransactionMode.SEND -> ownedTokens.filter { token ->
                val adjustedBalance = adjustBalanceForDecimals(token.balance, token.decimals, token)
                adjustedBalance > BigDecimal.ZERO
            }
            TransactionMode.REQUEST -> ownedTokens
        }
        
        val chainFilteredTokens = if (selectedChainId == null) {
            baseTokens
        } else {
            baseTokens.filter { it.chainId == selectedChainId }
        }

        if (searchQuery.isBlank()) {
            chainFilteredTokens
        } else {
            chainFilteredTokens.filter { token ->
                token.name.contains(searchQuery, ignoreCase = true) ||
                token.symbol.contains(searchQuery, ignoreCase = true) ||
                token.contractAddress.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Determine title based on mode and screen
    val title = when (currentScreen) {
        TransactionScreenState.TOKEN_SEARCH -> "SELECT TOKEN"
        TransactionScreenState.AMOUNT_INPUT -> {
            val tokenSymbol = selectedToken?.symbol?.uppercase() ?: "TOKEN"
            when (mode) {
                TransactionMode.SEND -> "SEND TOKEN"
                TransactionMode.REQUEST -> "REQUEST TOKEN"
            }
        }
    }
    
    val isValidAmount = amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDouble() > 0
    
    GlobeBackground(
        focusManager = focusManager,
        interactionSource = interactionSource,
        primaryColor = primaryColor,
        title = title,
        onDismiss = {
            // If on token search, go back to amount input
            // If on amount input, dismiss the overlay
            if (currentScreen == TransactionScreenState.TOKEN_SEARCH) {
                currentScreen = TransactionScreenState.AMOUNT_INPUT
                searchQuery = ""
            } else {
                onDismiss()
            }
        }
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "TransactionScreenTransition"
        ) { screen ->
            when (screen) {
                TransactionScreenState.TOKEN_SEARCH -> {
                    TokenSearchContent(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        isSearchFocused = isSearchFocused,
                        onSearchFocusChanged = { isSearchFocused = it },
                        tokens = filteredTokens,
                        isLoading = isLoadingTokens,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        gifEnabledLoader = gifEnabledLoader,
                        mode = mode,
                        selectedChainId = selectedChainId,
                        showChainButton = mode == TransactionMode.REQUEST,
                        onNetworkClick = { isChainSelectorVisible = true },
                        onNavigateBack = {
                            currentScreen = TransactionScreenState.AMOUNT_INPUT
                            searchQuery = ""
                        },
                        onTokenSelected = { token ->
                            selectedToken = token
                            amount = ""
                            useMaxAmount = false
                            currentScreen = TransactionScreenState.AMOUNT_INPUT
                        }
                    )
                }
                
                TransactionScreenState.AMOUNT_INPUT -> {
                    AmountInputContent(
                        mode = mode,
                        amount = amount,
                        onAmountChange = { amount = it },
                        selectedToken = selectedToken,
                        description = description,
                        onDescriptionChange = { description = it },
                        isValidAmount = isValidAmount,
                        useMaxAmount = useMaxAmount,
                        onMaxClick = {
                            selectedToken?.let { token ->
                                // Use adjusted balance (human-readable format)
                                val adjustedBalance = adjustBalanceForDecimals(token.balance, token.decimals, token)
                                amount = adjustedBalance.stripTrailingZeros().toPlainString()
                                useMaxAmount = true
                            }
                        },
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        onSelectToken = {
                            currentScreen = TransactionScreenState.TOKEN_SEARCH
                            searchQuery = ""
                        },
                        onComplete = {
                            selectedToken?.let { token ->
                                val request = buildTransactionRequest(
                                    amount = amount,
                                    token = token.toTokenOption(),
                                    recipientAddress = recipientAddress,
                                    description = description.ifEmpty { null },
                                    mode = mode
                                )
                                onComplete(request)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }

        ChainSelectorOverlay(
            isVisible = isChainSelectorVisible,
            selectedChainId = selectedChainId,
            onChainSelected = { selectedChainId = it },
            onDismiss = { isChainSelectorVisible = false },
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }
}

/**
 * Token search content with search field and token list
 */
@Composable
private fun TokenSearchContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchFocused: Boolean,
    onSearchFocusChanged: (Boolean) -> Unit,
    tokens: List<OwnedTokenProviderContract.OwnedTokenData>,
    isLoading: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    gifEnabledLoader: ImageLoader,
    mode: TransactionMode,
    selectedChainId: Int?,
    showChainButton: Boolean,
    onNetworkClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onTokenSelected: (OwnedTokenProviderContract.OwnedTokenData) -> Unit
) {
    val scrollState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search field styled like NewConversationSheet with background
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            DgenSearchBar(
                searchValue = searchQuery,
                onSearchValueChange = { onSearchQueryChange(it) },
                focusedSearch = isSearchFocused,
                onFocusChanged = { onSearchFocusChanged(it) },
                textColor = primaryColor,
                backgroundColor = secondaryColor,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                onClear = { onSearchQueryChange("") },
                onNavigateBack = onNavigateBack,
                selectedChainId = selectedChainId,
                onNetworkClick = onNetworkClick,
                showChainButton = showChainButton
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Token list
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    DgenLoadingMatrix(
                        activeLEDColor = primaryColor,
                        unactiveLEDColor = secondaryColor
                    )
                }
            }
            
            tokens.isEmpty() && searchQuery.isNotEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tokens found for \"${searchQuery}\"",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            fontSize = 14.sp,
                            color = dgenWhite.copy(alpha = 0.6f)
                        )
                    )
                }
            }
            
            tokens.isEmpty() -> {
                InfoScreen(
                    primaryColor = primaryColor,
                    description = "No assets available"
                )
            }
            
            else -> {
                TokenListView(
                    tokens = tokens,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    scrollState = scrollState,
                    onTokenSelected = onTokenSelected
                )
            }
        }
    }
}

/**
 * Token list view with gradient fades - styled like WalletManager TokenSelectorOverlay
 */
@Composable
private fun TokenListView(
    tokens: List<OwnedTokenProviderContract.OwnedTokenData>,
    primaryColor: Color,
    secondaryColor: Color,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    onTokenSelected: (OwnedTokenProviderContract.OwnedTokenData) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .verticalLazyListScrollbar(
                    scrollState,
                    scrollBarTrackColor = secondaryColor,
                    scrollBarColor = primaryColor,
                    endPadding = (-12).dp,
                    autoHide = true,
                    fadeInDuration = 300,
                    fadeOutDuration = 300,
                    hideDelay = 1000L
                )
                .fillMaxSize()
                .background(Color.Transparent),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            items(tokens, key = { "${it.contractAddress}_${it.chainId}" }) { token ->
                TokenRow(
                    token = token,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    onClick = { onTokenSelected(token) }
                )
            }
        }
        
        // Top gradient fade
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(dgenBlack, Color.Transparent)
                    )
                )
                .zIndex(3f)
        )
        
        // Bottom gradient fade
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, dgenBlack)
                    )
                )
                .zIndex(3f)
        )
    }
}

/**
 * Token row item styled like WalletManager TokenRow
 */
@Composable
private fun TokenRow(
    token: OwnedTokenProviderContract.OwnedTokenData,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    // Adjust balance for decimals (convert from raw to human-readable)
    val adjustedBalance = adjustBalanceForDecimals(token.balance, token.decimals, token)
    val isOwned = adjustedBalance > BigDecimal.ZERO
    
    // Recalculate fiat value using adjusted balance
    val adjustedFiatValue = adjustedBalance.toDouble() * token.price
    
    val tokenOption = token.toTokenOption()
    
    // Truncate symbol for display
    val displaySymbol = truncateSymbol(token.symbol)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Token logo with chain overlay
        TokenLogoWithChain(
            token = tokenOption,
            size = 40.dp,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            showChainOverlay = true
        )
        
        // Token name and symbol
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = token.name,
                fontFamily = PitagonsSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = dgenWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 200.dp)
            )
            
            Text(
                text = "\$$displaySymbol",
                fontFamily = PitagonsSans,
                color = dgenWhite.copy(alpha = pulseOpacity),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Balance and value (if owned)
        if (isOwned) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${formatTokenBalance(adjustedBalance)} $displaySymbol",
                    fontFamily = PitagonsSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                if (adjustedFiatValue > 0.0) {
                    Text(
                        text = "\$${formatFiatValue(adjustedFiatValue)}",
                        fontFamily = PitagonsSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Token selector component styled like WalletManager TokenSelector
 * Shows token logo with chain badge, symbol, and dropdown arrow
 */
@Composable
private fun TokenSelector(
    token: OwnedTokenProviderContract.OwnedTokenData?,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = true
) {
    Row(
        modifier = modifier
            .then(
                if (isSelectable) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .widthIn(max = 132.dp)
            .background(
                Color.Transparent,
                RoundedCornerShape(3.dp)
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (token != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Token image with chain overlay
                TokenLogoWithChain(
                    token = token.toTokenOption(),
                    size = 32.dp,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    showChainOverlay = true
                )
                
                // Token symbol - truncate if longer than 10 chars
                val displaySymbol = truncateSymbol(token.symbol)
                val tokenSymbol = if (token.symbol == "ETH") "ETH" else "\$$displaySymbol"
                Text(
                    text = tokenSymbol,
                    fontFamily = SpaceMono,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = if (isSelectable) 56.dp else 80.dp)
                )
            }
        } else {
            Text(
                text = "SELECT",
                fontSize = 16.sp,
                fontFamily = SpaceMono,
                color = primaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 80.dp)
            )
        }

        // Show chevron only if selectable
        if (isSelectable) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select token",
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Amount input content for transaction
 */
@Composable
private fun AmountInputContent(
    mode: TransactionMode,
    amount: String,
    onAmountChange: (String) -> Unit,
    selectedToken: OwnedTokenProviderContract.OwnedTokenData?,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isValidAmount: Boolean,
    useMaxAmount: Boolean,
    onMaxClick: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onSelectToken: () -> Unit,
    onComplete: () -> Unit
) {
    val amountFocusRequester = remember { FocusRequester() }
    
    // Request focus when the screen appears to open keyboard automatically
    LaunchedEffect(Unit) {
        delay(100) // Small delay to ensure the composable is fully composed
        amountFocusRequester.requestFocus()
    }
    
    // Calculate adjusted balance and fiat value for selected token
    val adjustedBalance = selectedToken?.let { token ->
        adjustBalanceForDecimals(token.balance, token.decimals, token)
    }
    val adjustedFiatValue = adjustedBalance?.let { balance ->
        balance.toDouble() * (selectedToken?.price ?: 0.0)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Amount input section using AmountTextFieldBasic with TokenSelector
        AmountTextFieldBasic(
            currentAmount = amount,
            currentFiatAmount = selectedToken?.let { 
                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                String.format("%.2f", amountDouble * it.price) 
            } ?: "0.00",
            formattedMaxAmount = adjustedBalance?.let { formatTokenBalance(it) } ?: "0.00",
            formattedMaxFiatAmount = adjustedFiatValue?.let { formatFiatValue(it) } ?: "0.00",
            useMaxAmount = useMaxAmount,
            title = "AMOUNT",
            showMaxAmount = mode == TransactionMode.SEND,
            secondaryContent = { isSelectable ->
                TokenSelector(
                    token = selectedToken,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    onClick = onSelectToken,
                    isSelectable = isSelectable
                )
            },
            onAmountChange = { newAmount, _ ->
                onAmountChange(newAmount)
            },
            onMaxClick = onMaxClick,
            readOnly = false,
            maxClickable = mode == TransactionMode.SEND,
            secondarySelectable = true,
            primaryColor = primaryColor,
            focusRequester = amountFocusRequester
        )
        
        // Spacer pushes description to the bottom
        Spacer(modifier = Modifier.weight(1f))
        
        // Description section - positioned at the bottom
        DescriptionSection(
            description = description,
            onDescriptionChange = onDescriptionChange,
            title = "NOTE",
            placeholder = "Add a note (optional)",
            primaryColor = primaryColor,
            maxLines = 2,
            maxLength = 100
        )
    }
}

// Utility functions

/**
 * Truncates token symbol to max 10 characters with "..." if longer
 */
private fun truncateSymbol(symbol: String, maxLength: Int = 10): String {
    return if (symbol.length > maxLength) {
        symbol.take(maxLength) + "..."
    } else {
        symbol
    }
}

/**
 * Adjusts token balance by decimals to convert from raw (wei) to human-readable format.
 * ETH (native token) balances from the content provider are already in the correct format.
 * ERC20 tokens need to be divided by 10^decimals.
 */
private fun adjustBalanceForDecimals(rawBalance: BigDecimal, decimals: Int, token: OwnedTokenProviderContract.OwnedTokenData): BigDecimal {
    // Check if this is ETH (native token) - balances are already in the correct format
    val isEthToken = token.contractAddress.lowercase() == "0x0000000000000000000000000000000000000000" ||
                     token.symbol.uppercase() == "ETH" ||
                     token.name.uppercase() == "ETHEREUM" ||
                     token.contractAddress == token.chainId.toString()
    
    // For ETH, return the balance as-is (already correct)
    if (isEthToken) {
        return rawBalance
    }
    
    // For other tokens, adjust by decimals
    return if (decimals == 0) {
        rawBalance
    } else {
        rawBalance.divide(BigDecimal.TEN.pow(decimals), decimals, java.math.RoundingMode.DOWN)
    }
}

/**
 * Formats token balance using the same approach as WalletManager (formatWithSuffix).
 * Supports K, M, B, T suffixes for large values.
 * For values < 1000: shows up to maxDecimals decimal places.
 */
private fun formatTokenBalance(balance: BigDecimal): String {
    return formatWithSuffix(balance.toDouble(), maxDecimals = 4)
}

/**
 * Formats fiat value matching WalletManager's TokenRow style.
 * Returns just the formatted number (caller adds "$" prefix).
 */
private fun formatFiatValue(value: Double): String {
    return formatWithSuffix(value, maxDecimals = 2)
}

/**
 * Formats numbers with K/M/B/T suffixes for large values - matches WalletManager's formatWithSuffix.
 * - For values >= 1000: uses K, M, B, T suffixes with 2 decimals
 * - For values < 1000: uses up to maxDecimals
 * - Uses HALF_UP rounding
 * - Doesn't round tiny numbers to zero (falls back to full precision)
 */
private fun formatWithSuffix(value: Double, maxDecimals: Int = 4): String {
    val absValue = kotlin.math.abs(value)
    
    val (divisor, suffix) = when {
        absValue >= 1_000_000_000_000 -> 1_000_000_000_000.0 to "T"
        absValue >= 1_000_000_000     -> 1_000_000_000.0     to "B"
        absValue >= 1_000_000         -> 1_000_000.0         to "M"
        absValue >= 1_000             -> 1_000.0             to "K"
        else                          -> 1.0                 to ""
    }
    
    val decimals = if (suffix.isNotEmpty()) 2 else maxDecimals
    val scaled = value / divisor
    val bd = BigDecimal.valueOf(scaled)
    
    // Avoid rounding tiny numbers to zero
    val scaledAndRounded = if (suffix.isNotEmpty()) {
        // With suffix (e.g., "K", "M") – we can safely round to the desired decimals
        bd.setScale(decimals, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
    } else {
        // Without suffix (values < 1K): be careful not to round tiny numbers down to zero
        val candidate = bd.setScale(decimals, java.math.RoundingMode.HALF_UP)
        if (candidate.compareTo(BigDecimal.ZERO) == 0 && bd.compareTo(BigDecimal.ZERO) != 0) {
            // Rounding wiped out all significant digits – fall back to full precision
            bd.stripTrailingZeros()
        } else {
            candidate.stripTrailingZeros()
        }
    }
    
    return scaledAndRounded.toPlainString() + suffix
}

private fun buildTransactionRequest(
    amount: String,
    token: TokenOption,
    recipientAddress: String,
    description: String?,
    mode: TransactionMode
): TransactionRequest {
    val amountDouble = amount.toDoubleOrNull() ?: 0.0
    val amountInBaseUnits = BigDecimal(amountDouble)
        .multiply(BigDecimal.TEN.pow(token.decimals))
        .toBigInteger()
    val valueHex = "0x${amountInBaseUnits.toString(16)}"
    
    return if (token.contractAddress == null) {
        // Native token transfer
        TransactionRequest(
            chainId = token.chainId,
            calls = listOf(
                TransactionCall(
                    to = recipientAddress,
                    value = valueHex,
                    data = "0x"
                )
            ),
            metadata = TransactionMetadata(
                description = description,
                tokenSymbol = token.symbol,
                tokenAmount = amount,
                tokenDecimals = token.decimals
            )
        )
    } else {
        // ERC20 transfer - encode using web3j FunctionEncoder (like WalletManager)
        val data = encodeErc20Transfer(recipientAddress, amountInBaseUnits)
        
        TransactionRequest(
            chainId = token.chainId,
            calls = listOf(
                TransactionCall(
                    to = token.contractAddress,
                    value = "0x0",
                    data = data
                )
            ),
            metadata = TransactionMetadata(
                description = description,
                tokenSymbol = token.symbol,
                tokenAmount = amount,
                tokenDecimals = token.decimals
            )
        )
    }
}

/**
 * Encodes an ERC20 transfer function call using web3j's FunctionEncoder.
 * 
 * The transfer function signature is: transfer(address recipient, uint256 amount)
 * Function selector: 0xa9059cbb
 * 
 * @param recipientAddress The address to transfer tokens to
 * @param amount The amount of tokens to transfer (in base units/wei)
 * @return The encoded function call data as a hex string with 0x prefix
 */
private fun encodeErc20Transfer(recipientAddress: String, amount: BigInteger): String {
    val function = Function(
        "transfer",
        listOf(
            Address(recipientAddress),
            Uint256(amount)
        ),
        emptyList() // Return types not needed for encoding
    )
    return FunctionEncoder.encode(function)
}
