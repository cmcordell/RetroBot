package com.retrobot.steam.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/*
 * Steam wraps their JSON Responses multiple times.
 * These classes are needed for Moshi to automatically read the JSON from input.
 */

@JsonClass(generateAdapter = true)
internal data class AppNews(
    @Json(name = "appnews") val gameNews: GameNews
)

@JsonClass(generateAdapter = true)
internal data class AchievementsPercentages(
    @Json(name = "achievementpercentages") val achievements: Achievements
)

@JsonClass(generateAdapter = true)
internal data class Achievements(
    @Json(name = "achievements") val achievements: List<Achievement>
)