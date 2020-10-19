package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayerSummary(
    @Json(name = "steamid") val steamId: String,
    @Json(name = "communityvisibilitystate") val communityVisibilityState: Int, // May need an enum
    @Json(name = "profilestate") val profileState: Int, // May need an enum
    @Json(name = "personaname") val personaName: String,
    @Json(name = "profileurl") val profileUrl: String,
    @Json(name = "avatar") val avatarUrl: String,
    @Json(name = "avatarmedium") val avatarMediumUrl: String,
    @Json(name = "avatarfull") val avatarFullUrl: String,
    @Json(name = "lastlogoff") val lastLogOff: Long,
    @Json(name = "personastate") val personaState: Int, // May need an enum
    @Json(name = "realname") val realName: String,
    @Json(name = "primaryclanid") val primaryClanId: String,
    @Json(name = "timecreated") val timeCreated: Long,
    @Json(name = "personastateflags") val personaStateFlags: Int,
    @Json(name = "loccountrycode") val countryCode: String,
    @Json(name = "locstatecode") val stateCode: String,
    @Json(name = "loccityid") val cityId: Int
)