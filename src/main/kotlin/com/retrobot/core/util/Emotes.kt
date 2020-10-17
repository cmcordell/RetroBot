package com.retrobot.core.util

import com.retrobot.core.Emote.Character.A
import com.retrobot.core.Emote.Character.ASTERISK
import com.retrobot.core.Emote.Character.B
import com.retrobot.core.Emote.Character.C
import com.retrobot.core.Emote.Character.D
import com.retrobot.core.Emote.Character.E
import com.retrobot.core.Emote.Character.EIGHT
import com.retrobot.core.Emote.Character.EXCLAMATION_MARK
import com.retrobot.core.Emote.Character.F
import com.retrobot.core.Emote.Character.FIVE
import com.retrobot.core.Emote.Character.FOUR
import com.retrobot.core.Emote.Character.G
import com.retrobot.core.Emote.Character.H
import com.retrobot.core.Emote.Character.HASH
import com.retrobot.core.Emote.Character.I
import com.retrobot.core.Emote.Character.J
import com.retrobot.core.Emote.Character.K
import com.retrobot.core.Emote.Character.L
import com.retrobot.core.Emote.Character.M
import com.retrobot.core.Emote.Character.N
import com.retrobot.core.Emote.Character.NINE
import com.retrobot.core.Emote.Character.O
import com.retrobot.core.Emote.Character.ONE
import com.retrobot.core.Emote.Character.P
import com.retrobot.core.Emote.Character.Q
import com.retrobot.core.Emote.Character.QUESTION_MARK
import com.retrobot.core.Emote.Character.R
import com.retrobot.core.Emote.Character.S
import com.retrobot.core.Emote.Character.SEVEN
import com.retrobot.core.Emote.Character.SIX
import com.retrobot.core.Emote.Character.T
import com.retrobot.core.Emote.Character.THREE
import com.retrobot.core.Emote.Character.TWO
import com.retrobot.core.Emote.Character.U
import com.retrobot.core.Emote.Character.V
import com.retrobot.core.Emote.Character.W
import com.retrobot.core.Emote.Character.X
import com.retrobot.core.Emote.Character.Y
import com.retrobot.core.Emote.Character.Z
import com.retrobot.core.Emote.Character.ZERO
import com.vdurmont.emoji.EmojiManager
import java.util.regex.Pattern
import kotlin.math.min


object Emotes {
    private const val REGEX_CUSTOM_EMOTE = "<a?:.{0,32}:(\\d+)>"
    private val PATTERN_CUSTOM_EMOTE = Pattern.compile(REGEX_CUSTOM_EMOTE)
    private const val TWEMOJI_URL = "https://twemoji.maxcdn.com/v/latest/72x72/"
    private const val FOLDER_UNICODE_EMOTES = "unicodeemotes"
    private const val PROGRESS_BAR_PARTS = 20
    private const val PROGRESS_BAR_FILL = "█"
    private const val PROGRESS_BAR_EMPTY = " "
    private val CHARACTER_EMOTES = mapOf(
        "0" to ZERO,
        "1" to ONE,
        "2" to TWO,
        "3" to THREE,
        "4" to FOUR,
        "5" to FIVE,
        "6" to SIX,
        "7" to SEVEN,
        "8" to EIGHT,
        "9" to NINE,
        "a" to A,
        "b" to B,
        "c" to C,
        "d" to D,
        "e" to E,
        "f" to F,
        "g" to G,
        "h" to H,
        "i" to I,
        "j" to J,
        "k" to K,
        "l" to L,
        "m" to M,
        "n" to N,
        "o" to O,
        "p" to P,
        "q" to Q,
        "r" to R,
        "s" to S,
        "t" to T,
        "u" to U,
        "v" to V,
        "w" to W,
        "x" to X,
        "y" to Y,
        "z" to Z,
        "?" to QUESTION_MARK,
        "!" to EXCLAMATION_MARK,
        "#" to HASH,
        "*" to ASTERISK
    )

    fun isGuildEmote(emote: String) : Boolean {
        return PATTERN_CUSTOM_EMOTE.matcher(emote).matches()
    }

    fun isUnicodeEmote(emote: String) : Boolean {
        return EmojiManager.isEmoji(emote)
    }

    fun getGuildEmoteId(emote: String) : String? {
        val matcher = PATTERN_CUSTOM_EMOTE.matcher(emote)
        return when {
            matcher.find() -> matcher.group(1)
            emote.matches(Regex("^\\d+$")) -> emote
            else -> null
        }
    }

    fun getEmoteFor(value: Char) : String? {
        return CHARACTER_EMOTES[value.toString()]
    }

    fun getEmoteFor(value: String) : String? {
        return CHARACTER_EMOTES[value]
    }

    fun getEmoteFor(number: Int) : String? {
        return CHARACTER_EMOTES[number.toString()]
    }

    fun makeProgressBar(max: Int, current: Int) : String {
        val fillEnd = min(PROGRESS_BAR_PARTS, (current.toDouble() / max * PROGRESS_BAR_PARTS).toInt())
        val sb = StringBuilder()
        for (part in 1..PROGRESS_BAR_PARTS) {
            if (part <= fillEnd) {
                sb.append(PROGRESS_BAR_FILL)
            } else {
                sb.append(PROGRESS_BAR_EMPTY)
            }
        }
        return sb.toString()
    }

    fun getUnicodeEmotePath(unicode: String) : String {
        val sb = StringBuilder()
        unicode.codePoints().mapToObj(Integer::toHexString).forEach { sb.append(it) }
        return "$FOLDER_UNICODE_EMOTES/$sb.png"
    }

    //
    fun getTwemojiUrl(unicode: String) : String {
        val sb = StringBuilder()
        unicode.codePoints()
                .mapToObj(Integer::toHexString)
                .toArray()
                .forEachIndexed { index, hex ->
                    if (index > 0) sb.append("-")
                    sb.append(hex)
                }
        println("$TWEMOJI_URL$sb.png")
        return "$TWEMOJI_URL$sb.png"
    }
}