package com.retrobot.kqb.domain.usecase

import com.retrobot.core.Duration
import com.retrobot.kqb.data.AwardRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.model.*


class GetPlayerUseCase(
    private val awardRepo: AwardRepository,
    private val matchRepo: MatchRepository,
    private val teamRepo: TeamRepository
) {
    suspend fun getPlayerByName(name: String): Player? {
        val teams = getTeamsByPlayer(name)
        if (teams.isEmpty()) {
            return null
        }

        val playerName = getMatchingMember(name, teams[0])
        val teamsWithMatches = mutableListOf<TeamWithMatches>()
        teams.forEach { team ->
            teamsWithMatches.add(TeamWithMatches(team, getUpcomingMatchesByTeam(team)))
        }
        val awards = getAwardsByPlayer(playerName)

        return Player(playerName, teamsWithMatches, awards)
    }

    private suspend fun getTeamsByPlayer(player: String): List<Team> {
        return teamRepo.getByMember(player)
            .filter { team -> isPlayerMemberOfTeam(player, team) }
    }

    private suspend fun getUpcomingMatchesByTeam(team: Team): List<Match> {
        val now = System.currentTimeMillis()
        val oneWeekFromNow = now + Duration.WEEK
        return matchRepo.getByTeam(team.name)
            .filter { it.date in now until oneWeekFromNow }
            .sortedBy { it.date }
    }

    private suspend fun getAwardsByPlayer(player: String): List<Award> {
        return awardRepo.getByPlayer(player)
            .filter { it.player.equals(player, true) }
    }

    private fun isPlayerMemberOfTeam(player: String, team: Team) : Boolean {
        for (member in team.members) {
            if (member.equals(player, true)) {
                return true
            }
        }
        return false
    }

    private fun getMatchingMember(player: String, team: Team) : String {
        for (member in team.members) {
            if (member.equals(player, true)) {
                return member
            }
        }
        return player
    }
}