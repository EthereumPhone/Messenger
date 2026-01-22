package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethereumphone.dgenlibrary.theme.dgenBlack

@Composable
fun ChainSelectorOverlay(
    isVisible: Boolean,
    selectedChainId: Int?,
    onChainSelected: (Int?) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val chains = listOf(
        null to "ALL CHAINS",
        8453 to "BASE",
        1 to "MAINNET",
        10 to "OPTIMISM",
        137 to "POLYGON",
        42161 to "ARBITRUM"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(dgenBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SecondaryScreenHeader(
                    primaryColor = primaryColor,
                    title = "SELECT CHAIN",
                    onDismiss = onDismiss
                )

                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(chains) { (chainId, chainName) ->
                            ChainRow(
                                chainId = chainId,
                                chainName = chainName,
                                isSelected = selectedChainId == chainId,
                                primaryColor = primaryColor,
                                onClick = {
                                    onChainSelected(chainId)
                                    onDismiss()
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(dgenBlack, dgenBlack, Color.Transparent)
                                )
                            )
                            .zIndex(3f)
                    )

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
        }
    }
}

@Composable
private fun ChainRow(
    chainId: Int?,
    chainName: String,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 16.dp, bottom = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChainIcon(
            chainId = chainId,
            size = 32.dp
        )

        Text(
            text = chainName,
            style = TextStyle(
                fontFamily = SpaceMono,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            ),
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(primaryColor, shape = RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun ChainIcon(
    chainId: Int?,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val iconModifier = modifier
        .size(size)
        .clip(RoundedCornerShape(4.dp))

    when (chainId) {
        1 -> Icon(
            painter = painterResource(id = R.drawable.mainnet),
            contentDescription = "Ethereum",
            modifier = iconModifier,
            tint = Color.Unspecified
        )
        10 -> Icon(
            painter = painterResource(id = R.drawable.optimism),
            contentDescription = "Optimism",
            modifier = iconModifier,
            tint = Color.Unspecified
        )
        137 -> Icon(
            painter = painterResource(id = R.drawable.polygon),
            contentDescription = "Polygon",
            modifier = iconModifier,
            tint = Color.Unspecified
        )
        8453 -> Icon(
            painter = painterResource(id = R.drawable.base),
            contentDescription = "Base",
            modifier = iconModifier,
            tint = Color.Unspecified
        )
        42161 -> Icon(
            painter = painterResource(id = R.drawable.arbitrum),
            contentDescription = "Arbitrum",
            modifier = iconModifier,
            tint = Color.Unspecified
        )
        else -> Icon(
            painter = painterResource(id = R.drawable.mainnet),
            contentDescription = "All Chains",
            modifier = iconModifier,
            tint = Color.Unspecified
        )
    }
}

fun getChainName(chainId: Int?): String {
    return when (chainId) {
        1 -> "ETH"
        10 -> "OP"
        137 -> "POL"
        8453 -> "BASE"
        42161 -> "ARB"
        null -> "ALL"
        else -> "ALL"
    }
}
