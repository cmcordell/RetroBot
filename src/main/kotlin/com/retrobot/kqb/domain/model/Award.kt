package com.retrobot.kqb.domain.model

import com.retrobot.core.Emote
import com.squareup.moshi.JsonClass
import java.awt.Color


data class Award(
        val awardType: AwardType,
        val season: String,
        val circuit: String,
        val division: String,
        val conference: String,
        val week: String,
        val player: String,
        val stats: List<Statistic>
)


enum class AwardType(
        val title: String,
        val description: String,
        val color: Color,
        val emoji: String
) {
    QUEEN_OF_THE_HIVE(
        "Queen of the Hive",
        "Highest kill/death ratio for a Queen who competes in at least two sets",
        Color(191, 144, 0),
        Emote.Unicode.PRINCESS
    ),
    ETERNAL_WARRIOR(
        "Eternal Warrior",
        "Highest number of kills per set for a Warrior who competes in at least two sets",
        Color(180, 95, 6),
        Emote.Unicode.CROSSED_SWORDS
    ),
    PURPLE_HEART(
        "Purple Heart",
        "Highest average death count for a worker on a winning team who competes in at least two sets",
        Color(153, 0, 255),
        Emote.Unicode.HEART_PURPLE
    ),
    BERRY_BONANZA(
        "Berry Bonanza",
        "Highest average berry count per set for a worker who competes in at least two sets",
        Color(173, 27, 77),
        Emote.Unicode.BLUEBERRIES
    ),
    SNAIL_WHISPERER(
        "Snail Whisperer",
        "Highest average snail miles per set for a worker who competes in at least two sets",
        Color(61, 133, 198),
        Emote.Unicode.SNAIL
    ),
    TRIPLE_THREAT(
        "Triple Threat",
        "Most kills, berries, and snail compared to other workers who competed in at least one set",
        Color(38, 166, 154),
        Emote.Unicode.RECYCLE_SYMBOL
    )
}

@JsonClass(generateAdapter = true)
data class Statistic(
        val name: String,
        val value: Double
)