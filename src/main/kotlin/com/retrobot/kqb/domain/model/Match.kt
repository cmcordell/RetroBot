package com.retrobot.kqb.domain.model

/**
 * Represents an KQB IGL Match.
 */
data class Match(
        val season: String,
        val circuit: String,
        val division: String,
        val conference: String,
        val week: String,  // Week in season, i.e. Bye-Week, Week 1, Finals 1
        val awayTeam: String,
        val homeTeam: String,
        val colorScheme: ColorScheme = ColorScheme.DEFAULT, // Teams in KQB must either be Blue or Gold.  Away team defaults to Gold, home to Blue.
        val date: Long = 0,
        val caster: String = "",
        val coCasters: List<String> = listOf(),
        val streamLink: String = "",
        val vodLink: String = "",
        val awaySetsWon: Int = 0,
        val homeSetsWon: Int = 0
) {
    val setsPlayed: Int = awaySetsWon + homeSetsWon
    val winner: String = when {
        awaySetsWon > homeSetsWon -> awayTeam
        homeSetsWon > awaySetsWon -> homeTeam
        else -> ""
    }
}