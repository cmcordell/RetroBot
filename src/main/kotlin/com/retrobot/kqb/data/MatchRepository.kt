package com.retrobot.kqb.data

import com.retrobot.kqb.domain.Match


interface MatchRepository {
    suspend fun put(match: Match)
    suspend fun put(matches: Set<Match>)

    suspend fun getByWeekAndTeams(week: String, awayTeam: String, homeTeam: String): Match?
    suspend fun getByCircuit(circuit: String, division: String = "", conference: String = ""): Set<Match>
    suspend fun getByDate(from: Long, to: Long): Set<Match>
    suspend fun getByWeek(week: String): Set<Match>
    suspend fun getByTeam(team: String): Set<Match>
    suspend fun getByCaster(caster: String): Set<Match>
    suspend fun getAll(): Set<Match>

    suspend fun clear()
}