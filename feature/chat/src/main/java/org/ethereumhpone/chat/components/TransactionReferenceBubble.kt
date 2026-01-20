package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Token
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import kotlinx.datetime.Instant
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumphone.dgenlibrary.theme.dgenGreen
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.Contact
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import org.ethereumphone.model.TransactionReference
import org.ethereumphone.model.TransactionReferenceMetadata
import org.ethereumphone.model.TransactionTypes
import kotlin.time.Duration.Companion.seconds

/**
 * Composable for rendering a transaction reference message bubble.
 * Displays completed transaction details with a link to the block explorer.
 */
@Composable
fun TransactionReferenceBubble(
    modifier: Modifier = Modifier,
    message: Message,
    transactionReference: TransactionReference,
    isUserMe: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    isFirstMessageByAuthor: Boolean = false
) {
    val uriHandler = LocalUriHandler.current
    val chainName = chainIdToName(transactionReference.networkId)
    val metadata = transactionReference.metadata
    val blockExplorerUrl = metadata?.blockExplorerUrl ?: getBlockExplorerUrl(transactionReference.networkId, transactionReference.reference)
    
    val bubbleShape = RoundedCornerShape(16.dp)
    val bubbleBackground = if (isUserMe) primaryColor.copy(alpha = 0.15f) else secondaryColor.copy(alpha = 0.15f)
    val borderColor = dgenGreen.copy(alpha = 0.5f)
    
    // Get the appropriate icon based on transaction type
    val txIcon = getTransactionIcon(metadata?.transactionType)
    
    Column(
        horizontalAlignment = if (isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier
    ) {
        // Transaction Reference Card
        Column(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleBackground)
                .border(1.dp, borderColor, bubbleShape)
                .clickable { 
                    if (blockExplorerUrl.isNotEmpty()) {
                        uriHandler.openUri(blockExplorerUrl) 
                    }
                }
                .padding(16.dp)
                .width(280.dp)
        ) {
            // Header: Transaction label with success indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = txIcon,
                        contentDescription = "Transaction",
                        tint = dgenGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getTransactionTypeLabel(metadata?.transactionType),
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isUserMe) dgenWhite else primaryColor
                        )
                    )
                }
                
                // Success badge
                SuccessBadge()
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Chain info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Network:",
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        fontSize = 12.sp,
                        color = if (isUserMe) dgenWhite.copy(alpha = 0.7f) else primaryColor.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = chainName,
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = dgenGreen
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Amount display (if available)
            val amount = metadata?.amount
            val currency = metadata?.currency
            val decimals = metadata?.decimals
            if (amount != null && currency != null && decimals != null) {
                val humanAmount = formatAmount(amount, decimals)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = humanAmount,
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = if (isUserMe) dgenWhite else primaryColor
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currency,
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = if (isUserMe) dgenWhite.copy(alpha = 0.8f) else primaryColor.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            
            // From/To addresses (if available)
            val fromAddress = metadata?.fromAddress
            val toAddress = metadata?.toAddress
            if (fromAddress != null || toAddress != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (fromAddress != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "From:",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    fontSize = 11.sp,
                                    color = if (isUserMe) dgenWhite.copy(alpha = 0.6f) else primaryColor.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.width(40.dp)
                            )
                            Text(
                                text = truncateAddress(fromAddress),
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    fontSize = 11.sp,
                                    color = if (isUserMe) dgenWhite.copy(alpha = 0.8f) else primaryColor.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                    if (toAddress != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "To:",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    fontSize = 11.sp,
                                    color = if (isUserMe) dgenWhite.copy(alpha = 0.6f) else primaryColor.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.width(40.dp)
                            )
                            Text(
                                text = truncateAddress(toAddress),
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    fontSize = 11.sp,
                                    color = if (isUserMe) dgenWhite.copy(alpha = 0.8f) else primaryColor.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Transaction hash
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tx: ${truncateAddress(transactionReference.reference)}",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontSize = 11.sp,
                        color = dgenGreen
                    )
                )
                if (blockExplorerUrl.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "View on explorer",
                        tint = dgenGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Timestamp
            AuthorNameTimestamp(
                messageEntity = message,
                isUserMe = isUserMe,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }
    }
}

@Composable
private fun SuccessBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(dgenGreen.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Confirmed",
                tint = dgenGreen,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "CONFIRMED",
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = dgenGreen
                )
            )
        }
    }
}

private fun getTransactionIcon(transactionType: String?): ImageVector {
    return when (transactionType) {
        TransactionTypes.SWAP -> Icons.Outlined.SwapHoriz
        TransactionTypes.TRANSFER -> Icons.Outlined.Token
        else -> Icons.Outlined.Receipt
    }
}

private fun getTransactionTypeLabel(transactionType: String?): String {
    return when (transactionType) {
        TransactionTypes.TRANSFER -> "Transfer"
        TransactionTypes.SWAP -> "Swap"
        TransactionTypes.MINT -> "Mint"
        TransactionTypes.LEND -> "Lend"
        TransactionTypes.BORROW -> "Borrow"
        TransactionTypes.STAKE -> "Stake"
        TransactionTypes.UNSTAKE -> "Unstake"
        TransactionTypes.APPROVE -> "Approval"
        TransactionTypes.CONTRACT_INTERACTION -> "Contract Call"
        else -> "Transaction"
    }
}

private fun formatAmount(amount: Long, decimals: Int): String {
    val divisor = Math.pow(10.0, decimals.toDouble())
    val result = amount.toDouble() / divisor
    return if (result == result.toLong().toDouble()) {
        result.toLong().toString()
    } else {
        String.format("%.6f", result).trimEnd('0').trimEnd('.')
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

private fun getBlockExplorerUrl(networkId: Long, txHash: String): String = when (networkId) {
    1L -> "https://etherscan.io/tx/$txHash"
    10L -> "https://optimistic.etherscan.io/tx/$txHash"
    137L -> "https://polygonscan.com/tx/$txHash"
    42161L -> "https://arbiscan.io/tx/$txHash"
    8453L -> "https://basescan.org/tx/$txHash"
    11155111L -> "https://sepolia.etherscan.io/tx/$txHash"
    else -> ""
}

@Composable
@Preview
fun TransactionReferenceBubblePreview() {
    val recipient = Recipient(
        id = "userA",
        address = "0xAbC123CryptoBroWallet",
        ens = "alice.eth",
        contact = Contact("lk1", "Alice", null, "0x423")
    )
    val now = Instant.parse("2025-04-17T12:23:05Z")
    
    val txReference = TransactionReference(
        namespace = "eip155",
        networkId = 8453,
        reference = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
        metadata = TransactionReferenceMetadata(
            transactionType = TransactionTypes.TRANSFER,
            currency = "ETH",
            amount = 1000000000000000L, // 0.001 ETH
            decimals = 18,
            fromAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f7e5ca",
            toAddress = "0xAbC123CryptoBroWallet000000000000000000",
            blockExplorerUrl = "https://basescan.org/tx/0x1234..."
        )
    )
    
    val message = Message(
        id = "msg1",
        threadId = "thread123",
        recipient = recipient,
        date = now - (5 * 60).seconds,
        dateSent = now - (5 * 60).seconds,
        seen = true,
        deliveryStatus = DeliveryStatus.PUBLISHED,
        replyReference = null,
        isMe = true,
        attachments = emptyList(),
        reactions = emptyList(),
        body = "Transaction Reference",
        transactionReference = txReference
    )
    
    TransactionReferenceBubble(
        message = message,
        transactionReference = txReference,
        isUserMe = true,
        primaryColor = Color(0xFF00FF88),
        secondaryColor = Color(0xFF1A1A2E)
    )
}

@Composable
@Preview
fun TransactionReferenceBubbleSwapPreview() {
    val recipient = Recipient(
        id = "userB",
        address = "0x54321Wallet",
        ens = "bob.eth",
        contact = Contact("lk2", "Bob", null, "0x789")
    )
    val now = Instant.parse("2025-04-17T12:23:05Z")
    
    val txReference = TransactionReference(
        namespace = "eip155",
        networkId = 1,
        reference = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890",
        metadata = TransactionReferenceMetadata(
            transactionType = TransactionTypes.SWAP,
            currency = "ETH → USDC",
            amount = 500000000000000000L, // 0.5 ETH
            decimals = 18,
            fromAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f7e5ca",
            blockExplorerUrl = "https://etherscan.io/tx/0xabcdef..."
        )
    )
    
    val message = Message(
        id = "msg2",
        threadId = "thread123",
        recipient = recipient,
        date = now - (10 * 60).seconds,
        dateSent = now - (10 * 60).seconds,
        seen = true,
        deliveryStatus = DeliveryStatus.PUBLISHED,
        replyReference = null,
        isMe = false,
        attachments = emptyList(),
        reactions = emptyList(),
        body = "Swap Transaction",
        transactionReference = txReference
    )
    
    TransactionReferenceBubble(
        message = message,
        transactionReference = txReference,
        isUserMe = false,
        primaryColor = Color(0xFF00FF88),
        secondaryColor = Color(0xFF1A1A2E)
    )
}
