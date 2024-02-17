package com.jamesellerbee.taskfireandroid.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun String.to12HourFormat(): String {
    return LocalTime.parse(this, DateTimeFormatter.ofPattern("HH:mm"))
        .format(DateTimeFormatter.ofPattern("h:mm a"))
}