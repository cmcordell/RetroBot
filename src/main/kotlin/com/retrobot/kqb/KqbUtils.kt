package com.retrobot.kqb


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
        return when (divisor) {
            0 -> 0.0
            else -> (dividend.toDouble()/divisor*100)
        }
    }
}