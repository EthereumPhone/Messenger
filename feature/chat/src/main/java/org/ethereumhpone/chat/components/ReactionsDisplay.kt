package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.model.Reaction
import org.ethereumphone.model.ReactionSchema
import org.ethosmobile.components.library.theme.Colors

/**
 * A grouped reaction that combines identical emojis from different users.
 */
data class GroupedReaction(
    val emoji: String,
    val count: Int,
    val isFromMe: Boolean, // Whether current user has added this reaction
    val senderInboxIds: List<String>
)

/**
 * Groups reactions by emoji for display.
 */
fun List<Reaction>.groupByEmoji(myInboxId: String): List<GroupedReaction> {
    return this.groupBy { it.content }
        .map { (emoji, reactions) ->
            GroupedReaction(
                emoji = emoji,
                count = reactions.size,
                isFromMe = reactions.any { it.senderInboxId == myInboxId },
                senderInboxIds = reactions.map { it.senderInboxId }
            )
        }
        .sortedByDescending { it.count }
}

/**
 * Displays message reactions as compact pills below a message bubble.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionsDisplay(
    reactions: List<Reaction>,
    myInboxId: String,
    isUserMe: Boolean,
    primaryColor: Color,
    onReactionClick: (String) -> Unit = {}, // Emoji clicked - toggle reaction
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return
    
    val groupedReactions = reactions.groupByEmoji(myInboxId)
    
    FlowRow(
        modifier = modifier.padding(
            start = if (isUserMe) 0.dp else 8.dp,
            end = if (isUserMe) 8.dp else 0.dp,
            top = 2.dp
        ),
        horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groupedReactions.forEach { grouped ->
            ReactionPill(
                emoji = grouped.emoji,
                count = grouped.count,
                isFromMe = grouped.isFromMe,
                primaryColor = primaryColor,
                isUserMe = isUserMe,
                onClick = { onReactionClick(grouped.emoji) }
            )
        }
    }
}

/**
 * A single reaction pill showing emoji and count.
 */
@Composable
fun ReactionPill(
    emoji: String,
    count: Int,
    isFromMe: Boolean,
    primaryColor: Color,
    isUserMe: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isFromMe -> primaryColor.copy(alpha = 0.3f)
        else -> Colors.DARK_GRAY.copy(alpha = 0.6f)
    }
    
    val borderColor = when {
        isFromMe -> primaryColor
        else -> Color.Transparent
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (isFromMe) {
                    Modifier.border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 14.sp
            )
            if (count > 1) {
                Text(
                    text = count.toString(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isUserMe) dgenWhite else primaryColor
                    )
                )
            }
        }
    }
}

/**
 * Common emoji reactions for the quick picker.
 */
object CommonReactions {
    val quickReactions = listOf("👍", "❤️", "😂", "😮", "😢", "🔥")
    val allReactions = listOf(
        "👍", "👎", "❤️", "💔", "😂", "😮", "😢", "😡",
        "🔥", "🎉", "✅", "❌", "👀", "🙏", "💯", "🚀"
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun ReactionsDisplayPreview() {
    val sampleReactions = listOf(
        Reaction("1", "msg1", "user1", ReactionSchema.UNICODE, "👍"),
        Reaction("2", "msg1", "user2", ReactionSchema.UNICODE, "👍"),
        Reaction("3", "msg1", "user1", ReactionSchema.UNICODE, "❤️"),
        Reaction("4", "msg1", "myInboxId", ReactionSchema.UNICODE, "👍"),
    )
    
    ReactionsDisplay(
        reactions = sampleReactions,
        myInboxId = "myInboxId",
        isUserMe = false,
        primaryColor = Color(0xFF00FF88),
        onReactionClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun ReactionPillPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReactionPill(
            emoji = "👍",
            count = 3,
            isFromMe = true,
            primaryColor = Color(0xFF00FF88),
            isUserMe = false,
            onClick = {}
        )
        ReactionPill(
            emoji = "❤️",
            count = 1,
            isFromMe = false,
            primaryColor = Color(0xFF00FF88),
            isUserMe = false,
            onClick = {}
        )
    }
}

