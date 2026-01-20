package org.ethereumhpone.chat.components

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.AmountTextFieldBasic
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.TransactionCall
import org.ethereumphone.model.TransactionMetadata
import org.ethereumphone.model.TransactionRequest
import java.math.BigDecimal

/**
 * Screen state for the request transaction overlay
 */
private enum class RequestScreenState {
    AMOUNT_INPUT,
    TOKEN_SEARCH
}

/**
 * Overlay for requesting transactions.
 * Uses GlobeBackground with AmountTextFieldBasic and DescriptionSection.
 * 
 * @param recipientAddress The address to request from
 * @param recipientName Optional display name for the recipient
 * @param currentChainId Current blockchain chain ID
 * @param availableTokens List of available tokens for selection
 * @param onDismiss Callback when overlay is dismissed
 * @param onSendRequest Callback when request is sent
 * @param primaryColor Primary color for styling
 */
@Composable
fun RequestTransactionOverlay(
    modifier: Modifier = Modifier,
    recipientAddress: String,
    recipientName: String? = null,
    currentChainId: Long = 8453L,
    availableTokens: List<TokenOption> = CommonTokens.allTokens.filter { it.chainId == currentChainId },
    onDismiss: () -> Unit,
    onSendRequest: (TransactionRequest) -> Unit,
    primaryColor: Color = SystemColorManager.primaryColor
) {
    val context = LocalContext.current
    val secondaryColor = Color(0xFF1A1A1A)
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // Current screen state
    var currentScreen by remember { mutableStateOf(RequestScreenState.AMOUNT_INPUT) }
    
    // Token loading state
    var ownedTokens by remember { mutableStateOf<List<OwnedTokenProviderContract.OwnedTokenData>>(emptyList()) }
    var isLoadingTokens by remember { mutableStateOf(true) }
    var tokenSearchQuery by remember { mutableStateOf("") }
    
    // Form state
    var amount by remember { mutableStateOf("") }
    var selectedToken by remember { mutableStateOf<TokenOption?>(null) }
    var description by remember { mutableStateOf("") }
    var useMaxAmount by remember { mutableStateOf(false) }
    
    // Load tokens from WalletManager
    LaunchedEffect(Unit) {
        isLoadingTokens = true
        withContext(Dispatchers.IO) {
            try {
                val tokens = OwnedTokenProviderContract.getAllOwnedTokens(context.contentResolver)
                ownedTokens = tokens.sortedByDescending { it.fiatValue }
                
                // Set default selected token if we have tokens
                if (tokens.isNotEmpty() && selectedToken == null) {
                    val tokenOnCurrentChain = tokens.find { it.chainId.toLong() == currentChainId }
                    selectedToken = (tokenOnCurrentChain ?: tokens.first()).toTokenOption()
                }
            } catch (e: Exception) {
                Log.e("RequestTransactionOverlay", "Error loading tokens", e)
            }
        }
        isLoadingTokens = false
    }
    
    // Fallback to default tokens if none loaded
    LaunchedEffect(isLoadingTokens, ownedTokens) {
        if (!isLoadingTokens && ownedTokens.isEmpty() && selectedToken == null) {
            selectedToken = availableTokens.firstOrNull() ?: CommonTokens.ETH_BASE
        }
    }
    
    // Filter tokens based on search query
    val filteredTokens = remember(ownedTokens, tokenSearchQuery) {
        if (tokenSearchQuery.isBlank()) {
            ownedTokens
        } else {
            ownedTokens.filter { token ->
                token.name.contains(tokenSearchQuery, ignoreCase = true) ||
                token.symbol.contains(tokenSearchQuery, ignoreCase = true)
            }
        }
    }
    
    val isValidAmount = amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDouble() > 0
    
    val title = when (currentScreen) {
        RequestScreenState.AMOUNT_INPUT -> "REQUEST ${selectedToken?.symbol?.uppercase() ?: "TOKEN"}"
        RequestScreenState.TOKEN_SEARCH -> "SELECT TOKEN"
    }
    
    val recipientDisplay = recipientName?.takeIf { it.isNotBlank() } ?: recipientAddress
    
    GlobeBackground(
        modifier = modifier,
        focusManager = focusManager,
        interactionSource = interactionSource,
        primaryColor = primaryColor,
        title = title,
        onDismiss = {
            if (currentScreen == RequestScreenState.TOKEN_SEARCH) {
                currentScreen = RequestScreenState.AMOUNT_INPUT
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
            label = "RequestScreenTransition"
        ) { screen ->
            when (screen) {
                RequestScreenState.AMOUNT_INPUT -> {
                    RequestAmountInputContent(
                        amount = amount,
                        onAmountChange = { newAmount, _ ->
                            amount = newAmount
                            useMaxAmount = false
                        },
                        selectedToken = selectedToken,
                        recipientDisplay = recipientDisplay,
                        description = description,
                        onDescriptionChange = { description = it },
                        isValidAmount = isValidAmount,
                        useMaxAmount = useMaxAmount,
                        onMaxClick = {
                            selectedToken?.let { token ->
                                amount = token.formattedBalance
                                useMaxAmount = true
                            }
                        },
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        onSelectToken = { currentScreen = RequestScreenState.TOKEN_SEARCH },
                        onSendRequest = {
                            selectedToken?.let { token ->
                                val request = buildTransactionRequest(
                                    amount = amount,
                                    token = token,
                                    recipientAddress = recipientAddress,
                                    description = description.ifEmpty { null }
                                )
                                onSendRequest(request)
                                onDismiss()
                            }
                        }
                    )
                }
                
                RequestScreenState.TOKEN_SEARCH -> {
                    RequestTokenSearchContent(
                        searchQuery = tokenSearchQuery,
                        onSearchQueryChange = { tokenSearchQuery = it },
                        tokens = filteredTokens,
                        isLoading = isLoadingTokens,
                        selectedToken = selectedToken,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        onTokenSelected = { token ->
                            selectedToken = token.toTokenOption()
                            amount = ""
                            useMaxAmount = false
                            currentScreen = RequestScreenState.AMOUNT_INPUT
                        },
                        fallbackTokens = availableTokens
                    )
                }
            }
        }
    }
}

/**
 * Amount input content for request transaction
 */
@Composable
private fun RequestAmountInputContent(
    amount: String,
    onAmountChange: (String, Boolean) -> Unit,
    selectedToken: TokenOption?,
    recipientDisplay: String,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isValidAmount: Boolean,
    useMaxAmount: Boolean,
    onMaxClick: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onSelectToken: () -> Unit,
    onSendRequest: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount Input using AmountTextFieldBasic
            AmountTextFieldBasic(
                currentAmount = amount,
                currentFiatAmount = selectedToken?.let { token ->
                    amount.toDoubleOrNull()?.let { amt ->
                        String.format("%.2f", amt * token.price)
                    }
                } ?: "",
                formattedMaxAmount = selectedToken?.formattedBalance ?: "",
                formattedMaxFiatAmount = selectedToken?.formattedFiatValue ?: "",
                useMaxAmount = useMaxAmount,
                title = "AMOUNT",
                secondaryContent = { isSelectable ->
                    TransactionTokenSelector(
                        token = selectedToken,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        onClick = onSelectToken,
                        isSelectable = isSelectable
                    )
                },
                onAmountChange = onAmountChange,
                onMaxClick = onMaxClick,
                readOnly = false,
                showMaxAmount = false,
                maxClickable = false,
                secondarySelectable = true,
                primaryColor = primaryColor
            )
            
            // Description section using the new DescriptionSection component
            DescriptionSection(
                description = description,
                onDescriptionChange = onDescriptionChange,
                title = "NOTE",
                placeholder = "Add a note (optional)",
                primaryColor = primaryColor,
                maxLines = 2,
                maxLength = 100
            )
            
            // Spacer to push content toward top
            Spacer(modifier = Modifier.weight(1f))
            
            // Space for floating button
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        // Floating send button at bottom center
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            TerminalActionButton(
                text = "REQUEST",
                icon = org.ethereumphone.dgenlibrary.R.drawable.ic_send_request,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                onClick = onSendRequest,
                enabled = isValidAmount && selectedToken != null,
                isActive = isValidAmount && selectedToken != null
            )
        }
    }
}

@Composable
private fun RequestRecipientSection(
    recipientDisplay: String,
    primaryColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "FROM",
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = recipientDisplay,
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Token search content for request transaction
 */
@Composable
private fun RequestTokenSearchContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    tokens: List<OwnedTokenProviderContract.OwnedTokenData>,
    isLoading: Boolean,
    selectedToken: TokenOption?,
    primaryColor: Color,
    secondaryColor: Color,
    onTokenSelected: (OwnedTokenProviderContract.OwnedTokenData) -> Unit,
    fallbackTokens: List<TokenOption>
) {
    val scrollState = rememberLazyListState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = dgenWhite.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    textStyle = TextStyle(
                        fontFamily = PitagonsSans,
                        fontSize = 16.sp,
                        color = dgenWhite
                    ),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search tokens...",
                                    style = TextStyle(
                                        fontFamily = PitagonsSans,
                                        fontSize = 16.sp,
                                        color = dgenWhite.copy(alpha = 0.4f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = dgenWhite.copy(alpha = 0.5f)
                        )
                    }
                }
            }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.height(32.dp)
                        )
                        Text(
                            text = "Loading your assets...",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                fontSize = 14.sp,
                                color = dgenWhite.copy(alpha = 0.6f)
                            )
                        )
                    }
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
                        text = "No tokens found for \"$searchQuery\"",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            fontSize = 14.sp,
                            color = dgenWhite.copy(alpha = 0.6f)
                        )
                    )
                }
            }
            
            tokens.isEmpty() -> {
                // No tokens from WalletManager - show fallback tokens
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "No assets found. You can still request these common tokens:",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            fontSize = 13.sp,
                            color = dgenWhite.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        state = scrollState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(fallbackTokens) { token ->
                            FallbackTokenCardItem(
                                token = token,
                                isSelected = selectedToken?.symbol == token.symbol &&
                                        selectedToken?.chainId == token.chainId,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                onClick = {
                                    val ownedToken = OwnedTokenProviderContract.OwnedTokenData(
                                        contractAddress = token.contractAddress
                                            ?: "0x0000000000000000000000000000000000000000",
                                        decimals = token.decimals,
                                        name = token.name,
                                        symbol = token.symbol,
                                        logo = token.logoUrl,
                                        chainId = token.chainId.toInt(),
                                        swappable = false,
                                        balance = BigDecimal.ZERO,
                                        price = 0.0,
                                        chains = listOf(token.chainId.toInt())
                                    )
                                    onTokenSelected(ownedToken)
                                }
                            )
                        }
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    state = scrollState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tokens) { token ->
                        AssetTokenCard(
                            token = token,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            isSelected = selectedToken?.symbol == token.symbol &&
                                    selectedToken?.chainId == token.chainId.toLong(),
                            onClick = { onTokenSelected(token) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fallback token card for common tokens
 */
@Composable
private fun FallbackTokenCardItem(
    token: TokenOption,
    isSelected: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Token logo
        TokenLogoWithChain(
            token = token,
            size = 40.dp,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            showChainOverlay = true
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = token.name,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            )
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = token.symbol,
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenWhite.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = chainIdToName(token.chainId),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = primaryColor.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

/**
 * Build a TransactionRequest from the overlay inputs.
 */
private fun buildTransactionRequest(
    amount: String,
    token: TokenOption,
    recipientAddress: String,
    description: String?
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
                description = description ?: "Request ${token.symbol}",
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
                description = description ?: "Request ${token.symbol}",
                tokenSymbol = token.symbol,
                tokenAmount = amount,
                tokenDecimals = token.decimals
            )
        )
    }
}

private fun chainIdToName(chainId: Long): String = when (chainId) {
    1L -> "Ethereum"
    10L -> "Optimism"
    137L -> "Polygon"
    42161L -> "Arbitrum"
    8453L -> "Base"
    11155111L -> "Sepolia"
    else -> "Chain $chainId"
}
