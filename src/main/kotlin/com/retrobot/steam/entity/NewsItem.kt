package com.retrobot.steam.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class NewsItem(
    @Json(name = "gid") val id: String,
    val title: String,
    val url: String,
    @Json(name = "is_external_url") val isExternalUrl: Boolean,
    val author: String,
    val contents: String,
    @Json(name = "feedlabel") val feedLabel: String,
    val date: Long, // TODO Moshi should auto convert to date
    @Json(name = "feedname") val feedName: String,
    @Json(name = "feed_type") val feedType: Int,
    @Json(name = "appid") val gameId: String
)