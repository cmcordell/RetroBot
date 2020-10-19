package com.retrobot.steam.moshi.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GlobalAchievement(
    val name: String,
    val percent: Double
)