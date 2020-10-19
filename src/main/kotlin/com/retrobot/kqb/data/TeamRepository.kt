package com.retrobot.kqb.data

import com.retrobot.kqb.domain.model.Team

interface TeamRepository {
    suspend fun put(team: Team)
    suspend fun put(teams: List<Team>)

    suspend fun getByCircuit(circuit: String, division: String = "", conference: String = "") : List<Team>
    suspend fun getByName(name: String) : List<Team>
    suspend fun getByMember(name: String) : List<Team>
    suspend fun getAll() : List<Team>

    suspend fun clear()
}