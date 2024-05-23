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