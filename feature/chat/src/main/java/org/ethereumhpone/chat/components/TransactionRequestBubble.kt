package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body2_fontSize
import com.example.dgenlibrary.ui.theme.header3_fontSize
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.pulseOpacity
import com.example.dgenlibrary.ui.theme.smalllabel_fontSize
import kotlinx.datetime.Instant
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumhpone.chat.R
import org.ethereumphone.dgenlibrary.theme.dgenGreen
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.Contact
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient
import org.ethereumphone.model.TransactionCall
import org.ethereumphone.model.TransactionMetadata
import org.ethereumphone.model.TransactionRequest
import org.ethereumphone.model.TransactionRequestStatus
import kotlin.time.Duration.Companion.seconds

/**
 * Composable for rendering a transaction request message bubble.
 * Displays transaction request details.
 */
@Composable
fun TransactionRequestBubble(
    modifier: Modifier = Modifier,
    message: Message,
    transactionRequest: TransactionRequest,
    isUserMe: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    onExecuteTransaction: (TransactionRequest) -> Unit,
    onRejectTransaction: () -> Unit = {},
    isFirstMessageByAuthor: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val metadata = transactionRequest.metadata
    val chainName = chainIdToName(transactionRequest.chainId)
    
    val bubbleShape = RoundedCornerShape(3.dp)
    val bubbleBackground = secondaryColor
    val borderColor = primaryColor //.copy(alpha = pulseOpacity)
    val senderName = resolveSenderName(message)
    val headerText = if (isUserMe) "You requested" else "$senderName requested"
    
    Column(
        horizontalAlignment = if (isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            }
    ) {
        // Transaction Request Card - constrained max width
        Column(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleBackground)
                .border(1.dp, borderColor, bubbleShape)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .widthIn(min = 150.dp, max = 260.dp)
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment =  Alignment.CenterVertically
            ){
                Text(
                    text = headerText.uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = label_fontSize,
                        color = primaryColor
                    )
                )


                // DO NOT DELETE
                //TODO: displaying status of the request
//                if (false)
//                Text(
//                    modifier = Modifier
//                        .drawBehind {
//                            drawRoundRect(
//                                primaryColor,
//                                cornerRadius = CornerRadius(3.dp.toPx())
//                            )
//                        }
//                        .padding(horizontal = 6.dp, vertical = 2.dp),
//                    text = "PAID",
//                    style = TextStyle(
//                        fontFamily = SpaceMono,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = smalllabel_fontSize,
//                        color = secondaryColor
//                    )
//                )
            }

            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Amount display (if available)
            val tokenAmount = metadata?.tokenAmount
            val tokenSymbol = metadata?.tokenSymbol
            val amountText = when {
                !tokenAmount.isNullOrBlank() && !tokenSymbol.isNullOrBlank() -> "$tokenAmount $tokenSymbol"
                !tokenAmount.isNullOrBlank() -> tokenAmount
                !tokenSymbol.isNullOrBlank() -> tokenSymbol
                else -> null
            }
            if (amountText != null) {
                Text(
                    text = amountText,
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
            )
            {
                Text(
                    text = chainName.uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = primaryColor
                    )
                )
                val chainLogoRes = chainIdToLogoRes(transactionRequest.chainId)
                if (chainLogoRes != null) {
                    Icon(
                        painter = painterResource(id = chainLogoRes),
                        contentDescription = "$chainName logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            // Description (if available)
            val description = metadata?.description
            if (description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        fontSize = 13.sp,
                        color = if (isUserMe) dgenWhite.copy(alpha = 0.8f) else primaryColor.copy(alpha = 0.8f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(0.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onExecuteTransaction(transactionRequest) }
                    .heightIn(min = 48.dp)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "SEND",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = label_fontSize,
                        color = primaryColor
                    )
                )
                Icon(
                    painter = painterResource(id = R.drawable.send_arrow),
                    contentDescription = "Send",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Timestamp
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ){
            AuthorNameTimestamp(
                messageEntity = message,
                isUserMe = isUserMe,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }
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
    10L -> "Optimism"
    137L -> "Polygon"
    42161L -> "Arbitrum"
    8453L -> "Base"
    11155111L -> "Sepolia"
    else -> "Chain $chainId"
}

private fun chainIdToLogoRes(chainId: Long): Int? = when (chainId) {
    1L -> R.drawable.mainnet
    10L -> R.drawable.optimism
    137L -> R.drawable.polygon
    42161L -> R.drawable.arbitrum
    8453L -> R.drawable.base_square
    else -> null
}

/**
 * Focus/Overlay version of TransactionRequestBubble.
 * Used when the message is selected in the overlay.
 * Constrained sizing for better overlay presentation.
 */
@Composable
fun FocusTransactionRequestBubble(
    modifier: Modifier = Modifier,
    message: Message,
    transactionRequest: TransactionRequest,
    isUserMe: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    onExecuteTransaction: (TransactionRequest) -> Unit = {}
) {
    val metadata = transactionRequest.metadata
    val chainName = chainIdToName(transactionRequest.chainId)
    
    val bubbleShape = RoundedCornerShape(3.dp)
    val bubbleBackground = secondaryColor
    val borderColor = primaryColor //.copy(alpha = pulseOpacity)
    val senderName = resolveSenderName(message)
    val headerText = if (isUserMe) "You requested" else "$senderName requested"
    
    Column(
        horizontalAlignment = Alignment.Start, // Always left-align in focus mode
        modifier = modifier
    ) {
        // Transaction Request Card - constrained width for overlay
        Column(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleBackground)
                .border(1.dp, borderColor, bubbleShape)
                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                .fillMaxWidth() // Fill parent width (controlled by parent)
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = headerText.uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = label_fontSize,
                        color = primaryColor
                    )
                )
            }

            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Amount display (if available)
            val tokenAmount = metadata?.tokenAmount
            val tokenSymbol = metadata?.tokenSymbol
            val amountText = when {
                !tokenAmount.isNullOrBlank() && !tokenSymbol.isNullOrBlank() -> "$tokenAmount $tokenSymbol"
                !tokenAmount.isNullOrBlank() -> tokenAmount
                !tokenSymbol.isNullOrBlank() -> tokenSymbol
                else -> null
            }
            if (amountText != null) {
                Text(
                    text = amountText,
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
            )
            {
                Text(
                    text = chainName.uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = primaryColor
                    )
                )
                val chainLogoRes = chainIdToLogoRes(transactionRequest.chainId)
                if (chainLogoRes != null) {
                    Icon(
                        painter = painterResource(id = chainLogoRes),
                        contentDescription = "$chainName logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            // Description (if available) - with constrained lines for focus mode
            val description = metadata?.description
            if (description != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        fontSize = 12.sp,
                        color = if (isUserMe) dgenWhite.copy(alpha = 0.8f) else primaryColor.copy(alpha = 0.8f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onExecuteTransaction(transactionRequest) }
                    .heightIn(min = 40.dp) // Slightly smaller for focus mode
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "SEND",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = label_fontSize,
                        color = primaryColor
                    )
                )
                Icon(
                    painter = painterResource(id = R.drawable.send_arrow),
                    contentDescription = "Send",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp) // Slightly smaller icon
                )
            }
        }
        
        // Timestamp - left aligned for focus mode
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.Start
        ){
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
fun TransactionRequestBubblePreview() {
    val recipient = Recipient(
        id = "userA",
        address = "0xAbC123CryptoBroWallet",
        ens = "alice.eth",
        contact = Contact("lk1", "Alice", null, "0x423")
    )
    val now = Instant.parse("2025-04-17T12:23:05Z")
    
    val txRequest = TransactionRequest(
        chainId = 8453,
        calls = listOf(
            TransactionCall(
                to = "0x742d35Cc6634C0532925a3b844Bc9e7595f7e5ca",
                value = "0x38d7ea4c68000", // 0.001 ETH
                data = "0x"
            )
        ),
        metadata = TransactionMetadata(
            description = "Send ETH to Alice",
            tokenSymbol = "ETH",
            tokenAmount = "0.001",
            tokenDecimals = 18
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
        isMe = false,
        attachments = emptyList(),
        reactions = emptyList(),
        body = "Transaction Request",
        transactionRequest = txRequest,
        transactionStatus = TransactionRequestStatus.PENDING,
        transactionHash = null
    )
    
    TransactionRequestBubble(
        message = message,
        transactionRequest = txRequest,
        isUserMe = false,
        primaryColor = Color(0xFF00FF88),
        secondaryColor = Color(0xFF1A1A2E),
        onExecuteTransaction = {},
        onRejectTransaction = {}
    )
}

