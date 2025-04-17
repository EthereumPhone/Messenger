package org.ethereumhpone.chat.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun truncateToMinute(instant: Instant): Instant {
    val epochSeconds = instant.epochSeconds
    val truncatedSeconds = epochSeconds - (epochSeconds % 60)
    return Instant.fromEpochSeconds(truncatedSeconds)
}


fun truncateToDate(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val localDateTime = instant.toLocalDateTime(zone)
    val localMidnight = LocalDateTime(
        date = localDateTime.date,
        time = LocalTime(0, 0)
    )
    return localMidnight.toInstant(zone)
}


