package org.ethereumhpone.chat.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import kotlinx.datetime.Instant
import org.ethereumhpone.chat.components.message.AuthorNameTimestamp
import org.ethereumphone.dgenlibrary.theme.dgenGreen
import org.ethereumphone.dgenlibrary.theme.dgenRed
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
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
 * Displays transaction details and provides an execute button.
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
    isFirstMessageByAuthor: Boolean = false
) {
    val status = message.transactionStatus ?: TransactionRequestStatus.PENDING
    val chainName = chainIdToName(transactionRequest.chainId)
    val metadata = transactionRequest.metadata
    
    // Status-based colors
    val statusColor = when (status) {
        TransactionRequestStatus.PENDING -> dgenTurqoise
        TransactionRequestStatus.EXECUTING -> Color(0xFFFFAA00) // Orange
        TransactionRequestStatus.SUCCESS -> dgenGreen
        TransactionRequestStatus.FAILED -> dgenRed
        TransactionRequestStatus.REJECTED -> Color.Gray
    }
    
    val bubbleShape = RoundedCornerShape(16.dp)
    val bubbleBackground = if (isUserMe) primaryColor.copy(alpha = 0.15f) else secondaryColor.copy(alpha = 0.15f)
    val borderColor = statusColor.copy(alpha = 0.5f)
    
    Column(
        horizontalAlignment = if (isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier
    ) {
        // Transaction Request Card
        Column(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleBackground)
                .border(1.dp, borderColor, bubbleShape)
                .padding(16.dp)
                .width(280.dp)
        ) {
            // Header: Transaction Request label with status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Token,
                        contentDescription = "Transaction",
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transaction Request",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isUserMe) dgenWhite else primaryColor
                        )
                    )
                }
                
                // Status badge
                StatusBadge(status = status, color = statusColor)
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
                        color = statusColor
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Amount display (if available)
            val tokenAmount = metadata?.tokenAmount
            val tokenSymbol = metadata?.tokenSymbol
            if (tokenAmount != null && tokenSymbol != null) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = tokenAmount,
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = if (isUserMe) dgenWhite else primaryColor
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tokenSymbol,
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
            
            // Description (if available)
            val description = metadata?.description
            if (description != null) {
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
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Calls info
            Text(
                text = "${transactionRequest.calls.size} call${if (transactionRequest.calls.size > 1) "s" else ""} to execute",
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontSize = 11.sp,
                    color = if (isUserMe) dgenWhite.copy(alpha = 0.6f) else primaryColor.copy(alpha = 0.6f)
                )
            )
            
            // First call target address (truncated)
            val firstCall = transactionRequest.calls.firstOrNull()
            if (firstCall != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = "To",
                        tint = if (isUserMe) dgenWhite.copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = truncateAddress(firstCall.to),
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            fontSize = 11.sp,
                            color = if (isUserMe) dgenWhite.copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons (only show if PENDING and not my own request)
            if (status == TransactionRequestStatus.PENDING && !isUserMe) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Execute button
                    Button(
                        onClick = { onExecuteTransaction(transactionRequest) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = dgenGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallMade,
                            contentDescription = "Execute",
                            tint = dgenWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Execute",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = dgenWhite
                            )
                        )
                    }
                    
                    // Reject button
                    Button(
                        onClick = onRejectTransaction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, dgenRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reject",
                            tint = dgenRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else if (status == TransactionRequestStatus.EXECUTING) {
                // Loading state
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = statusColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Executing...",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            fontSize = 12.sp,
                            color = statusColor
                        )
                    )
                }
            } else if (status == TransactionRequestStatus.SUCCESS) {
                // Success state with tx hash
                val txHash = message.transactionHash
                if (txHash != null) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = dgenGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tx: ${truncateAddress(txHash)}",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                fontSize = 11.sp,
                                color = dgenGreen
                            )
                        )
                    }
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
private fun StatusBadge(status: TransactionRequestStatus, color: Color) {
    val text = when (status) {
        TransactionRequestStatus.PENDING -> "PENDING"
        TransactionRequestStatus.EXECUTING -> "EXECUTING"
        TransactionRequestStatus.SUCCESS -> "SUCCESS"
        TransactionRequestStatus.FAILED -> "FAILED"
        TransactionRequestStatus.REJECTED -> "REJECTED"
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = SpaceMono,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = color
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

