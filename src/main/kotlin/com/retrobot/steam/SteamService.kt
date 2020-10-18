package com.retrobot.steam

import com.retrobot.steam.entity.*
import com.retrobot.steam.entity.AchievementsPercentages
import com.retrobot.steam.entity.AppNews
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import java.net.URL

/**
 * Steam API Wrapper
 */
class SteamService {

    private val APP_ID_KQB = "663670"

    private val GAME_NEWS_URL = "https://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=%s%s&format=json"
    private val GLOBAL_ACHEIVEMENT_PERCENTAGES_URL = "https://api.steampowered.com/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/?gameid=%s&format=json"
    private val GLOBAL_STATS_URL = "https://api.steampowered.com/ISteamUserStats/GetGlobalStatsForGame/v0001/?appid=%s&count=%d&format=json"

    private val moshi: Moshi = Moshi.Builder().build()


    fun getNewsForGame(gameId: String, count: Int = 0, maxLength: Int = 0): GameNews? {
        val argBuilder = StringBuilder()
        if (count > 0) argBuilder.append("&count=$count")
        if (maxLength > 0) argBuilder.append("&maxlength=$maxLength")
        val url = GAME_NEWS_URL.format(gameId, argBuilder.toString())
        val inputStream = URL(url).openStream()

        return moshi.adapter(AppNews::class.java).fromJson(JsonReader.of(inputStream.source().buffer()))?.gameNews
    }

    fun getGlobalAchievementPercentagesGame(gameId: String): List<Achievement> {
        val url = GLOBAL_ACHEIVEMENT_PERCENTAGES_URL.format(gameId)
        val inputStream = URL(url).openStream()

        return moshi.adapter(AchievementsPercentages::class.java)
            .fromJson(JsonReader.of(inputStream.source().buffer()))
            ?.achievements
            ?.achievements
            ?: listOf()
    }

    fun getGlobalStatsForGame(gameId: String, stats: Array<String>) {
        TODO("Implement")
    }

    // Needs API Key
    fun getPlayerSummaries(steamIds: Array<String>) {
        TODO("Implement")
    }

    // Needs API Key
    fun getFriendList(steamId: String, relationship: Relationship) {
        TODO("Implement")
    }

    // Needs API Key
    fun getPlayerAchievements(steamId: String, gameId: String) {
        TODO("Implement")
    }

    // Needs API Key
    fun getUserStatsForGame(steamId: String, gameId: String) {
        TODO("Implement")
    }

    // Needs API Key
    fun getOwnedGames(steamId: String, includeGameInfo: Boolean, includePlayedFreeGames: Boolean, gameIdsToFilter: Array<String>) {
        TODO("Implement")
    }

    // Needs API Key
    fun getRecentlyPlayedGames(steamId: String, count: Int) {
        TODO("Implement")
    }

    // Needs API Key
    fun isPlayingSharedGame(steamId: String, gameId: String) {
        TODO("Implement")
    }

    // Needs API Key
    fun getSchemaForGame(gameId: String) {
        TODO("Implement")
    }

    // Needs API Key
    fun getPlayerBans(steamIds: Array<String>) {
        TODO("Implement")
    }
}