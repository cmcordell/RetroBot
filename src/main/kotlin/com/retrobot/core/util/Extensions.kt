package com.retrobot.core.util

import com.retrobot.core.SPECIFIER_BOT_NAME
import com.retrobot.core.SPECIFIER_COMMAND_PREFIX
import com.retrobot.core.domain.GuildSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.lang.String.format
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


// TODO Wrap all usages of Long as millis to an inline class Millis and refactor this extension to Millis.convert...
fun Long.convertMillisToTime(zoneId: ZoneId) : String {
    val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)
    return DateTimeFormatter.ofPattern("EEEE, MMM d 'at' h:mma 'EST'").format(localDateTime)
}

fun String.containsInOrder(itemsInOrder: List<String>, ignoreCase: Boolean = false): Boolean {
    var currentIndex = -1
    for (item in itemsInOrder) {
        val index = indexOf(item, ignoreCase = ignoreCase)
        if (index > currentIndex) {
            currentIndex = index
        } else {
            return false
        }
    }
    return true
}

fun String.removePrefixIgnoreCase(prefix: CharSequence): String {
    if (startsWith(prefix, true)) {
        return substring(prefix.length)
    }
    return this
}

fun String.sanitize(): String {
    return Markdown.sanitize(this)
}

fun String.formatGuildInfo(guildSettings: GuildSettings): String {
    return replace(SPECIFIER_COMMAND_PREFIX, guildSettings.commandPrefix)
            .replace(SPECIFIER_BOT_NAME, guildSettings.botNickname)
}

fun Color.hexString(): String = format("#%02x%02x%02x", red, green, blue)

fun Duration.format(
    includeSeconds: Boolean = false
): String {
    val sb = StringBuilder()

    val days = toDaysPart()
    if (days > 0) {
        sb.append("$days Days")
    }

    val hours = toHoursPart()
    if (hours > 0) {
        if (sb.isNotBlank()) sb.append(" ")
        sb.append("$hours Hours")
    }

    val minutes = toMinutesPart()
    if (minutes > 0) {
        if (sb.isNotBlank()) sb.append(" ")
        sb.append("$minutes Minutes")
    }

    val seconds = toSecondsPart()
    if (includeSeconds && seconds > 0) {
        if (sb.isNotBlank()) sb.append(" ")
        sb.append("$seconds Seconds")
    }

    return sb.toString()
}

fun <E> Collection<E>.toDelimitedString(): String {
    return this.toDelimitedString(", ")
}

fun <E> Collection<E>.toDelimitedString(delimiter: String): String {
    return this.toDelimitedString(delimiter) { it.toString() }
}

fun <E> Collection<E>.toDelimitedString(delimiter: String, map: (E) -> String): String {
    val sb = StringBuilder()
    forEachIndexed { index, e ->
        if (index > 0) {
            sb.append(delimiter)
        }
        sb.append(map(e))
    }
    return sb.toString()
}

fun <K, V> Map<K, V>.merge(vararg others: Map<K, V>, keepOriginal: Boolean = true): Map<K, V> =
        this.toMutableMap().apply {
            others.forEach { other ->
                other.forEach {
                    merge(it.key, it.value) { orig, new ->
                        if (keepOriginal) orig else new
                    }
                }
            }
        }

fun <V> caseInsensitiveTreeMapOf(vararg pairs: Pair<String, V>): TreeMap<String, V>
        = TreeMap<String, V>(String.CASE_INSENSITIVE_ORDER).apply { putAll(pairs) }

/**
 * [CoroutineScope.launch] that will catch errors and continue processing.
 */
fun CoroutineScope.launchContinue(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
): Job {
    return launch(context, start) {
        try {
            block()
        } catch (e: Exception) {
            Logger.log(e)
        }
    }
}

fun EmbedBuilder.addFields(fields: Iterable<MessageEmbed.Field>): EmbedBuilder {
    fields.forEach { field ->
        addField(field)
    }
    return this
}

fun EmbedBuilder.buildMessage(): Message = MessageBuilder(this).build()
fun EmbedBuilder.toMessageBuilder(): MessageBuilder = MessageBuilder(this)
fun MessageEmbed.toMessage(): Message = MessageBuilder().setEmbed(this).build()
fun MessageEmbed.toBuilder(): EmbedBuilder = EmbedBuilder(this)
fun Message.toEmbedBuilders(): List<EmbedBuilder> = embeds.map { it.toBuilder() }



