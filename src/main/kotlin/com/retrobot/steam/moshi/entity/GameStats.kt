package com.retrobot.steam.moshi.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameStats(
    val achievements: List<GameAchievement>,
    val stats: List<GameStat>
)