package com.retrobot.kqb.data

import com.retrobot.kqb.domain.model.Award


interface AwardRepository {
    suspend fun put(award: Award)
    suspend fun put(awards: Set<Award>)

    suspend fun getByCircuit(circuit: String, division: String = "", conference: String = "") : Set<Award>
    suspend fun getByPlayer(player: String) : Set<Award>
    suspend fun getAll() : Set<Award>

    suspend fun clear()
}