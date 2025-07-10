package org.ethereumhpone.chat.components


import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.pulseOpacity
import com.example.dgenlibrary.ui.theme.smalllabel_fontSize
import org.ethereumhpone.chat.AssetsUiState
import org.ethereumhpone.chat.ChatSendViewModel
import org.ethereumhpone.chat.OverlayContactScreen
import org.ethereumhpone.chat.RecipientUiState
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.chat.util.abbreviateNumber
import org.ethereumhpone.chat.util.formatSmart
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.components.DgenBasicTextfield
import org.ethereumphone.dgenlibrary.components.TextToggle
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.model.Recipient
import org.ethosmobile.components.library.models.TransferItem
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import org.ethereumphone.dgenlibrary.components.SelectableCarousel
import org.ethereumphone.dgenlibrary.formatWithSuffix
import org.ethereumphone.dgenlibrary.screens.InformationScreen
import org.ethereumhpone.chat.ConversationUiState

@Composable
fun OverlaySendScreenRoute(
    onBackClick: () -> Unit,
    onDone: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    recipientUiState: RecipientUiState,
    viewModel: ChatSendViewModel = hiltViewModel(),
){

    val assetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()

    val conversationState by viewModel.conversation.collectAsStateWithLifecycle()
    val recipientDisplay = remember(conversationState) {
        if (conversationState is ConversationUiState.Success) {
            (conversationState as ConversationUiState.Success).conversation.getHeader()
        } else ""
    }

    // Ensure QR code removed when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onScreenClosed()
        }
    }

    OverlaySendScreen(
        onBackClick = onBackClick,
        onDone = onDone,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaySendScreen(
    onBackClick: () -> Unit,
    onDone: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    assetsUiState: AssetsUiState,
    recipientDisplay: String,
    onReadyToSendChanged: (Boolean) -> Unit
) {

    val context = LocalContext.current
    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if ( SDK_INT >= 28 ) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()
    }
    val scrollState = rememberLazyListState()
    var max by remember { mutableStateOf(0.0) }
    var amount by remember { mutableStateOf(TextFieldValue()) }
    var to by remember { mutableStateOf(TextFieldValue())}
    var token by remember { mutableStateOf("")}
    var useDollarAmount by remember { mutableStateOf(false) }
    var dollarAmount by remember { mutableStateOf(TextFieldValue())}
    var fiatPrice by remember { mutableStateOf(0.0) }

    var readyToSend by remember { mutableStateOf(false)}

    // Notify parent when readyToSend changes
    LaunchedEffect(readyToSend) {
        onReadyToSendChanged(readyToSend)
    }
    
    var title = when(readyToSend){
        false ->  "SELECT TOKEN"
        true -> "SEND $${token.uppercase()}"
    }

    val recipientAddress = recipientDisplay


    Column(Modifier
        .fillMaxSize()
        .background(dgenBlack),
            verticalArrangement =  Arrangement.Top) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){

            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(min = 10.dp, max = 250.dp)
                ,
            )

            IconButton(onClick = {
                if(readyToSend){
                    readyToSend = false
                } else {
                    onBackClick()
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.baseline_close_24),
                    contentDescription = "BackButton",
                    modifier = Modifier.size(32.dp),
                    tint = primaryColor
                )
            }
        }

        AnimatedContent(
            modifier = Modifier,
            targetState = readyToSend,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, 300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            }
        ) { targetState ->
            if (!targetState){
                when(assetsUiState) {
                    AssetsUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            DgenLoadingMatrix(
                                activeLEDColor = primaryColor,
                                unactiveLEDColor = secondaryColor
                            )
                        }
                    }
                    AssetsUiState.Empty -> {

                        InformationScreen(
                            gifEnabledLoader = gifEnabledLoader,
                            primaryColor = primaryColor,
                            text = "No Assets available"
                        )
                    }
                    is AssetsUiState.Success -> {
                        val assets = assetsUiState.assets
                        Box {
                            LazyColumn(
                                state = scrollState,
                                modifier = Modifier
                                    .verticalLazyListScrollbar(
                                        scrollState, fixed = true,
                                        scrollBarTrackColor = secondaryColor,
                                        scrollBarColor = primaryColor,
                                    )
                                    .fillMaxSize()
                                    .background(dgenBlack),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item { Spacer(Modifier.height(1.dp)) }
                                items(assets.size) { index ->
                                    val asset = assets[index]
                                    val tokenPrice = asset.price
                                    AvailableToken(
                                        logoUrl = asset.logoUrl ?: "",
                                        name = asset.symbol,
                                        balance = asset.balance,
                                        fiatamount = tokenPrice,
                                        modifier = Modifier
                                            .padding(start = 24.dp, end = 40.dp)
                                            .pointerInput(Unit) {
                                                detectTapGestures {
                                                    token = asset.symbol
                                                    max = asset.balance
                                                    fiatPrice = tokenPrice
                                                    readyToSend = true
                                                }
                                            },
                                        primaryColor = primaryColor
                                    )
                                }
                                item { Spacer(Modifier.height(8.dp)) }
                            }
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .align(Alignment.TopCenter)
                                    .background(
                                        Brush.verticalGradient(listOf(dgenBlack, Color.Transparent))
                                    )
                            )
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
                        InformationScreen(
                            gifEnabledLoader = gifEnabledLoader,
                            primaryColor = primaryColor,
                            text = "Empty"
                        )
                    }
                }

            }
            else{
                Column (
//                    verticalArrangement =  Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp, horizontal = 32.dp),
                ){
                    Spacer(Modifier.height(24.dp))
                     Column(
                         verticalArrangement = Arrangement.spacedBy(24.dp)
                     ) {
                         Column(modifier = Modifier.fillMaxWidth()) {
                             Row(
                                 verticalAlignment = Alignment.CenterVertically,
                                 horizontalArrangement = Arrangement.spacedBy(8.dp)
                             ) {
                                 Text(
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
                                     modifier = Modifier.offset(y=-2.dp),
                                     textLeft = token.uppercase(),
                                     textRight = "$",
                                     onToggle = { useDollarAmount = !useDollarAmount },
                                     value = useDollarAmount,
                                     primaryColor = primaryColor
                                 )
                             }

                             Crossfade(
                                 targetState = useDollarAmount,
                                 animationSpec = tween(300),
                             ) { isDollar ->
                                 if (isDollar) {
                                     DgenBasicTextfield(
                                         placeholder = {
                                             Text(
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
                                             // Allow only numbers and a single dot
                                             if (text.isEmpty() || text.matches("^\\d*\\.?\\d*\$".toRegex())) {
                                                 dollarAmount = value
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
                                             Text(
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
                                             // Allow only numbers and a single dot, no commas or spaces
                                             if (text.isEmpty() || text.matches("^\\d*\\.?\\d*\$".toRegex())) {
                                                 amount = value
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
                             val availableAmountText = if (useDollarAmount) {
                                 val dollarValue = max * fiatPrice
                                 val decimalFormat = DecimalFormat("0.00").apply {
                                     decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
                                 }
                                 "$" + decimalFormat.format(dollarValue)
                             } else {
                                 abbreviateNumber(max)
                             }
                             Text(
                                 text = buildAnnotatedString {
                                     append(availableAmountText)
                                     withStyle(style = SpanStyle(
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

                            // Spacer & SelectableCarousel for choosing the chain of the selected token
                            Spacer(modifier = Modifier.height(16.dp))

                            // Build list of chains where the current token is available
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

                                            // Update available balance & price based on chosen chain
                                            val assetForChain = if (newChainId != null && assetsUiState is AssetsUiState.Success) {
                                                assetsUiState.assets.firstOrNull {
                                                    it.chainId == newChainId && it.symbol.equals(token, ignoreCase = true)
                                                }
                                            } else null

                                            assetForChain?.let {
                                                max = it.balance
                                                fiatPrice = it.price
                                            }
                                        }
                                    )


                            }
                         }

                         Column(modifier = Modifier.fillMaxWidth()) {

                             Text(
                                 text= "TO",
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

                             Text(
                                 text = recipientAddress,
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
                }
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableTextGrid(
    items: List<String>,
    preselectedIndex: Int = -1,
    onSelectionChanged: (selectedIndex: Int) -> Unit = {},
    primaryColor: Color
) {
    // rememberSaveable if you want this to survive process death / config changes
    var selectedIndex by remember { mutableStateOf(preselectedIndex) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
    ) {
        itemsIndexed(items) { index, text ->

            RecipientName(
                selected = (index == selectedIndex),
                name = text,
                onClick = {
                    selectedIndex = index
                    onSelectionChanged(index)
                },
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
fun RecipientName(
    selected: Boolean,
    name: String,
    onClick: () -> Unit,
    primaryColor: Color
){
    Text(
        modifier = Modifier.pointerInput(Unit){
            detectTapGestures {
                onClick()
            }
        },
        text = name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = SpaceMono,
            color = primaryColor.copy(alpha = if (selected) 1f else pulseOpacity),
            fontWeight = FontWeight.SemiBold,
            fontSize = label_fontSize
        )
    )
}

@Composable
fun AvailableToken(
    modifier: Modifier = Modifier,
    logoUrl: String = "",
    name: String = "",
    balance: Double = 0.0,
    fiatamount: Double = 0.0,
    primaryColor: Color
) {
    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            when(logoUrl != ""){
                true ->{
                    AsyncImage(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(32.dp),
                        model = logoUrl,
                        contentDescription = "Translated description of what the image contains"
                    )
                }
                false -> {
                    Image(
                        modifier = Modifier
                            .size(32.dp),
                        painter = painterResource(org.ethereumhpone.chat.R.drawable.unknown_token),
                        contentDescription = "Ethereum"
                    )

                }
            }
            Text(
                text = name,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(min = 10.dp, max = 250.dp)
                ,
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(

                buildAnnotatedString {
                    append(balance.formatWithSuffix())

                    append("  ")

                    withStyle(style = SpanStyle(
                        fontFamily = PitagonsSans,
                        color = primaryColor.copy(0.7f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                    ) {
                        append("$" + decimalFormat.format(balance * fiatamount))
                    }

                },
                fontFamily = PitagonsSans,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )



        }
    }
}



@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun OverlaySendScreenPreview(){
//    OverlaySendScreen(
//        {}, {}, dgenTurqoise,
//        secondaryColor = dgenOcean
//    )
}

// Utility mapping functions for chain abbreviations <-> chainId used by SelectableCarousel
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
