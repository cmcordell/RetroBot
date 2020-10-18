package com.retrobot.steam.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameNews(
    @Json(name = "appid") val gameId: String,
    @Json(name = "newsitems") val newsItems: List<NewsItem>,
    val count: Int
)