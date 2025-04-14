package org.ethereumhpone.contracts.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

fun printFormattedDateInfo(date: Date?): String? {


    val formattedDate = date?.let { formatDate(it) }

    val calendar = Calendar.getInstance()
    if (date != null) {
        calendar.time = date
    }

    val currentCalendar = Calendar.getInstance()

    when {
        date?.let { isWithinLast7Days(it) } == true -> {
            val weekday = getWeekday(date)
            if (isSameDay(calendar, currentCalendar)){
                return formattedDate
            }

            return weekday
        }
        date?.let { isBeforeLast7Days(it) } == true -> {
            return formattedDate
        }
        else -> {
            return formattedDate
        }
    }
}

fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    val currentCalendar = Calendar.getInstance()

    return when {
        isSameDay(calendar, currentCalendar) -> {
            SimpleDateFormat("HH:mm").format(date)
        }
        calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) -> {
            SimpleDateFormat("MM.dd").format(date)
        }
        else -> {
            SimpleDateFormat("yyyy.MM.dd").format(date)
        }
    }
}

fun isSameDay(calendar1: Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}

fun isWithinLast7Days(date: Date): Boolean {
    val currentDate = Date()
    val sevenDaysAgo = Calendar.getInstance().apply {
        time = currentDate
        add(Calendar.DAY_OF_YEAR, -7)
    }.time

    return !date.before(sevenDaysAgo) && !date.after(currentDate)
}

fun isBeforeLast7Days(date: Date): Boolean {
    val sevenDaysAgo = Calendar.getInstance().apply {
        time = Date()
        add(Calendar.DAY_OF_YEAR, -7)
    }.time

    return date.before(sevenDaysAgo)
}

fun getWeekday(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> "Unknown"
    }
}