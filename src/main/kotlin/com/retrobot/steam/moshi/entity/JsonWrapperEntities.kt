package com.retrobot.steam.moshi.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/*
 * Steam wraps their JSON Responses multiple times.
 * These classes are needed for Moshi to automatically read the JSON from input.
 */

@JsonClass(generateAdapter = true)
internal data class AppNews(
    @Json(name = "appnews") val gameNews: GameNews
)

@JsonClass(generateAdapter = true)
internal data class GameNews(
    @Json(name = "appid") val gameId: String,
    @Json(name = "newsitems") val newsItems: List<NewsItem>,
    val count: Int
)

@JsonClass(generateAdapter = true)
internal data class GlobalAchievementsPercentages(
    @Json(name = "achievementpercentages") val achievements: GlobalAchievements
)

@JsonClass(generateAdapter = true)
internal data class GlobalAchievements(
    @Json(name = "achievements") val achievements: List<GlobalAchievement>
)

@JsonClass(generateAdapter = true)
internal data class FriendsListResponse(
    @Json(name = "friendslist") val friendsList: FriendsList
)

@JsonClass(generateAdapter = true)
internal data class FriendsList(
    val friends: List<Friend>
)

@JsonClass(generateAdapter = true)
internal data class PlayerStatsResponse(
    @Json(name = "playerstats") val playerStats: PlayerStats
)

@JsonClass(generateAdapter = true)
internal data class PlayerStats(
    @Json(name = "steamID") val steamId: String,
    val gameName: String,
    val achievements: List<Achievement>
)

@JsonClass(generateAdapter = true)
internal data class GameSchemaResponse(
    @Json(name = "game") val gameSchema: GameSchema
)

@JsonClass(generateAdapter = true)
internal data class PlayerBansResponse(
    @Json(name = "players") val playersBanInfo: List<PlayerBanInfo>
)

internal interface Response

@JsonClass(generateAdapter = true)
internal class ResponseWrapper<R: Response>(val response: R)

@JsonClass(generateAdapter = true)
internal data class PlayerCountResponse(
    @Json(name = "player_count") val playerCount: Int,
    val result: Int
) : Response

@JsonClass(generateAdapter = true)
internal data class PlayerSummariesResponse(
    @Json(name = "players") val playerSummaries: List<PlayerSummary>
) : Response

@JsonClass(generateAdapter = true)
internal data class PlayerOwnedGamesResponse(
    @Json(name = "game_count") val gameCount: Int,
    val games: List<Game>
) : Response

@JsonClass(generateAdapter = true)
internal data class PlayerRecentlyPlayedGamesResponse(
    @Json(name = "total_count") val gameCount: Int,
    val games: List<Game>
) : Response

@JsonClass(generateAdapter = true)
internal data class IsPlayingSharedGameResponse(
    @Json(name = "lender_steamid") val lenderSteamId: String
) : Response

