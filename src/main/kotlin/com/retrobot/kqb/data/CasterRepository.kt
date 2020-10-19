package com.retrobot.kqb.data

import com.retrobot.kqb.domain.model.Caster


interface CasterRepository {
    suspend fun put(caster: Caster)
    suspend fun put(casters: List<Caster>)

    suspend fun getByName(name: String) : List<Caster>
    suspend fun getAll() : List<Caster>

    suspend fun clear()
}