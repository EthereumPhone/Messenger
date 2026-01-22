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
import org.ethereumphone.dgenlibrary.screens.InfoScreen
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.TransactionCall
import org.ethereumphone.model.TransactionMetadata
import org.ethereumphone.model.TransactionRequest
import org.ethosmobile.contacts.ui.components.DgenCursorSearchTextfield
import org.ethereumphone.dgenlibrary.components.AmountTextFieldBasic
import java.math.BigDecimal

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
 * Mock common tokens for UI debugging purposes
 */
object MockCommonTokens {
    val tokens = listOf(
        OwnedTokenProviderContract.OwnedTokenData(
            contractAddress = "0x0000000000000000000000000000000000000000",
            decimals = 18,
            name = "Ethereum",
            symbol = "ETH",
            logo = null,
            chainId = 1,
            swappable = true,
            balance = BigDecimal("1.5432"),
            price = 3500.0,
            chains = listOf(1, 8453, 10, 42161)
        ),
        OwnedTokenProviderContract.OwnedTokenData(
            contractAddress = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913",
            decimals = 6,
            name = "USD Coin",
            symbol = "USDC",
            logo = null,
            chainId = 8453,
            swappable = true,
            balance = BigDecimal("250.00"),
            price = 1.0,
            chains = listOf(1, 8453, 10, 42161)
        ),
        OwnedTokenProviderContract.OwnedTokenData(
            contractAddress = "0x0000000000000000000000000000000000000000",
            decimals = 18,
            name = "Ethereum",
            symbol = "ETH",
            logo = null,
            chainId = 8453,
            swappable = true,
            balance = BigDecimal("0.8765"),
            price = 3500.0,
            chains = listOf(1, 8453, 10, 42161)
        ),
        OwnedTokenProviderContract.OwnedTokenData(
            contractAddress = "0xdAC17F958D2ee523a2206206994597C13D831ec7",
            decimals = 6,
            name = "Tether USD",
            symbol = "USDT",
            logo = null,
            chainId = 1,
            swappable = true,
            balance = BigDecimal("500.00"),
            price = 1.0,
            chains = listOf(1, 10, 42161)
        ),
        OwnedTokenProviderContract.OwnedTokenData(
            contractAddress = "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599",
            decimals = 8,
            name = "Wrapped BTC",
            symbol = "WBTC",
            logo = null,
            chainId = 1,
            swappable = true,
            balance = BigDecimal("0.0234"),
            price = 65000.0,
            chains = listOf(1, 10, 42161)
        ),
        OwnedTokenProviderContract.OwnedTokenData(
            contractAddress = "0x0000000000000000000000000000000000000000",
            decimals = 18,
            name = "Ethereum",
            symbol = "ETH",
            logo = null,
            chainId = 10,
            swappable = true,
            balance = BigDecimal("0.4321"),
            price = 3500.0,
            chains = listOf(1, 8453, 10, 42161)
        ),
        OwnedTokenProviderContract.OwnedTokenData(
            contractAddress = "0x7Fc66500c84A76Ad7e9c93437bFc5Ac33E2DDaE9",
            decimals = 18,
            name = "Aave",
            symbol = "AAVE",
            logo = null,
            chainId = 1,
            swappable = true,
            balance = BigDecimal("12.5"),
            price = 180.0,
            chains = listOf(1, 10, 137)
        ),
        OwnedTokenProviderContract.OwnedTokenData(
            contractAddress = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984",
            decimals = 18,
            name = "Uniswap",
            symbol = "UNI",
            logo = null,
            chainId = 1,
            swappable = true,
            balance = BigDecimal("45.0"),
            price = 8.50,
            chains = listOf(1, 10, 42161)
        )
    )
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
    
    val recipientAddress = remember(conversationState) {
        if (conversationState is ConversationUiState.Success) {
            (conversationState as ConversationUiState.Success).conversation.recipients.firstOrNull()?.address ?: ""
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
    LaunchedEffect(mode) {
        when (mode) {
            TransactionMode.SEND -> viewModel.onScreenOpened()
            TransactionMode.REQUEST -> viewModel.onRequestScreenOpened()
        }
    }
    
    // Ensure terminal button is removed when composable is disposed
    DisposableEffect(mode) {
        onDispose {
            when (mode) {
                TransactionMode.SEND -> viewModel.onScreenClosed()
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
            onDismiss()
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
                ownedTokens = tokens.sortedByDescending { it.fiatValue }
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
    
    // Set default token from mock tokens if no real tokens available
    LaunchedEffect(isLoadingTokens, ownedTokens) {
        if (!isLoadingTokens && ownedTokens.isEmpty() && selectedToken == null) {
            selectedToken = MockCommonTokens.tokens.firstOrNull()
        }
    }
    
    // Filter tokens based on search query
    val filteredTokens = remember(ownedTokens, searchQuery, mode, selectedChainId) {
        // For now, use mock tokens for debugging - comment this out to use real tokens
        val tokensToFilter = if (ownedTokens.isEmpty()) {
            MockCommonTokens.tokens
        } else {
            ownedTokens
        }
        
        // For SEND mode, only show owned tokens (balance > 0)
        val baseTokens = when (mode) {
            TransactionMode.SEND -> tokensToFilter.filter { it.balance > BigDecimal.ZERO }
            TransactionMode.REQUEST -> tokensToFilter
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
                TransactionMode.SEND -> "SEND $tokenSymbol"
                TransactionMode.REQUEST -> "REQUEST $tokenSymbol"
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
                                amount = token.balance.toPlainString()
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
                // No assets available - commented out for now, showing mock tokens instead
                // InfoScreen(
                //     gifEnabledLoader = gifEnabledLoader,
                //     primaryColor = primaryColor,
                //     description = "No assets available"
                // )
                
                // Show mock tokens for UI debugging
                TokenListView(
                    tokens = MockCommonTokens.tokens,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    scrollState = scrollState,
                    onTokenSelected = onTokenSelected
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
    val isOwned = token.balance > BigDecimal.ZERO
    val tokenOption = token.toTokenOption()

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
                text = "\$${token.symbol}",
                fontFamily = PitagonsSans,
                color = dgenWhite.copy(alpha = pulseOpacity),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Balance and value (if owned)
        if (isOwned) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${formatTokenBalance(token.balance)} ${token.symbol}",
                    fontFamily = PitagonsSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                if (token.fiatValue > 0.0) {
                    Text(
                        text = "\$${formatFiatValue(token.fiatValue)}",
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
                
                // Token symbol
                val tokenSymbol = if (token.symbol == "ETH") "ETH" else "\$${token.symbol}"
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
            formattedMaxAmount = selectedToken?.balance?.let { formatTokenBalance(it) } ?: "0.00",
            formattedMaxFiatAmount = selectedToken?.fiatValue?.let { formatFiatValue(it) } ?: "0.00",
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
            primaryColor = primaryColor
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

private fun formatTokenBalance(balance: BigDecimal): String {
    return if (balance < BigDecimal("0.0001") && balance > BigDecimal.ZERO) {
        "<0.0001"
    } else {
        balance.setScale(4, java.math.RoundingMode.DOWN).stripTrailingZeros().toPlainString()
    }
}

private fun formatFiatValue(value: Double): String {
    return when {
        value < 0.01 && value > 0 -> "<0.01"
        value < 1000 -> String.format("%.2f", value)
        value < 1000000 -> String.format("%.1fK", value / 1000)
        else -> String.format("%.2fM", value / 1000000)
    }
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
    
    val actionDescription = when (mode) {
        TransactionMode.SEND -> "Send ${token.symbol}"
        TransactionMode.REQUEST -> "Request ${token.symbol}"
    }
    
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
                description = description ?: actionDescription,
                tokenSymbol = token.symbol,
                tokenAmount = amount,
                tokenDecimals = token.decimals
            )
        )
    } else {
        // ERC20 transfer
        val functionSelector = "a9059cbb" // transfer(address,uint256)
        val paddedTo = recipientAddress.removePrefix("0x").lowercase().padStart(64, '0')
        val paddedAmount = amountInBaseUnits.toString(16).padStart(64, '0')
        val data = "0x$functionSelector$paddedTo$paddedAmount"
        
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
                description = description ?: actionDescription,
                tokenSymbol = token.symbol,
                tokenAmount = amount,
                tokenDecimals = token.decimals
            )
        )
    }
}
