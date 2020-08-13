package com.retrobot.kqb.domain.model

/**
 * Represents an IGL conference (AKA Sub-Tier) within a [Division].
 * i.e. a, b, c
 */
// Not currently in use
data class Conference(
    val id: Long,
    val name: String,
    val teams: Set<Team>
)