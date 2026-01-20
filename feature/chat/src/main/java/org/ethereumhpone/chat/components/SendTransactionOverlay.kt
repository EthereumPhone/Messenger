package org.ethereumhpone.chat.components

import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.pulseOpacity
import com.example.dgenlibrary.ui.theme.smalllabel_fontSize
import org.ethereumhpone.chat.AssetsUiState
import org.ethereumhpone.chat.ChatSendViewModel
import org.ethereumhpone.chat.ConversationUiState
import org.ethereumhpone.chat.RecipientUiState
import org.ethereumphone.dgenlibrary.components.DgenBasicTextfield
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import org.ethereumphone.dgenlibrary.components.SelectableCarousel
import org.ethereumphone.dgenlibrary.components.TextToggle
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumhpone.chat.components.TerminalActionButton
import org.ethereumphone.dgenlibrary.screens.InfoScreen
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.chat.util.abbreviateNumber
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Screen state for the send transaction overlay
 */
private enum class SendScreenState {
    TOKEN_SELECTION,
    AMOUNT_INPUT
}

/**
 * Route composable for SendTransactionOverlay with ViewModel injection
 */
@Composable
fun SendTransactionOverlayRoute(
    onDismiss: () -> Unit,
    onSendTransaction: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    recipientUiState: RecipientUiState,
    viewModel: ChatSendViewModel = hiltViewModel()
) {
    val assetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val sendTransactionTriggered by viewModel.sendTransactionTriggered.collectAsStateWithLifecycle()
    val conversationState by viewModel.conversation.collectAsStateWithLifecycle()
    
    val recipientDisplay = remember(conversationState) {
        if (conversationState is ConversationUiState.Success) {
            (conversationState as ConversationUiState.Success).conversation.getHeader()
        } else ""
    }
    
    // Track if terminal button was pressed to trigger send
    LaunchedEffect(sendTransactionTriggered) {
        if (sendTransactionTriggered) {
            onSendTransaction()
        }
    }
    
    // Ensure screen state is cleaned when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onScreenClosed()
        }
    }
    
    SendTransactionOverlay(
        onDismiss = onDismiss,
        onSendTransaction = onSendTransaction,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        assetsUiState = assetsUiState,
        recipientDisplay = recipientDisplay,
        onReadyToSendChanged = { ready ->
            if (ready) {
                viewModel.onScreenOpened()
            } else {
                viewModel.onScreenClosed()
            }
        }
    )
}

/**
 * Overlay for sending transactions.
 * Uses GlobeBackground with token list selection similar to TokenSearchScreen.
 */
@Composable
fun SendTransactionOverlay(
    onDismiss: () -> Unit,
    onSendTransaction: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    assetsUiState: AssetsUiState,
    recipientDisplay: String,
    onReadyToSendChanged: (Boolean) -> Unit,
    showDebugAction: Boolean = false,
    onDebugSend: (String) -> Unit = {}
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
    
    val scrollState = rememberLazyListState()
    var currentScreen by remember { mutableStateOf(SendScreenState.TOKEN_SELECTION) }
    
    // Transaction parameters
    var max by remember { mutableStateOf(0.0) }
    var amount by remember { mutableStateOf(TextFieldValue()) }
    var token by remember { mutableStateOf("") }
    var useDollarAmount by remember { mutableStateOf(false) }
    var dollarAmount by remember { mutableStateOf(TextFieldValue()) }
    var fiatPrice by remember { mutableStateOf(0.0) }
    var selectedTokenIndex by remember { mutableStateOf(-1) }
    
    val isReadyToSend = currentScreen == SendScreenState.AMOUNT_INPUT
    
    // Notify parent when ready state changes
    LaunchedEffect(isReadyToSend) {
        onReadyToSendChanged(isReadyToSend)
    }
    
    val title = when (currentScreen) {
        SendScreenState.TOKEN_SELECTION -> "SELECT TOKEN"
        SendScreenState.AMOUNT_INPUT -> "SEND ${token.uppercase()}"
    }
    
    GlobeBackground(
        focusManager = focusManager,
        interactionSource = interactionSource,
        primaryColor = primaryColor,
        title = title,
        onDismiss = {
            if (currentScreen == SendScreenState.AMOUNT_INPUT) {
                currentScreen = SendScreenState.TOKEN_SELECTION
            } else {
                onDismiss()
            }
        }
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, 300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "SendScreenTransition"
        ) { targetState ->
            when (targetState) {
                SendScreenState.TOKEN_SELECTION -> {
                    TokenSelectionContent(
                        assetsUiState = assetsUiState,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        gifEnabledLoader = gifEnabledLoader,
                        scrollState = scrollState,
                        selectedTokenIndex = selectedTokenIndex,
                        onTokenSelected = { index, asset ->
                            selectedTokenIndex = index
                            token = asset.symbol
                            max = asset.balance
                            fiatPrice = asset.price
                            currentScreen = SendScreenState.AMOUNT_INPUT
                        }
                    )
                }
                
                SendScreenState.AMOUNT_INPUT -> {
                    val amountText = if (useDollarAmount) dollarAmount.text else amount.text
                    val displayAmount = if (useDollarAmount && amountText.isNotBlank()) {
                        "$$amountText"
                    } else {
                        amountText
                    }
                    val displayToken = if (token.isNotBlank()) token.uppercase() else "TOKEN"
                    val debugMessage = if (displayAmount.isNotBlank()) {
                        "Send $displayAmount $displayToken to $recipientDisplay"
                    } else {
                        "Send $displayToken to $recipientDisplay"
                    }

                    AmountInputContent(
                        amount = amount,
                        onAmountChange = { amount = it },
                        dollarAmount = dollarAmount,
                        onDollarAmountChange = { dollarAmount = it },
                        useDollarAmount = useDollarAmount,
                        onToggleDollarAmount = { useDollarAmount = !useDollarAmount },
                        token = token,
                        max = max,
                        fiatPrice = fiatPrice,
                        recipientDisplay = recipientDisplay,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        assetsUiState = assetsUiState,
                        onChainSelected = { newMax, newPrice ->
                            max = newMax
                            fiatPrice = newPrice
                        },
                        showDebugAction = showDebugAction,
                        debugEnabled = amountText.isNotBlank() && token.isNotBlank(),
                        onDebugSend = { onDebugSend(debugMessage) }
                    )
                }
            }
        }
    }
}

/**
 * Token selection content - displays list of user's tokens
 */
@Composable
private fun TokenSelectionContent(
    assetsUiState: AssetsUiState,
    primaryColor: Color,
    secondaryColor: Color,
    gifEnabledLoader: ImageLoader,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    selectedTokenIndex: Int,
    onTokenSelected: (Int, org.ethereumphone.model.TokenAsset) -> Unit
) {
    when (assetsUiState) {
        AssetsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                DgenLoadingMatrix(
                    activeLEDColor = primaryColor,
                    unactiveLEDColor = secondaryColor
                )
            }
        }
        
        AssetsUiState.Empty -> {
            InfoScreen(
                gifEnabledLoader = gifEnabledLoader,
                primaryColor = primaryColor,
                description = "No Assets available"
            )
        }
        
        is AssetsUiState.Success -> {
            val assets = assetsUiState.assets
            
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .verticalLazyListScrollbar(
                            scrollState,
                            scrollBarTrackColor = secondaryColor,
                            scrollBarColor = primaryColor,
                            autoHide = true,
                            fadeInDuration = 300,
                            fadeOutDuration = 300,
                            hideDelay = 1000L
                        )
                        .fillMaxSize()
                        .background(Color.Transparent),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 16.dp
                    )
                ) {
                    items(assets.size) { index ->
                        val asset = assets[index]
                        val tokenData = asset.toOwnedTokenData()
                        
                        AssetTokenCard(
                            token = tokenData,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            isSelected = selectedTokenIndex == index,
                            onClick = {
                                onTokenSelected(index, asset)
                            }
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
                            Brush.verticalGradient(listOf(dgenBlack, Color.Transparent))
                        )
                )
                
                // Bottom gradient fade
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, dgenBlack))
                        )
                )
            }
        }
        
        AssetsUiState.Error -> {
            InfoScreen(
                gifEnabledLoader = gifEnabledLoader,
                primaryColor = primaryColor,
                description = "Error loading assets from WalletManager"
            )
        }
    }
}

/**
 * Amount input content - shows amount entry and recipient info
 */
@Composable
private fun AmountInputContent(
    amount: TextFieldValue,
    onAmountChange: (TextFieldValue) -> Unit,
    dollarAmount: TextFieldValue,
    onDollarAmountChange: (TextFieldValue) -> Unit,
    useDollarAmount: Boolean,
    onToggleDollarAmount: () -> Unit,
    token: String,
    max: Double,
    fiatPrice: Double,
    recipientDisplay: String,
    primaryColor: Color,
    secondaryColor: Color,
    assetsUiState: AssetsUiState,
    onChainSelected: (Double, Double) -> Unit,
    showDebugAction: Boolean,
    debugEnabled: Boolean,
    onDebugSend: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount section
            Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "AMOUNT",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = primaryColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = label_fontSize,
                        lineHeight = label_fontSize,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
                TextToggle(
                    modifier = Modifier.offset(y = (-2).dp),
                    textLeft = token.uppercase(),
                    textRight = "$",
                    onToggle = { onToggleDollarAmount() },
                    value = useDollarAmount,
                    primaryColor = primaryColor
                )
            }
            
            Crossfade(
                targetState = useDollarAmount,
                animationSpec = tween(300),
                label = "AmountTypeCrossfade"
            ) { isDollar ->
                if (isDollar) {
                    DgenBasicTextfield(
                        placeholder = {
                            androidx.compose.material3.Text(
                                text = "$0.0",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = primaryColor.copy(alpha = pulseOpacity),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 56.sp
                                )
                            )
                        },
                        value = dollarAmount,
                        onValueChange = { value ->
                            val text = value.text
                            if (text.isEmpty() || text.matches("^\\d*\\.?\\d*$".toRegex())) {
                                onDollarAmountChange(value)
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 56.sp,
                            lineHeight = 56.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        maxLines = 1,
                        keyboardtype = KeyboardType.Decimal,
                        cursorWidth = 32.dp,
                        cursorColor = primaryColor,
                        isAnyFieldFocused = remember { mutableStateOf(false) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    DgenBasicTextfield(
                        placeholder = {
                            androidx.compose.material3.Text(
                                text = "0.0",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = primaryColor.copy(alpha = pulseOpacity),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 56.sp
                                )
                            )
                        },
                        value = amount,
                        onValueChange = { value ->
                            val text = value.text
                            if (text.isEmpty() || text.matches("^\\d*\\.?\\d*$".toRegex())) {
                                onAmountChange(value)
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 56.sp,
                            lineHeight = 56.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        maxLines = 1,
                        keyboardtype = KeyboardType.Decimal,
                        cursorWidth = 32.dp,
                        cursorColor = primaryColor,
                        isAnyFieldFocused = remember { mutableStateOf(false) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Available amount display
            val availableAmountText = if (useDollarAmount) {
                val dollarValue = max * fiatPrice
                val decimalFormat = DecimalFormat("0.00").apply {
                    decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
                }
                "$" + decimalFormat.format(dollarValue)
            } else {
                abbreviateNumber(max)
            }
            
            androidx.compose.material3.Text(
                text = buildAnnotatedString {
                    append(availableAmountText)
                    withStyle(
                        style = SpanStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = smalllabel_fontSize,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        if (useDollarAmount) {
                            append(" available")
                        } else {
                            append(" $token available")
                        }
                    }
                },
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = smalllabel_fontSize,
                    lineHeight = smalllabel_fontSize,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chain selector carousel
            val chainItems = remember(token, assetsUiState) {
                if (token.isNotEmpty() && assetsUiState is AssetsUiState.Success) {
                    assetsUiState.assets
                        .filter { it.symbol.equals(token, ignoreCase = true) }
                        .map { chainIdToAbbrev(it.chainId) }
                        .distinct()
                } else {
                    emptyList()
                }
            }
            
            var selectedChainIndex by remember(token) { mutableStateOf(0) }
            
            if (chainItems.isNotEmpty()) {
                SelectableCarousel(
                    items = chainItems,
                    itemWidth = 65.dp,
                    itemHeight = 65.dp,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    initialSelectedIndex = selectedChainIndex,
                    onItemSelected = { newIndex ->
                        if (newIndex != null) {
                            selectedChainIndex = newIndex
                        }
                        val selectedAbbrev = newIndex?.let { chainItems.getOrNull(it) }
                        val newChainId = selectedAbbrev?.let { abbrevToChainId(it) }
                        
                        val assetForChain = if (newChainId != null && assetsUiState is AssetsUiState.Success) {
                            assetsUiState.assets.firstOrNull {
                                it.chainId == newChainId && it.symbol.equals(token, ignoreCase = true)
                            }
                        } else null
                        
                        assetForChain?.let {
                            onChainSelected(it.balance, it.price)
                        }
                    }
                )
            }
        }
        
            // Recipient section
            Column(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.Text(
                    text = "TO",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = primaryColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = label_fontSize,
                        lineHeight = label_fontSize,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                androidx.compose.material3.Text(
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
            
            if (showDebugAction) {
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        if (showDebugAction) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                TerminalActionButton(
                    text = "SEND",
                    icon = org.ethereumphone.dgenlibrary.R.drawable.ic_send_request,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    onClick = onDebugSend,
                    enabled = debugEnabled,
                    isActive = debugEnabled
                )
            }
        }
    }
}

// Utility mapping functions for chain abbreviations
private fun chainIdToAbbrev(chainId: Int): String = when (chainId) {
    1 -> "main"
    10 -> "op"
    42161 -> "arb"
    137 -> "pol"
    8453 -> "base"
    7777777 -> "zora"
    else -> "main"
}

private fun abbrevToChainId(abbrev: String): Int = when (abbrev) {
    "main" -> 1
    "op" -> 10
    "arb" -> 42161
    "pol" -> 137
    "base" -> 8453
    "zora" -> 7777777
    else -> 1
}

/**
 * Extension function to convert TokenAsset to OwnedTokenData
 */
private fun org.ethereumphone.model.TokenAsset.toOwnedTokenData(): OwnedTokenProviderContract.OwnedTokenData {
    return OwnedTokenProviderContract.OwnedTokenData(
        contractAddress = this.address,
        decimals = this.decimals,
        name = this.name,
        symbol = this.symbol,
        logo = this.logoUrl,
        chainId = this.chainId,
        swappable = this.swappable,
        balance = BigDecimal.valueOf(this.balance),
        price = this.price,
        chains = listOf(this.chainId)
    )
}
