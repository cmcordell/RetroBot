package com.retrobot.kqb.domain.model

data class Player(
    val name: String,
    val teamsWithMatches: List<TeamWithMatches>,
    val awards: List<Award>
)

data class TeamWithMatches(
    val team: Team,
    val matches: List<Match>
)