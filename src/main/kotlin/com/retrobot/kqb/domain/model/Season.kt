package com.retrobot.kqb.domain.model

import com.retrobot.kqb.domain.model.Circuit

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