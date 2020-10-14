package com.retrobot.kqb.domain.model

/**
 * Represents a known KQB caster/commentator.
 */
data class Caster(
    val name: String,
    val streamLink: String,
    val bio: String,
    val gamesCasted: Int = 0
)