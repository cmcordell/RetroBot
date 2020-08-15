package com.retrobot.twitch.data

import com.github.twitch4j.helix.domain.Game
import com.github.twitch4j.helix.domain.Stream


interface TwitchRepository {
    suspend fun getTopGames(limit: Int = 20): List<Game>
    suspend fun searchGames(query: String, limit: Int = 20): List<Game>
    suspend fun getStreamsByGame(gameId: String, limit: Int = 20): List<Stream>
    suspend fun getStreamsByUser(userId: String, limit: Int = 20): List<Stream>
}