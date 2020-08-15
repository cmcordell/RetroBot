package com.retrobot.twitch.domain

import com.github.twitch4j.helix.domain.Game
import java.time.Duration
import java.util.*
import java.util.regex.Pattern

/**
 * Represent the Domain version of a Twitch4J [com.github.twitch4j.helix.domain.Stream].
 */
data class Stream(
    val id: String,
    val userId: String,
    val userName: String,
    val game: Game,
    val communityIds: List<UUID>,
    val type: String,
    val title: String,
    val viewerCount: Int,
    val startedAt: Calendar,
    val tagIds: List<UUID>,
    val language: String,
    val thumbnailUrl: String
) {
    val uptime: Duration = Duration.between(startedAt.toInstant(), Calendar.getInstance().toInstant())

    /**
     * Gets the thumbnail url for specific dimensions
     *
     * @param width  thumbnail width
     * @param height thumbnail height
     * @return String
     */
    fun getSizedThumbnailUrl(width: Int, height: Int): String {
        return thumbnailUrl.replace(Pattern.quote("{width}"), width.toString()).replace(Pattern.quote("{height}"), height.toString())
    }
}