package org.ethereumhpone.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenGreen
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * A simplified token card for displaying user's owned assets in the Messenger app.
 * Used for token selection when sending transaction requests.
 */
@Composable
fun AssetTokenCard(
    token: OwnedTokenProviderContract.OwnedTokenData,
    primaryColor: Color,
    secondaryColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                primaryColor.copy(alpha = 0.15f)
            } else {
                primaryColor.copy(alpha = 0.05f)
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Token logo
                TokenLogo(
                    logoUrl = token.logo,
                    symbol = token.symbol,
                    chainId = token.chainId,
                    size = 44,
                    primaryColor = primaryColor
                )
                
                // Token name and symbol
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = token.name,
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        )
                        // Chain badge
                        ChainBadge(
                            chainId = token.chainId,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
            
            // Balance and value
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = formatBalance(token.balance),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                if (token.price > 0) {
                    Text(
                        text = formatFiatValue(token.fiatValue),
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Token logo with fallback to initials
 */
@Composable
private fun TokenLogo(
    logoUrl: String?,
    symbol: String,
    chainId: Int,
    size: Int,
    primaryColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size.dp)
    ) {
        if (!logoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = logoUrl,
                contentDescription = "$symbol logo",
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback to initials
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.2f))
            ) {
                Text(
                    text = symbol.take(2).uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size / 3).sp
                    )
                )
            }
        }
    }
}

/**
 * Small chain indicator badge
 */
@Composable
private fun ChainBadge(
    chainId: Int,
    primaryColor: Color
) {
    val chainName = when (chainId) {
        1 -> "ETH"
        10 -> "OP"
        137 -> "MATIC"
        42161 -> "ARB"
        8453 -> "BASE"
        11155111 -> "SEP"
        else -> "C$chainId"
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(
                color = primaryColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = chainName,
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        )
    }
}

/**
 * Format token balance for display
 */
private fun formatBalance(balance: BigDecimal): String {
    val df = DecimalFormat("#,##0.####")
    return if (balance < BigDecimal("0.0001") && balance > BigDecimal.ZERO) {
        "<0.0001"
    } else {
        df.format(balance)
    }
}

/**
 * Format fiat value for display
 */
private fun formatFiatValue(value: Double): String {
    return when {
        value < 0.01 && value > 0 -> "<$0.01"
        value < 1000 -> "$${String.format("%.2f", value)}"
        value < 1000000 -> "$${String.format("%.1fK", value / 1000)}"
        else -> "$${String.format("%.2fM", value / 1000000)}"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
fun AssetTokenCardPreview() {
    val sampleToken = OwnedTokenProviderContract.OwnedTokenData(
        contractAddress = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913",
        decimals = 6,
        name = "USD Coin",
        symbol = "USDC",
        logo = null,
        chainId = 8453,
        swappable = true,
        balance = BigDecimal("150.50"),
        price = 1.0,
        chains = listOf(8453, 1)
    )
    
    Column(
        modifier = Modifier
            .background(Color(0xFF0D0D0D))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AssetTokenCard(
            token = sampleToken,
            primaryColor = dgenGreen,
            secondaryColor = Color.Black,
            isSelected = false,
            onClick = {}
        )
        
        AssetTokenCard(
            token = sampleToken.copy(
                name = "Ethereum",
                symbol = "ETH",
                contractAddress = "0x0000000000000000000000000000000000000000",
                balance = BigDecimal("0.5432"),
                price = 3500.0,
                decimals = 18
            ),
            primaryColor = dgenGreen,
            secondaryColor = Color.Black,
            isSelected = true,
            onClick = {}
        )
    }
}
