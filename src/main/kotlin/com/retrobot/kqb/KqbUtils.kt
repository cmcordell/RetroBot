package com.retrobot.kqb

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object KqbUtils {
    fun getCircuitName(code: String) : String {
        return when {
            code.equals("E", true) -> "East"
            code.equals("W", true) -> "West"
            else -> code
        }
    }

    fun getCircuitCode(name: String) : String {
        return when {
            name.equals("East", true) -> "E"
            name.equals("West", true) -> "W"
            else -> name
        }
    }

    fun percent(dividend: Int, divisor: Int) : Double {
        return (dividend.toDouble()/divisor*100)
    }

    fun convertToEst(date: Long) : String {
        val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("US/Eastern"))
        return DateTimeFormatter.ofPattern("EEEE, MMM d 'at' h:mma 'EST'").format(localDateTime)
    }
}