package com.retrobot.kqb.data

import com.retrobot.kqb.domain.Team

interface TeamRepository {
    suspend fun put(team: Team)
    suspend fun put(teams: Set<Team>)

    suspend fun getByCircuit(circuit: String, division: String = "", conference: String = "") : Set<Team>
    suspend fun getByName(name: String) : Set<Team>
    suspend fun getByMember(name: String) : Set<Team>
    suspend fun getAll() : Set<Team>

    suspend fun clear()
}