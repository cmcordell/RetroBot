package com.retrobot.kqb.data.inmemoryrepo

import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.domain.Match

// TODO Use a Cache instead of map
class InMemoryMatchRepository : MatchRepository {
    private val matches = mutableMapOf<String, Match>()

    override suspend fun put(match: Match) {
        matches[match.id] = match
    }

    override suspend fun put(matches: Set<Match>) {
        matches.forEach { match ->
            this.matches[match.id] = match
        }
    }

    override suspend fun getByWeekAndTeams(week: String, awayTeam: String, homeTeam: String): Match? {
        return matches.values.firstOrNull { match ->
            match.week.contains(week, true)
                    && match.awayTeam.contains(awayTeam, true)
                    && match.homeTeam.contains(homeTeam, true)
        }
    }

    override suspend fun getByCircuit(circuit: String, division: String, conference: String): Set<Match> {
        return matches.values.filter { match ->
            match.circuit.contains(circuit, true)
                    && match.division.contains(division, true)
                    && match.conference.contains(conference, true)
        }.toSet()
    }

    override suspend fun getByDate(from: Long, to: Long) : Set<Match> {
        return matches.values.filter { match ->
            match.date in from..to
        }.toSet()
    }

    override suspend fun getByWeek(week: String): Set<Match> {
        return matches.values.filter { match ->
            match.week.contains(week, true)
        }.toSet()
    }

    override suspend fun getByTeam(team: String): Set<Match> {
        return matches.values.filter { match ->
            match.awayTeam.contains(team, true) || match.homeTeam.contains(team, true)
        }.toSet()
    }

    override suspend fun getByCaster(caster: String): Set<Match> {
        return matches.values.filter { match ->
            match.caster.contains(caster, true) || match.coCasters.contains(caster)
        }.toSet()
    }

    override suspend fun getAll(): Set<Match> {
        return matches.values.toSet()
    }

    override suspend fun clear() {
        matches.clear()
    }
}