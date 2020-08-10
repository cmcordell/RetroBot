package com.retrobot.kqb.domain

/**
 * Represents an IGL Season.
 * i.e. Spring, Summer, Fall, Winter
 */
// Not currently in use
data class Season(
    val id: Long,
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val circuits: Set<Circuit>
)