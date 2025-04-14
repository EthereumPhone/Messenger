package org.ethereumphone.dgenlibrary.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TimeHeader(timestamp: Instant) {
    val formatted = formatTimestamp(timestamp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = dgenWhite.copy(alpha = 0.1f)
        ) {
            Text(
                text = formatted.uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
        }
    }
}

@Composable
fun formatTimestamp(timestamp: Instant): String {
    val zone = remember { TimeZone.currentSystemDefault() }
    val now = remember { Clock.System.now().toLocalDateTime(zone) }
    val messageDateTime = remember(timestamp) { timestamp.toLocalDateTime(zone) }

    val today = now.date
    val messageDate = messageDateTime.date

    val daysAgo = today.daysUntil(messageDate)

    val timePart = "%02d:%02d".format(messageDateTime.hour, messageDateTime.minute)

    return when {
        daysAgo == 0 -> "$timePart"
        daysAgo == -1 -> "Yesterday"
        daysAgo in -6..-2 -> {
            val dayName = messageDate.dayOfWeek.name.lowercase()
                .replaceFirstChar { it.uppercase() }
            "$dayName"
        }
        else -> {
            val day = messageDate.dayOfMonth.toString().padStart(2, '0')
            val month = messageDate.month.name.lowercase()
                .replaceFirstChar { it.uppercase() }
                .take(3)
            "$day $month"
        }
    }
}

