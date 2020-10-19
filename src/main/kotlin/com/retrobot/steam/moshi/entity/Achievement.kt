package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Achievement(
    @Json(name = "apiname") val apiName: String,
    val achieved: Boolean,
    @Json(name = "unlocktime") val unlockTime: Long,
    val name: String,
    val description: String
)