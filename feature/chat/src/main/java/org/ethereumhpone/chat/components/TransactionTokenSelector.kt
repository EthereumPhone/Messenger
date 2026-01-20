package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono

/**
 * Token selector component matching the WalletManager/TokenLauncher design.
 * Displays the selected token with logo and symbol, or "SELECT" if no token is selected.
 * 
 * @param token The currently selected token (or null if none)
 * @param primaryColor Primary accent color
 * @param secondaryColor Secondary/background color
 * @param onClick Callback when the selector is clicked
 * @param modifier Modifier for the composable
 * @param isSelectable Whether the selector should be interactive
 */
@Composable
fun TransactionTokenSelector(
    token: TokenOption?,
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
                val isNativeToken = token.contractAddress == null ||
                        token.contractAddress == "0x0000000000000000000000000000000000000000" ||
                        token.symbol.uppercase() == "ETH"

                TokenLogoWithChain(
                    token = token,
                    size = 32.dp,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    showChainOverlay = isNativeToken
                )
                
                // Token symbol with $ prefix for non-ETH tokens
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
