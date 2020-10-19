package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Friend(
    @Json(name = "steamid") val steamId: String,
    val relationship: Relationship,
    @Json(name = "friend_since") val friendSince: Long
)