package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameStat(
    val name: String,
    @Json(name = "defaultvalue") val defaultValue: Int,
    val displayName: String
)