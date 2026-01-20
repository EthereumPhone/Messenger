package org.ethereumhpone.chat.components

import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.pulseOpacity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.AmountTextFieldBasic
import org.ethereumphone.dgenlibrary.components.DgenBasicTextfield
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenGreen
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.TransactionCall
import org.ethereumphone.model.TransactionMetadata
import org.ethereumphone.model.TransactionRequest
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Data class representing a token option for sending transaction requests.
 */
data class TokenOption(
    val symbol: String,
    val name: String,
    val decimals: Int,
    val contractAddress: String? = null, // null for native token
    val chainId: Long,
    val logoUrl: String? = null,
    val balance: BigDecimal = BigDecimal.ZERO,
    val price: Double = 0.0
) {
    val fiatValue: Double
        get() = balance.toDouble() * price
    
    val formattedBalance: String
        get() = balance.setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
    
    val formattedFiatValue: String
        get() = String.format("%.2f", fiatValue)
}

/**
 * Common tokens for transaction requests (fallback when WalletManager is not available).
 */
object CommonTokens {
    val ETH_MAINNET = TokenOption("ETH", "Ethereum", 18, null, 1L)
    val ETH_BASE = TokenOption("ETH", "Ethereum", 18, null, 8453L)
    val ETH_OPTIMISM = TokenOption("ETH", "Ethereum", 18, null, 10L)
    val ETH_ARBITRUM = TokenOption("ETH", "Ethereum", 18, null, 42161L)
    val USDC_BASE = TokenOption("USDC", "USD Coin", 6, "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913", 8453L)
    val USDC_MAINNET = TokenOption("USDC", "USD Coin", 6, "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", 1L)
    
    val allTokens = listOf(ETH_BASE, USDC_BASE, ETH_MAINNET, USDC_MAINNET, ETH_OPTIMISM, ETH_ARBITRUM)
}

/**
 * Screen state for the transaction request sheet
 */
private enum class SheetScreen {
    AMOUNT_INPUT,
    TOKEN_SEARCH
}

/**
 * Overlay for composing and sending transaction requests.
 * Styled to match WalletManager/TokenLauncher with globe animation background,
 * AmountTextFieldBasic component, and terminal-style action button.
 */
//@Composable
//fun SendTransactionRequestSheet(
//    modifier: Modifier = Modifier,
//    recipientAddress: String,
//    recipientName: String? = null,
//    currentChainId: Long = 8453L,
//    availableTokens: List<TokenOption> = CommonTokens.allTokens.filter { it.chainId == currentChainId },
//    onDismiss: () -> Unit,
//    onSendRequest: (TransactionRequest) -> Unit,
//    primaryColor: Color = SystemColorManager.primaryColor
//) {
//    val context = LocalContext.current
//    val secondaryColor = Color(0xFF1A1A1A)
//    val focusManager = LocalFocusManager.current
//    val interactionSource = remember { MutableInteractionSource() }
//
//    // GIF-enabled image loader for globe animation
//    val gifEnabledLoader = ImageLoader.Builder(context)
//        .components {
//            if (Build.VERSION.SDK_INT >= 28) {
//                add(ImageDecoderDecoder.Factory())
//            } else {
//                add(GifDecoder.Factory())
//            }
//        }.build()
//
//    // Current screen state
//    var currentScreen by remember { mutableStateOf(SheetScreen.AMOUNT_INPUT) }
//
//    // Token loading state
//    var ownedTokens by remember { mutableStateOf<List<OwnedTokenProviderContract.OwnedTokenData>>(emptyList()) }
//    var isLoadingTokens by remember { mutableStateOf(true) }
//    var tokenSearchQuery by remember { mutableStateOf("") }
//
//    // Form state
//    var amount by remember { mutableStateOf("") }
//    var selectedToken by remember { mutableStateOf<TokenOption?>(null) }
//    var description by remember { mutableStateOf("") }
//    var useMaxAmount by remember { mutableStateOf(false) }
//
//    // Load tokens from WalletManager
//    LaunchedEffect(Unit) {
//        isLoadingTokens = true
//        withContext(Dispatchers.IO) {
//            try {
//                val tokens = OwnedTokenProviderContract.getAllOwnedTokens(context.contentResolver)
//                ownedTokens = tokens.sortedByDescending { it.fiatValue }
//
//                // Set default selected token if we have tokens
//                if (tokens.isNotEmpty() && selectedToken == null) {
//                    val tokenOnCurrentChain = tokens.find { it.chainId.toLong() == currentChainId }
//                    selectedToken = (tokenOnCurrentChain ?: tokens.first()).toTokenOption()
//                }
//            } catch (e: Exception) {
//                Log.e("SendTransactionRequestSheet", "Error loading tokens", e)
//            }
//        }
//        isLoadingTokens = false
//    }
//
//    // If no tokens from WalletManager, fallback to default tokens
//    LaunchedEffect(isLoadingTokens, ownedTokens) {
//        if (!isLoadingTokens && ownedTokens.isEmpty() && selectedToken == null) {
//            selectedToken = availableTokens.firstOrNull() ?: CommonTokens.ETH_BASE
//        }
//    }
//
//    // Filter tokens based on search query
//    val filteredTokens = remember(ownedTokens, tokenSearchQuery) {
//        if (tokenSearchQuery.isBlank()) {
//            ownedTokens
//        } else {
//            ownedTokens.filter { token ->
//                token.name.contains(tokenSearchQuery, ignoreCase = true) ||
//                token.symbol.contains(tokenSearchQuery, ignoreCase = true)
//            }
//        }
//    }
//
//    val isValidAmount = amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDouble() > 0
//
//    // Full-screen overlay with globe animation background (matching WalletManager structure)
//    GlobeBackground(
//        focusManager = focusManager,
//        interactionSource = interactionSource,
//        primaryColor = primaryColor,
//        title = "REQUEST TRANSACTION",
//        onDismiss = onDismiss
//    ) {
//        AmountTextFieldBasic(
//            currentAmount = amount,
//            currentFiatAmount = selectedToken?.let { token ->
//                amount.toDoubleOrNull()?.let { amt ->
//                    String.format("%.2f", amt * token.price)
//                }
//            } ?: "",
//            formattedMaxAmount = selectedToken?.formattedBalance ?: "0.00",
//            formattedMaxFiatAmount = selectedToken?.formattedFiatValue ?: "0.00",
//            useMaxAmount = useMaxAmount,
//            title = "AMOUNT",
//            secondaryContent = { isSelectable ->
//                TransactionTokenSelector(
//                    token = selectedToken,
//                    primaryColor = primaryColor,
//                    secondaryColor = secondaryColor,
//                    onClick = { currentScreen = SheetScreen.TOKEN_SEARCH },
//                    isSelectable = isSelectable
//                )
//            },
//            onAmountChange = { newAmount, _ ->
//                amount = newAmount
//                useMaxAmount = false
//            },
//            onMaxClick = {
//                selectedToken?.let { token ->
//                    amount = token.formattedBalance
//                    useMaxAmount = true
//                }
//            },
//            readOnly = false,
//            showMaxAmount = true,
//            maxClickable = true,
//            secondarySelectable = true,
//            primaryColor = primaryColor
//        )
//    }
//
//        // Content - direct sibling of globe, no nested Box
////        AnimatedContent(
////            targetState = currentScreen,
////            transitionSpec = {
////                if (targetState == SheetScreen.TOKEN_SEARCH) {
////                    (fadeIn()) togetherWith
////                            (fadeOut())
////                } else {
////                    (fadeIn()) togetherWith
////                            (fadeOut())
////                }
////            },
////            label = "SheetScreenTransition"
////        ) { screen ->
////            when (screen) {
////                SheetScreen.AMOUNT_INPUT -> {
////                    AmountInputScreen(
////                        recipientAddress = recipientAddress,
////                        recipientName = recipientName,
////                        amount = amount,
////                        onAmountChange = { newAmount, _ ->
////                            amount = newAmount
////                            useMaxAmount = false
////                        },
////                        selectedToken = selectedToken,
////                        description = description,
////                        onDescriptionChange = { description = it },
////                        isValidAmount = isValidAmount,
////                        useMaxAmount = useMaxAmount,
////                        onMaxClick = {
////                            selectedToken?.let { token ->
////                                amount = token.formattedBalance
////                                useMaxAmount = true
////                            }
////                        },
////                        primaryColor = primaryColor,
////                        secondaryColor = secondaryColor,
////                        onDismiss = onDismiss,
////                        onSelectToken = { currentScreen = SheetScreen.TOKEN_SEARCH },
////                        onSendRequest = {
////                            selectedToken?.let { token ->
////                                val request = buildTransactionRequest(
////                                    amount = amount,
////                                    token = token,
////                                    recipientAddress = recipientAddress,
////                                    description = description.ifEmpty { null }
////                                )
////                                onSendRequest(request)
////                                onDismiss()
////                            }
////                        }
////                    )
////                }
////                SheetScreen.TOKEN_SEARCH -> {
////                    TokenSearchScreen(
////                        searchQuery = tokenSearchQuery,
////                        onSearchQueryChange = { tokenSearchQuery = it },
////                        tokens = filteredTokens,
////                        isLoading = isLoadingTokens,
////                        selectedToken = selectedToken,
////                        primaryColor = primaryColor,
////                        secondaryColor = secondaryColor,
////                        onBack = { currentScreen = SheetScreen.AMOUNT_INPUT },
////                        onTokenSelected = { token ->
////                            selectedToken = token.toTokenOption()
////                            amount = ""
////                            useMaxAmount = false
////                            currentScreen = SheetScreen.AMOUNT_INPUT
////                        },
////                        fallbackTokens = availableTokens
////                    )
////                }
////            }
////        }
//
//}

// Note: AmountInputScreen and DescriptionField have been replaced by:
// - RequestTransactionOverlay.kt (full request flow with GlobeBackground)
// - DescriptionSection.kt (reusable description input component)

/**
 * Token search screen - displays user's owned tokens
 */
@Composable
private fun TokenSearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    tokens: List<OwnedTokenProviderContract.OwnedTokenData>,
    isLoading: Boolean,
    selectedToken: TokenOption?,
    primaryColor: Color,
    secondaryColor: Color,
    onBack: () -> Unit,
    onTokenSelected: (OwnedTokenProviderContract.OwnedTokenData) -> Unit,
    fallbackTokens: List<TokenOption>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECT TOKEN",
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(min = 10.dp, max = 250.dp)
            )

            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.baseline_close_24),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp),
                    tint = primaryColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(secondaryColor)
                .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = dgenWhite.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
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
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = dgenWhite.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
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
                            modifier = Modifier.size(32.dp)
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(fallbackTokens) { token ->
                            FallbackTokenCard(
                                token = token,
                                isSelected = selectedToken?.symbol == token.symbol && 
                                            selectedToken?.chainId == token.chainId,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                onClick = {
                                    val ownedToken = OwnedTokenProviderContract.OwnedTokenData(
                                        contractAddress = token.contractAddress ?: "0x0000000000000000000000000000000000000000",
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
 * Simple fallback token card for common tokens when WalletManager is not available
 */
@Composable
private fun FallbackTokenCard(
    token: TokenOption,
    isSelected: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) primaryColor.copy(alpha = 0.15f)
                else secondaryColor
            )
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
            Row(
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
 * Build a TransactionRequest from the sheet inputs.
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
                description = description ?: "Send ${token.symbol}",
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
                description = description ?: "Send ${token.symbol}",
                tokenSymbol = token.symbol,
                tokenAmount = amount,
                tokenDecimals = token.decimals
            )
        )
    }
}

private fun truncateAddress(address: String): String {
    return if (address.length > 12) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
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

@Composable
@Preview
fun SendTransactionRequestSheetPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(dgenBlack)
            .padding(24.dp)
    ) {
        Text(
            text = "Request Transaction Preview",
            style = TextStyle(
                fontFamily = SpaceMono,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = dgenWhite
            )
        )
    }
}
