package com.retrobot.twitch.domain

import com.github.twitch4j.helix.domain.Game
import com.github.twitch4j.helix.domain.Stream as TwitchStream
import com.retrobot.twitch.data.TwitchRepository


class TwitchStreamsUseCase(
    private val twitchRepository: TwitchRepository
) {
    suspend fun getTopStreams(limit: Int = 10): List<Stream> {
        val games = twitchRepository.getTopGames(limit)
        return games.mapNotNull { game ->
            twitchRepository.getStreamsByGame(game.id, 1).map {
                    stream -> mapToStream(stream, game)
            }.firstOrNull()
        }
    }

    suspend fun getStreamsByGame(game: Game, limit: Int = 10): List<Stream> {
        return twitchRepository.getStreamsByGame(game.id, limit).map { mapToStream(it, game) }
    }

    suspend fun getStreamsByGameQuery(query: String, limit: Int = 10): List<Stream> {
        return twitchRepository.searchGames(query, 1).firstOrNull()?.let { game ->
            twitchRepository.getStreamsByGame(game.id, limit).map { mapToStream(it, game) }
        } ?: listOf()
    }

    private fun mapToStream(stream: TwitchStream, game: Game): Stream {
        return Stream(
            stream.id,
            stream.userId,
            stream.userName,
            game,
            stream.type,
            stream.title,
            stream.viewerCount,
            stream.startedAtInstant,
            stream.tagIds,
            stream.language,
            stream.thumbnailUrl
        )
    }
}