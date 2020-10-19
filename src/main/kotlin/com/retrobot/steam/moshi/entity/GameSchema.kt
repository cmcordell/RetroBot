package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameSchema(
    val gameName: String,
    val gameVersion: String,
    @Json(name = "availableGameStats") val gameStats: GameStats
)