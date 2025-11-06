package org.ethereumhpone.chat.components.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumhpone.chat.model.messageFormatter
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import androidx.compose.ui.graphics.Color
import org.ethereumphone.model.Message

/**
 * Overlay-specific lightweight message item.
 * Aligns the message text, timestamp, and delivery checks on a single line (baseline-aligned),
 * styled consistently with ChatItemBubbleV3 but without extras (media, replies, gestures).
 */
@Composable
fun OverlayMessageItem(
    msg: Message,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
) {
    val isUserMe = msg.isMe
    val textColor = if (isUserMe) dgenWhite else primaryColor

    Column(
        horizontalAlignment = if (isUserMe) Alignment.End else Alignment.Start,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // Terminal-style prompt for incoming messages
            if (!isUserMe) {
                Text(
                    text = "> ",
                    style = TextStyle(
                        textAlign = TextAlign.Start,
                        fontFamily = SpaceMono,
                        color = textColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        textDecoration = TextDecoration.None
                    ),
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .alignBy(LastBaseline)
                )
            }

            val styledMessage = messageFormatter(
                text = msg.body,
                primary = isUserMe
            )

            // Single-line aligned body; use Text for baseline alignment with metadata
            Text(
                text = styledMessage,
                style = TextStyle(
                    textAlign = TextAlign.Start,
                    fontFamily = PitagonsSans,
                    color = textColor,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textDecoration = TextDecoration.None
                ),
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .alignBy(LastBaseline)
            )

            Spacer(modifier = Modifier.padding(start = 12.dp))

            AuthorNameTimestamp(
                messageEntity = msg,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                isUserMe = isUserMe,
                modifier = Modifier.alignBy(LastBaseline)
            )
        }
    }
}

