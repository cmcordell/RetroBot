package com.retrobot.kqb.data

import com.retrobot.kqb.domain.model.Award


interface AwardRepository {
    suspend fun put(award: Award)
    suspend fun put(awards: List<Award>)

    suspend fun getByCircuit(circuit: String, division: String = "", conference: String = "") : List<Award>
    suspend fun getByPlayer(player: String) : List<Award>
    suspend fun getAll() : List<Award>

    suspend fun clear()
}