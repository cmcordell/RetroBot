package com.retrobot.kqb.domain

/**
 * Represents an IGL circuit within a [Season].
 * i.e. East, Central, West
 */
// Not currently in use
data class Circuit(
    val id: Long,
    val name: String,
    val divisions: Set<Division>
)