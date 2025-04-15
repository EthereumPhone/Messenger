package org.ethereumhpone.chat.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun truncateToMinute(instant: Instant): Instant {
    val epochSeconds = instant.epochSeconds
    val truncatedSeconds = epochSeconds - (epochSeconds % 60)
    return Instant.fromEpochSeconds(truncatedSeconds)
}


fun truncateToDate(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
    return instant.toLocalDateTime(zone).date
}


