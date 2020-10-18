package com.retrobot.steam.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Achievement(
    val name: String,
    val percent: Double
)