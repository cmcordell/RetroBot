package com.retrobot.kqb.data.exposedrepo

import com.retrobot.core.util.batchUpsert
import com.retrobot.core.util.dbActionQuery
import com.retrobot.core.util.dbQuery
import com.retrobot.core.util.upsert
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.domain.Caster
import org.jetbrains.exposed.sql.*

/**
 * KQB Caster Repository implemented with Kotlin Exposed DSL
 */
class ExposedCasterRepository : CasterRepository {

//    init {
//        GlobalScope.launch(Dispatchers.IO) {
//            Database.connect(DatabaseFactory.h2())
//        }
//    }


    override suspend fun put(caster: Caster) = dbActionQuery {
        Casters.upsert(Casters.columns) { table ->
            table[name] = caster.name
            table[streamLink] = caster.streamLink
        }
    }
    
    override suspend fun put(casters: Set<Caster>) = dbActionQuery {
        Casters.batchUpsert(casters, Casters.columns) { batch, caster ->
            batch[name] = caster.name
            batch[streamLink] = caster.streamLink
        }
    }

    override suspend fun getByName(name: String) : Set<Caster> = dbQuery {
        if (name.isBlank()) {
            setOf()
        } else {
            Casters.select { Casters.name.upperCase() like "%${name.toUpperCase()}%" }
                    .map(this::toCaster)
                    .toSet()
        }
    }

    override suspend fun getAll() : Set<Caster> = dbQuery {
        Casters.selectAll()
            .map(this::toCaster)
            .toSet()
    }

    override suspend fun clear() = dbActionQuery {
        Casters.deleteAll()
    }


    object Casters: Table("casters") {
        val name = varchar("name", 50)
        val streamLink = text("stream_link")
        override val primaryKey = PrimaryKey(name)
    }

    private fun toCaster(row: ResultRow): Caster = Caster(
        name = row[Casters.name],
        streamLink = row[Casters.streamLink]
    )
}