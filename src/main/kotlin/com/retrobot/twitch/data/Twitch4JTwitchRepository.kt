package com.retrobot.twitch.data

import com.github.twitch4j.helix.TwitchHelix
import com.github.twitch4j.helix.domain.Game
import com.github.twitch4j.helix.domain.Stream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of [TwitchRepository] using Twitch4J API.
 */
class Twitch4JTwitchRepository(private val twitchHelix: TwitchHelix) : TwitchRepository {

    override suspend fun getTopGames(limit: Int): List<Game> = withContext(Dispatchers.IO) {
        twitchHelix.getTopGames(null, null, null, "$limit").execute().games
    }

    override suspend fun searchGames(query: String, limit: Int): List<Game> = withContext(Dispatchers.IO) {
        try {
            twitchHelix.searchCategories(null, query, limit, null)
                .execute()
                .results
        } catch (e: Exception) {
            // If no results are received Twitch4J will throw an error
            listOf<Game>()
        }
    }

    override suspend fun getStreamsByGame(gameId: String, limit: Int): List<Stream> = withContext(Dispatchers.IO) {
        twitchHelix.getStreams(null, null, null, limit, null, listOf(gameId), null, null, null)
            .execute()
            .streams ?: listOf()
    }

    override suspend fun getStreamsByUser(userId: String, limit: Int): List<Stream> = withContext(Dispatchers.IO) {
        twitchHelix.getStreams(null, null, null, limit, null, null, null, listOf(userId), null)
            .execute()
            .streams ?: listOf()
    }
}