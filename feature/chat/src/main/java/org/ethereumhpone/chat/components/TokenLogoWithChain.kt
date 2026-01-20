package org.ethereumhpone.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.R

/**
 * Token logo composable that can display a chain overlay when needed.
 * Adapted for the Messenger project's TokenOption data class.
 * 
 * @param token The TokenOption to display
 * @param size The size of the main logo
 * @param primaryColor Primary color for text and borders
 * @param secondaryColor Secondary color for backgrounds
 * @param showChainOverlay Whether to show the chain overlay
 * @param modifier Modifier for the composable
 */
@Composable
fun TokenLogoWithChain(
    token: TokenOption,
    size: Dp = 32.dp,
    primaryColor: Color,
    secondaryColor: Color,
    showChainOverlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Main token logo
        if (isEthToken(token)) {
            // Use ETH placeholder for ETH tokens
            Icon(
                painter = painterResource(id = R.drawable.mainnet),
                contentDescription = "ETH",
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(3.dp)),
                tint = Color.Unspecified // Keep original colors
            )
        } else if (!token.logoUrl.isNullOrEmpty()) {
            // Use provided image URL with error handling
            val context = LocalContext.current
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(token.logoUrl)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = token.name,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(3.dp)),
                contentScale = ContentScale.Crop,
                error = {
                    // Fallback to symbol text on error
                    TokenSymbolFallback(
                        symbol = token.symbol,
                        size = size,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor
                    )
                },
                loading = {
                    // Show placeholder while loading
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(secondaryColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = token.symbol.take(2).uppercase(),
                            fontSize = (size.value * 0.4).sp,
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor.copy(alpha = 0.5f),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            )
        } else {
            // Fallback to symbol text
            TokenSymbolFallback(
                symbol = token.symbol,
                size = size,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }

        // Chain overlay (bottom right corner)
        if (showChainOverlay) {
            val overlaySize = size * 0.3f
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(overlaySize)
                    .graphicsLayer {
                        translationY = 2f
                        translationX = 2f
                    },
                contentAlignment = Alignment.Center
            ) {
                when (token.chainId) {
                    8453L, 84532L -> { // Base mainnet and testnet
                        Image(
                            painter = painterResource(id = R.drawable.base),
                            modifier = Modifier
                                .border(1.dp, secondaryColor, RoundedCornerShape(3.dp))
                                .clip(RoundedCornerShape(3.dp)),
                            contentDescription = "Base Chain",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    10L -> { // Optimism
                        Image(
                            painter = painterResource(id = R.drawable.optimism),
                            modifier = Modifier
                                .border(1.dp, secondaryColor, RoundedCornerShape(3.dp))
                                .clip(RoundedCornerShape(3.dp)),
                            contentDescription = "Optimism Chain",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    1L -> { // Mainnet
                        Image(
                            painter = painterResource(id = R.drawable.mainnet),
                            modifier = Modifier
                                .border(1.dp, secondaryColor, RoundedCornerShape(3.dp))
                                .clip(RoundedCornerShape(3.dp)),
                            contentDescription = "Mainnet",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    42161L -> { // Arbitrum
                        Image(
                            painter = painterResource(id = R.drawable.arbitrum),
                            contentDescription = "Arbitrum Chain",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    137L -> { // Polygon
                        Image(
                            painter = painterResource(id = R.drawable.polygon),
                            modifier = Modifier
                                .border(1.dp, secondaryColor, RoundedCornerShape(3.dp))
                                .clip(RoundedCornerShape(3.dp)),
                            contentDescription = "Polygon",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    7777777L -> { // Zora
                        Image(
                            painter = painterResource(id = R.drawable.zorb),
                            modifier = Modifier
                                .border(1.dp, secondaryColor, CircleShape)
                                .clip(CircleShape),
                            contentDescription = "Zora",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(secondaryColor, RoundedCornerShape(3.dp))
                                .border(1.dp, primaryColor, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "?",
                                fontSize = 8.sp,
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenSymbolFallback(
    symbol: String,
    size: Dp,
    primaryColor: Color,
    secondaryColor: Color
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
            .background(secondaryColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol.take(2).uppercase(),
            fontSize = (size.value * 0.4).sp,
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * Helper function to determine if a token is ETH (native token)
 */
private fun isEthToken(token: TokenOption): Boolean {
    return token.contractAddress == null || 
           token.contractAddress == "0x0000000000000000000000000000000000000000" ||
           token.symbol.uppercase() == "ETH"
}
