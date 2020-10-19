package com.retrobot.kqb.data

import com.retrobot.kqb.domain.model.Match


interface MatchRepository {
    suspend fun put(match: Match)
    suspend fun put(matches: List<Match>)

    suspend fun getByWeekAndTeams(week: String, awayTeam: String, homeTeam: String): Match?
    suspend fun getByCircuit(circuit: String, division: String = "", conference: String = ""): List<Match>
    suspend fun getByDate(from: Long, to: Long): List<Match>
    suspend fun getByWeek(week: String): List<Match>
    suspend fun getByTeam(team: String): List<Match>
    suspend fun getByCaster(caster: String): List<Match>
    suspend fun getAll(): List<Match>

    suspend fun clear()
}