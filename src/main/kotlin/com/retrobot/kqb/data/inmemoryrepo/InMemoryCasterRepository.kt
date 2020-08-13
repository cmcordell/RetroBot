package com.retrobot.kqb.data.inmemoryrepo

import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.domain.model.Caster

// TODO Use a Cache instead of map
class InMemoryCasterRepository : CasterRepository {
    private val casters = mutableMapOf<String, Caster>()

    override suspend fun put(caster: Caster) {
        casters[caster.name] = caster
    }

    override suspend fun put(casters: Set<Caster>) {
        casters.forEach { caster ->
            this.casters[caster.name] = caster
        }
    }

    override suspend fun getByName(name: String): Set<Caster> {
        return casters.values.filter { caster ->
            caster.name.contains(name, true)
        }.toSet()
    }

    override suspend fun getAll(): Set<Caster> {
        return casters.values.toSet()
    }

    override suspend fun clear() {
        casters.clear()
    }
}