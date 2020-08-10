package com.retrobot.kqb.data

import com.retrobot.kqb.domain.Caster


interface CasterRepository {
    suspend fun put(caster: Caster)
    suspend fun put(casters: Set<Caster>)

    suspend fun getByName(name: String) : Set<Caster>
    suspend fun getAll() : Set<Caster>

    suspend fun clear()
}