package com.retrobot.steam

import com.retrobot.steam.moshi.adapter.RelationshipAdapter
import com.retrobot.steam.moshi.entity.*
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.buffer
import okio.source
import java.net.URL

/**
 * Steam API Wrapper
 * // TODO Test
 */
class SteamService(private val apiKey: String = "") {

    private val CURRENT_PLAYERS_COUNT_URL = "http://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v0001/?appid=%s&format=json"
    private val GAME_NEWS_URL = "https://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=%s%s&format=json"
    private val GAME_SCHEMA_URL = "https://api.steampowered.com/ISteamUserStats/GetSchemaForGame/v2/?key=%s&appid=%s&format=json"
    private val GLOBAL_ACHEIVEMENT_PERCENTAGES_URL = "https://api.steampowered.com/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/?gameid=%s&format=json"
    private val GLOBAL_STATS_URL = "https://api.steampowered.com/ISteamUserStats/GetGlobalStatsForGame/v0001/?appid=%s&count=%d%s&format=json"
    private val IS_PLAYING_SHARED_GAME_URL = "https://api.steampowered.com/IPlayerService/IsPlayingSharedGame/v0001/?key=%s&%s&appid_playing=%s&format=json"
    private val PLAYER_ACHIEVEMENTS_URL = "https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/?key=%s&steamid=%s&appid=%s&l=english&format=json"
    private val PLAYER_BANS_URL = "https://api.steampowered.com/ISteamUser/GetPlayerBans/v1/?key=%s&steamids=%s&format=json"
    private val PLAYER_FRIENDS_LIST_URL = "https://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=%s&steamid=%s&relationship=%s&format=json"
    private val PLAYER_OWNED_GAMES_URL = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=%s&steamid=%s&include_appinfo=%s&include_played_free_games=%s&format=json"
    private val PLAYER_RECENTLY_PLAYED_GAMES_URL = "https://api.steampowered.com/IPlayerService/GetRecentlyPlayedGames/v0001/?key=%s&steamid=%s%s&format=json"
    private val PLAYER_SUMMARIES_URL = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=%s&steamids=%s&format=json"

    private val moshi: Moshi = Moshi.Builder()
        .add(RelationshipAdapter())
        .build()

    /**
     * Returns the latest news for a specific game.
     *
     * @param gameId The game's Steam App ID
     * @param count How many entities you want returned
     * @param maxLength Maximum length of each news entry
     * @return [List] of [NewsItem]s
     */
    fun getNewsForGame(gameId: String, count: Int = 0, maxLength: Int = 0): List<NewsItem> {
        val argBuilder = StringBuilder()
        if (count > 0) argBuilder.append("&count=$count")
        if (maxLength > 0) argBuilder.append("&maxlength=$maxLength")
        val url = GAME_NEWS_URL.format(gameId, argBuilder.toString())
        val inputStream = URL(url).openStream()

        return moshi.adapter(AppNews::class.java)
            .fromJson(JsonReader.of(inputStream.source().buffer()))?.gameNews?.newsItems ?: listOf()
    }

    /**
     * Returns the global achievements overview for a specific game in percentages.
     *
     * @param gameId The game's Steam App ID
     * @return [List] of [GlobalAchievement]s
     */
    fun getGlobalAchievementPercentagesGame(gameId: String): List<GlobalAchievement> {
        val url = GLOBAL_ACHEIVEMENT_PERCENTAGES_URL.format(gameId)
        val inputStream = URL(url).openStream()

        return moshi.adapter(GlobalAchievementsPercentages::class.java)
            .fromJson(JsonReader.of(inputStream.source().buffer()))?.achievements?.achievements ?: listOf()
    }

    /**
     * Returns the global achievements from [stats] for a specific game in percentages.
     *
     * @param gameId The game's Steam App ID
     * @param stats [List] of names of the [GlobalAchievement]s required
     * @return [List] of [GlobalAchievement]s
     */
    fun getGlobalStatsForGame(gameId: String, stats: List<String>): List<GlobalAchievement> {
        TODO("Figure out how to do dynamic JSON naming in Moshi")
//        if (stats.isEmpty()) return listOf()
//
//        val statSb = StringBuilder()
//        stats.forEachIndexed { index, stat ->
//            if (index > 0) statSb.append("&")
//            statSb.append("name[$index]=$stat")
//        }
//        val url = GLOBAL_STATS_URL.format(gameId, stats.size, statSb.toString())
//        val inputStream = URL(url).openStream()
//
//        return listOf()
//        return moshi.adapter(AppNews::class.java).fromJson(JsonReader.of(inputStream.source().buffer()))
    }

    /**
     * Returns the number of players currently playing the specified game.
     *
     * @param gameId The game's Steam App ID
     * @return The number of players currently playing this game
     */
    fun getNumberOfCurrentPlayers(gameId: String): Int {
        val url = CURRENT_PLAYERS_COUNT_URL.format(gameId)
        val inputStream = URL(url).openStream()
        val adapter = moshi.adapter<ResponseWrapper<PlayerCountResponse>>(
            Types.newParameterizedType(ResponseWrapper::class.java, PlayerCountResponse::class.java))
        return adapter.fromJson(JsonReader.of(inputStream.source().buffer()))?.response?.playerCount ?: 0
    }

    /**
     * Returns basic profile information for a list of player Steam IDs.
     * Profiles will only be fetched for the first 100 Steam IDs.
     *
     * @param steamIds The players' Steam IDs
     * @return [List] of [PlayerSummary]s
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun getPlayerSummaries(steamIds: List<String>): List<PlayerSummary> {
        checkApiKey()
        if (steamIds.isEmpty()) return listOf()

        val steamIdsSb = StringBuilder()
        steamIds.take(100).forEachIndexed { index, steamId ->
            if (index > 0) steamIdsSb.append(",")
            steamIdsSb.append(steamId)
        }

        val url = PLAYER_SUMMARIES_URL.format(apiKey, steamIdsSb.toString())
        val inputStream = URL(url).openStream()
        val adapter = moshi.adapter<ResponseWrapper<PlayerSummariesResponse>>(
            Types.newParameterizedType(ResponseWrapper::class.java, PlayerSummariesResponse::class.java))
        return adapter.fromJson(JsonReader.of(inputStream.source().buffer()))?.response?.playerSummaries ?: listOf()
    }

    /**
     * Returns the friend list of any Steam user, provided their Steam Community profile visibility is set to "Public".
     *
     * @param steamId The player's Steam App ID
     * @param relationship [Relationship], [Relationship.ALL] or [Relationship.FRIEND]
     * @return [List] of [Friend]s
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun getFriendList(steamId: String, relationship: Relationship = Relationship.ALL): List<Friend> {
        checkApiKey()

        val url = PLAYER_FRIENDS_LIST_URL.format(apiKey, steamId, relationship.value)
        val inputStream = URL(url).openStream()
        return moshi.adapter(FriendsListResponse::class.java)
            .fromJson(JsonReader.of(inputStream.source().buffer()))?.friendsList?.friends ?: listOf()
    }

    /**
     * Returns a list of achievements for this user by Steam App ID.
     *
     * @param steamId The player's Steam App ID
     * @param gameId The game's Steam App ID
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun getPlayerAchievements(steamId: String, gameId: String): List<Achievement> {
        checkApiKey()

        val url = PLAYER_ACHIEVEMENTS_URL.format(apiKey, steamId, gameId)
        val inputStream = URL(url).openStream()
        return moshi.adapter(PlayerStatsResponse::class.java)
            .fromJson(JsonReader.of(inputStream.source().buffer()))?.playerStats?.achievements ?: listOf()
    }

    /**
     * Returns a list of achievements for this user by Steam App ID.
     *
     * @param steamId The player's Steam App ID
     * @param gameId The game's Steam App ID
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun getUserStatsForGame(steamId: String, gameId: String) {
        checkApiKey()
        TODO("This seems to be a repeat of getPlayerAchievements with less data.  May be unnecessary to implement")
    }

    /**
     * Returns a list of games a player owns along with some playtime information, if the profile is publicly visible.
     * Private, friends-only, and other privacy settings are not supported unless you are asking for your own personal
     * details (i.e. the WebAPI key you are using is linked to the Steam App ID you are requesting).
     *
     * @param steamId The player's Steam App ID
     * @param includeGameInfo Include game name and logo information in the output.  Default is false.
     * @param includePlayedFreeGames Include free games (technically owned by everyone) this user has played before.  Default is true.
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun getOwnedGames(steamId: String, includeGameInfo: Boolean = true, includePlayedFreeGames: Boolean = true): List<Game> {
        checkApiKey()

        // TODO Fix: includeGameInfo is currently hardcoded to true because I don't know how to handle different possible json results with Moshi
        val url = PLAYER_OWNED_GAMES_URL.format(apiKey, steamId, true, includePlayedFreeGames)
        val inputStream = URL(url).openStream()
        val adapter = moshi.adapter<ResponseWrapper<PlayerOwnedGamesResponse>>(
            Types.newParameterizedType(ResponseWrapper::class.java, PlayerOwnedGamesResponse::class.java))
        return adapter.fromJson(JsonReader.of(inputStream.source().buffer()))?.response?.games ?: listOf()
    }

    /**
     * Returns a list of games a player has played in the last two weeks, if the profile is publicly visible.
     * Private, friends-only, and other privacy settings are not supported unless you are asking for your own personal
     * details (ie the WebAPI key you are using is linked to the Steam App ID you are requesting).
     *
     * @param steamId The player's Steam App ID
     * @param count The amount of games to return
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun getRecentlyPlayedGames(steamId: String, count: Int = 0): List<Game> {
        checkApiKey()

        val countArg = if (count > 0) "&count=$count" else ""
        val url = PLAYER_RECENTLY_PLAYED_GAMES_URL.format(apiKey, steamId, countArg)
        val inputStream = URL(url).openStream()
        val adapter = moshi.adapter<ResponseWrapper<PlayerRecentlyPlayedGamesResponse>>(
            Types.newParameterizedType(ResponseWrapper::class.java, PlayerRecentlyPlayedGamesResponse::class.java))
        return adapter.fromJson(JsonReader.of(inputStream.source().buffer()))?.response?.games ?: listOf()
    }

    /**
     * Returns true if a borrowing account is currently playing this game,
     * false if the game is not borrowed or the borrower currently doesn't play this game.
     *
     * @param steamId The player's Steam App ID
     * @param gameId The game's Steam App ID
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun isPlayingSharedGame(steamId: String, gameId: String): Boolean {
        checkApiKey()

        val url = IS_PLAYING_SHARED_GAME_URL.format(apiKey, steamId, gameId)
        val inputStream = URL(url).openStream()
        val adapter = moshi.adapter<ResponseWrapper<IsPlayingSharedGameResponse>>(
            Types.newParameterizedType(ResponseWrapper::class.java, IsPlayingSharedGameResponse::class.java))
        val lenderSteamId = adapter.fromJson(JsonReader.of(inputStream.source().buffer()))?.response?.lenderSteamId
        return (lenderSteamId != null && lenderSteamId != "0")
    }

    /**
     * Returns some basic metadata for a Steam game.
     *
     * @param gameId The game's Steam App ID
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun getSchemaForGame(gameId: String): GameSchema? {
        checkApiKey()

        val url = GAME_SCHEMA_URL.format(apiKey, gameId)
        val inputStream = URL(url).openStream()
        return moshi.adapter(GameSchemaResponse::class.java)
            .fromJson(JsonReader.of(inputStream.source().buffer()))?.gameSchema
    }

    /**
     * Returns Community, VAC, and Economy ban statuses for given players.
     *
     * @param steamIds The players' Steam IDs
     * @throws IllegalStateException If the Steam [apiKey] has not been set
     */
    fun getPlayerBans(steamIds: List<String>): List<PlayerBanInfo> {
        checkApiKey()

        if (steamIds.isEmpty()) return listOf()

        val steamIdsSb = StringBuilder()
        steamIds.take(100).forEachIndexed { index, steamId ->
            if (index > 0) steamIdsSb.append(",")
            steamIdsSb.append(steamId)
        }

        val url = PLAYER_BANS_URL.format(apiKey, steamIdsSb.toString())
        val inputStream = URL(url).openStream()
        return moshi.adapter(PlayerBansResponse::class.java)
            .fromJson(JsonReader.of(inputStream.source().buffer()))?.playersBanInfo ?: listOf()
    }

    private fun checkApiKey() {
        if (apiKey.isBlank()) throw IllegalStateException("API Key must be set to use this function.")
    }
}