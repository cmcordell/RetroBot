package com.retrobot.kqb.data.exposedrepo

import com.retrobot.core.util.batchUpsert
import com.retrobot.core.util.dbActionQuery
import com.retrobot.core.util.dbQuery
import com.retrobot.core.util.upsert
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.exposedrepo.KqbDatabase.Casters
import com.retrobot.kqb.domain.model.Caster
import org.jetbrains.exposed.sql.*

/**
 * KQB Caster Repository implemented with Kotlin Exposed DSL
 */
class ExposedCasterRepository(kqbDatabase: KqbDatabase) : CasterRepository {

    private val database = Database.connect(kqbDatabase.dataSource)

    override suspend fun put(caster: Caster) = dbActionQuery(database) {
        Casters.upsert(Casters.columns) { table ->
            table[name] = caster.name
            table[streamLink] = caster.streamLink
            table[bio] = caster.bio
            table[gamesCasted] = caster.gamesCasted
        }
    }
    
    override suspend fun put(casters: List<Caster>) = dbActionQuery(database) {
        Casters.batchUpsert(casters, Casters.columns, true) { batch, caster ->
            batch[name] = caster.name
            batch[streamLink] = caster.streamLink
            batch[bio] = caster.bio
            batch[gamesCasted] = caster.gamesCasted
        }
    }

    override suspend fun getByName(name: String) : List<Caster> = dbQuery(database) {
        if (name.isBlank()) {
            listOf()
        } else {
            Casters.select { Casters.name.upperCase() like "%${name.toUpperCase()}%" }
                    .map(this::toCaster)
                    .toList()
        }
    }

    override suspend fun getAll() : List<Caster> = dbQuery(database) {
        Casters.selectAll()
            .map(this::toCaster)
            .toList()
    }

    override suspend fun clear() = dbActionQuery(database) {
        Casters.deleteAll()
    }

    private fun toCaster(row: ResultRow): Caster = Caster(
            name = row[Casters.name],
            streamLink = row[Casters.streamLink],
            bio = row[Casters.bio],
            gamesCasted = row[Casters.gamesCasted]
    )
}