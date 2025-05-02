package org.ethereumhpone.contracts.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeArithmeticException
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days


fun printFormattedDateInfo(instant: Instant?): String? {
    if (instant == null) return null

    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val date = try {
        instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    } catch (e: DateTimeArithmeticException) {
        return "Invalid Date"
    }

    val formattedDate = formatInstant(instant)

    return when {
        isWithinLast7Days(instant, now) -> {
            if (date == today) {
                formattedDate
            } else {
                getWeekday(instant)
            }
        }
        isBeforeLast7Days(instant, now) -> formattedDate
        else -> formattedDate
    }
}

fun formatInstant(instant: Instant): String {
    val zone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(zone)

    val dateTime = try {
        instant.toLocalDateTime(zone)
    } catch (e: DateTimeArithmeticException) {
        return "Invalid Date"
    }

    return when {
        dateTime.date == now.date -> dateTime.time.toString()  // returns HH:mm:ss.SSS
        dateTime.year == now.year -> "%02d.%02d".format(dateTime.monthNumber, dateTime.dayOfMonth)
        else -> "%04d.%02d.%02d".format(dateTime.year, dateTime.monthNumber, dateTime.dayOfMonth)
    }
}

fun isWithinLast7Days(instant: Instant, now: Instant = Clock.System.now()): Boolean {
    val sevenDaysAgo = now - 7.days
    return instant >= sevenDaysAgo && instant <= now
}

fun isBeforeLast7Days(instant: Instant, now: Instant = Clock.System.now()): Boolean {
    val sevenDaysAgo = now - 7.days
    return instant < sevenDaysAgo
}

fun getWeekday(instant: Instant): String {
    val dateTime = try {
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
    } catch (e: DateTimeArithmeticException) {
        return "Invalid Date"
    }
    return when (dateTime.date.dayOfWeek) {
        DayOfWeek.SUNDAY -> "Sunday"
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
    }
}
