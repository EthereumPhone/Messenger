package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.header3_fontSize
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.pulseOpacity
import kotlinx.datetime.Instant
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
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
 * Displays completed transaction details.
 */
@Composable
fun TransactionReferenceBubble(
    modifier: Modifier = Modifier,
    message: Message,
    transactionReference: TransactionReference,
    isUserMe: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    isFirstMessageByAuthor: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val metadata = transactionReference.metadata
    val chainName = chainIdToName(transactionReference.networkId)
    
    val bubbleShape = RoundedCornerShape(3.dp)
    val bubbleBackground = secondaryColor
    val borderColor = primaryColor.copy(alpha = pulseOpacity)
    val senderName = resolveSenderName(message)
    val headerText = if (isUserMe) "You sent" else "$senderName sent"
    
    Column(
        horizontalAlignment = if (isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            }
    ) {
        // Transaction Reference Card - constrained max width
        Column(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleBackground)
                .border(1.dp, borderColor, bubbleShape)
                .padding(16.dp)
                .widthIn(min = 180.dp, max = 260.dp)
        ) {
            Text(
                text = headerText.uppercase(),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = label_fontSize,
                    color = primaryColor
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Amount display (if available)
            val amount = metadata?.amount
            val currency = metadata?.currency
            val decimals = metadata?.decimals
            if (amount != null && currency != null && decimals != null) {
                val humanAmount = formatAmount(amount, decimals)
                Text(
                    text = "$humanAmount $currency",
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = header3_fontSize,
                        color = if (isUserMe) dgenWhite else primaryColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = chainName.uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = primaryColor
                    )
                )
                val chainLogoRes = chainIdToLogoRes(transactionReference.networkId)
                if (chainLogoRes != null) {
                    Icon(
                        painter = painterResource(id = chainLogoRes),
                        contentDescription = "$chainName logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Timestamp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            AuthorNameTimestamp(
                messageEntity = message,
                isUserMe = isUserMe,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }
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

private fun resolveSenderName(message: Message): String {
    val contactName = message.recipient.contact?.name?.takeIf { it.isNotBlank() }
    val ens = message.recipient.ens?.takeIf { it.isNotBlank() }
    return contactName ?: ens ?: truncateAddress(message.recipient.address)
}

private fun chainIdToName(chainId: Long): String = when (chainId) {
    1L -> "Mainnet"
    56L -> "BNB Chain"
    10L -> "Optimism"
    137L -> "Polygon"
    42161L -> "Arbitrum"
    43114L -> "Avalanche"
    8453L -> "Base"
    7777777L -> "Zora"
    11155111L -> "Sepolia"
    else -> "Chain $chainId"
}

private fun chainIdToLogoRes(chainId: Long): Int? = when (chainId) {
    1L -> R.drawable.mainnet
    56L -> R.drawable.bnb
    10L -> R.drawable.optimism
    137L -> R.drawable.polygon
    42161L -> R.drawable.arbitrum
    43114L -> R.drawable.avalanche
    8453L -> R.drawable.base_square
    7777777L -> R.drawable.zorb
    else -> null
}

/**
 * Focus/Overlay version of TransactionReferenceBubble.
 * Used when the message is selected in the overlay.
 * Constrained sizing for better overlay presentation.
 */
@Composable
fun FocusTransactionReferenceBubble(
    modifier: Modifier = Modifier,
    message: Message,
    transactionReference: TransactionReference,
    isUserMe: Boolean,
    primaryColor: Color,
    secondaryColor: Color
) {
    val metadata = transactionReference.metadata
    val chainName = chainIdToName(transactionReference.networkId)
    
    val bubbleShape = RoundedCornerShape(3.dp)
    val bubbleBackground = secondaryColor
    val borderColor = primaryColor.copy(alpha = pulseOpacity)
    val senderName = resolveSenderName(message)
    val headerText = if (isUserMe) "You sent" else "$senderName sent"
    
    Column(
        horizontalAlignment = Alignment.Start, // Always left-align in focus mode
        modifier = modifier
    ) {
        // Transaction Reference Card - constrained width for overlay
        Column(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleBackground)
                .border(1.dp, borderColor, bubbleShape)
                .padding(12.dp)
                .fillMaxWidth() // Fill parent width (controlled by parent)
        ) {
            Text(
                text = headerText.uppercase(),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = label_fontSize,
                    color = primaryColor
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Amount display (if available)
            val amount = metadata?.amount
            val currency = metadata?.currency
            val decimals = metadata?.decimals
            if (amount != null && currency != null && decimals != null) {
                val humanAmount = formatAmount(amount, decimals)
                Text(
                    text = "$humanAmount $currency",
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = header3_fontSize,
                        color = if (isUserMe) dgenWhite else primaryColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = chainName.uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = primaryColor
                    )
                )
                val chainLogoRes = chainIdToLogoRes(transactionReference.networkId)
                if (chainLogoRes != null) {
                    Icon(
                        painter = painterResource(id = chainLogoRes),
                        contentDescription = "$chainName logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Timestamp - left aligned for focus mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.Start
        ) {
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
