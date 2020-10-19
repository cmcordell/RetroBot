package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameAchievement(
    val name: String,
    @Json(name = "defaultvalue") val defaultValue: Int,
    val displayName: String,
    val hidden: Int,
    @Json(name = "icon") val iconUrl: String,
    @Json(name = "icongray") val iconGrayUrl: String
)