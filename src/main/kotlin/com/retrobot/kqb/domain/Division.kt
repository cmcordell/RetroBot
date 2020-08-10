package com.retrobot.kqb.domain

/**
 * Represents an IGL division (AKA Tier) within a [Circuit].
 * i.e. 1, 2, 3
 */
// Not currently in use
data class Division(
    val id: Long,
    val name: String,
    val conferences: Set<Conference>
)