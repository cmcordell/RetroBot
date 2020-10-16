package com.retrobot.kqb.data.exposedrepo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.retrobot.core.util.batchUpsert
import com.retrobot.core.util.dbActionQuery
import com.retrobot.core.util.dbQuery
import com.retrobot.core.util.upsert
import com.retrobot.kqb.data.AwardRepository
import com.retrobot.kqb.data.exposedrepo.KqbDatabase.Awards
import com.retrobot.kqb.domain.model.Award
import com.retrobot.kqb.domain.model.Statistic
import com.retrobot.kqb.domain.model.Team
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like

/**
 * KQB Caster Repository implemented with Kotlin Exposed DSL
 */
class ExposedAwardRepository(kqbDatabase: KqbDatabase) : AwardRepository {

    private val database = Database.connect(kqbDatabase.dataSource)

    override suspend fun put(award: Award) = dbActionQuery(database) {
        Awards.upsert(Awards.columns) { table ->
            table[awardType] = award.awardType
            table[season] = award.season
            table[circuit] = award.circuit
            table[division] = award.division
            table[conference] = award.conference
            table[week] = award.week
            table[player] = award.player
            table[stats] = Gson().toJson(award.stats)
        }
    }

    override suspend fun put(awards: Set<Award>) = dbActionQuery(database) {
        Awards.batchUpsert(awards, Awards.columns, true) { batch, award ->
            batch[awardType] = award.awardType
            batch[season] = award.season
            batch[circuit] = award.circuit
            batch[division] = award.division
            batch[conference] = award.conference
            batch[week] = award.week
            batch[player] = award.player
            batch[stats] = Gson().toJson(award.stats)
        }
    }

    override suspend fun getByCircuit(circuit: String, division: String, conference: String) : Set<Award> = dbQuery(database) {
        Awards.select { Awards.circuit.upperCase() like "%${circuit.toUpperCase()}%" }
            .andWhere { Awards.division.upperCase() like "%${division.toUpperCase()}%" }
            .andWhere { Awards.conference.upperCase() like "%${conference.toUpperCase()}%" }
            .map(this::toAward)
            .toSet()
    }

    override suspend fun getByPlayer(player: String) : Set<Award> = dbQuery(database) {
        if (player.isBlank()) {
            setOf()
        } else {
            Awards.select { Awards.player.upperCase() like "%${player.toUpperCase()}%" }
                    .map(this::toAward)
                    .toSet()
        }
    }

    override suspend fun getAll() : Set<Award> = dbQuery(database) {
        Awards.selectAll()
                .map(this::toAward)
                .toSet()
    }

    override suspend fun clear() = dbActionQuery(database) {
        Awards.deleteAll()
    }

    private fun toAward(row: ResultRow): Award = Award(
            awardType = row[Awards.awardType],
            season = row[Awards.season],
            circuit = row[Awards.circuit],
            division = row[Awards.division],
            conference = row[Awards.conference],
            week = row[Awards.week],
            player = row[Awards.player],
            stats = Gson().fromJson(row[Awards.stats], object : TypeToken<List<Statistic>>() {}.type)
    )
}