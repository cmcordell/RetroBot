package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Game(
    @Json(name = "appid") val id: String,
    val name: String = "",
    @Json(name = "playtime_2weeks") val playtimeTwoWeeks: Long = 0,
    @Json(name = "playtime_forever") val playtimeForever: Long = 0,
    @Json(name = "img_icon_url") val iconUrl: String = "",
    @Json(name = "img_logo_url") val logoUrl: String = "",
    @Json(name = "has_community_visible_stats") val hasCommunityVisibleStats: Boolean = false,
    @Json(name = "playtime_windows_forever") val playtimeWindowsForever: Long = 0,
    @Json(name = "playtime_mac_forever") val playtimeMacForever: Long = 0,
    @Json(name = "playtime_linux_forever") val playtimeLinuxForever: Long = 0,
)