package com.retrobot.kqb.domain.model

/**
 * Represents a KQB IGL Team.
 */
data class Team(
    val name: String,
    val captain: String,
    val members: List<String>,
    val season: String,
    val circuit: String,
    val division: String,
    val conference: String,
    val matchesWon: Int = 0,
    val matchesLost: Int = 0,
    val matchesPlayed: Int = 0,
    val setsWon: Int = 0,
    val setsLost: Int = 0,
    val setsPlayed: Int = 0,
    val playoffSeed: Int = 0,
    val infoLink: String = ""
)