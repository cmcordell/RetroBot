package com.retrobot.kqb.data.inmemoryrepo

import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.Team

// TODO Use a Cache instead of map
object InMemoryTeamRepository : TeamRepository {
    private val teams = mutableMapOf<String, Team>()

    override suspend fun put(team: Team) {
        teams[team.name] = team
    }

    override suspend fun put(teams: Set<Team>) {
        teams.forEach { team ->
            this.teams[team.name] = team
        }
    }

    override suspend fun getByCircuit(circuit: String, division: String, conference: String): Set<Team> {
        return teams.values.filter { team ->
            team.circuit.contains(circuit, true)
                    && team.division.contains(division, true)
                    && team.conference.contains(conference, true)
        }.toSet()
    }

    override suspend fun getByName(name: String): Set<Team> {
        return teams.values.filter { team ->
            team.name.contains(name, true)
        }.toSet()
    }

    override suspend fun getByMember(name: String): Set<Team> {
        return teams.values.filter { team ->
            isPlayerOnTeam(name, team)
        }.toSet()
    }
    private fun isPlayerOnTeam(player: String, team: Team) : Boolean {
        for (member in team.members) {
            if (member.contains(player)) return true
        }
        return false
    }

    override suspend fun getAll(): Set<Team> {
        return teams.values.toSet()
    }

    override suspend fun clear() {
        teams.clear()
    }
}