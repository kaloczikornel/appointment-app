package com.example.utils

import com.example.models.Day
import java.text.SimpleDateFormat
import java.util.*

fun String.toDate(): Date {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    return formatter.parse(this) ?: throw IllegalArgumentException("Invalid date format")
}

fun String.toDay(): Day {
    return when (this) {
        "mon" -> Day.MONDAY
        "tues" -> Day.TUESDAY
        "wed" -> Day.WEDNESDAY
        "thurs" -> Day.THURSDAY
        "fri" -> Day.FRIDAY
        "sat" -> Day.SATURDAY
        "sun" -> Day.SUNDAY
        else -> throw IllegalArgumentException("Invalid day")
    }
}

fun Date.getDayOfWeek(): Day? {
    val calendar = Calendar.getInstance()
    calendar.time = this

    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        1 -> Day.SUNDAY
        2 -> Day.MONDAY
        3 -> Day.TUESDAY
        4 -> Day.WEDNESDAY
        5 -> Day.THURSDAY
        6 -> Day.FRIDAY
        7 -> Day.SATURDAY
        else -> null
    }
}