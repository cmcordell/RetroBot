package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayerBanInfo(
    @Json(name = "SteamId") val steamId: String,
    @Json(name = "CommunityBanned") val communityBanned: Boolean,
    @Json(name = "VACBanned") val vacBanned: Boolean,
    @Json(name = "NumberOfVACBans") val numberOfVacBans: Int,
    @Json(name = "DaysSinceLastBan") val daysSinceLastBan: Int,
    @Json(name = "EconomyBan") val economyBan: String
)