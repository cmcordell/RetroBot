package com.retrobot.utility

import com.retrobot.core.Bot
import com.retrobot.core.Commands.Utils.Jumbo.ARG_EMOJI
import com.retrobot.core.Commands.Utils.Jumbo.CATEGORY
import com.retrobot.core.Commands.Utils.Jumbo.COMMAND
import com.retrobot.core.Commands.Utils.Jumbo.DESCRIPTION
import com.retrobot.core.Commands.Utils.Jumbo.MAX_SIZE
import com.retrobot.core.Commands.Utils.Jumbo.MIN_SIZE
import com.retrobot.core.Commands.Utils.Jumbo.USAGE
import com.retrobot.core.File.Ext.GIF
import com.retrobot.core.File.Ext.PNG
import com.retrobot.core.domain.CustomEmote
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.UnicodeEmote
import com.retrobot.core.domain.WrappedEmote
import com.retrobot.core.domain.command.Command
import com.retrobot.core.domain.image.Images
import com.retrobot.core.domain.image.StreamingGifWriter
import com.retrobot.core.domain.image.StreamingGifWriter.GifAttributes
import com.retrobot.core.util.Emotes
import com.retrobot.core.util.Logger
import com.retrobot.core.util.Messages
import com.retrobot.core.util.merge
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.MutableImage
import com.vdurmont.emoji.EmojiParser
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URL
import java.time.Duration
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import com.retrobot.core.util.File as FileUtil

/**
 * Enlarges an Emoji
 * Commands:
 * !jumbo <emojis>
 */
class JumboCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        try {
            val scaledSize = determineScaledSize(event.message.contentRaw)
            val outputFile = createOutputImage(event.message, scaledSize)

            // Fail silently if outfile is null
            if (outputFile != null) {
                event.channel.sendFile(outputFile).queue {
                    outputFile.delete()
                }
            } else {
                event.channel.sendMessage("Oops! Something went wrong.")
            }
        } catch (e: Exception) {
            Logger.log(e)
            val message = Messages.generateMissingCommandArgumentsMessage(listOf(ARG_EMOJI), this, guildSettings)
            event.channel.sendMessage(message).queue()
        }
    }

    /**
     * Determine the scaled size of the image.
     * Scaled size is constrained to the range ([MIN_SIZE]..[MAX_SIZE])
     */
    private fun determineScaledSize(message: String) : Int {
        val arguments = message.split(" ")
        var scaledSize = MIN_SIZE
        try {
            for (argument in arguments) {
                if (argument.contains("px", true)) {
                    scaledSize = Regex("[^0-9]").replace(argument, "").toInt()
                } else if (argument.contains("%")) {
                    val percent = Regex("[^0-9]").replace(argument, "").toInt() / 100
                    scaledSize = (percent + 1) * MIN_SIZE
                }
            }
        } catch (e: NumberFormatException) { }
        scaledSize = scaledSize.coerceIn(MIN_SIZE, MAX_SIZE)
        return scaledSize
    }

    /**
     * Create the output image and saves to file.
     * If emotes contains a single GIF then the output will be a GIF
     *     else it will be a PNG
     * @param message
     * @param scaledSize
     * @return file created
     */
    private fun createOutputImage(
            message: Message,
            scaledSize: Int
    ) : File? {
        val emotes = parseOutEmotes(message.contentRaw, message.emotes)
        return when {
            emotes.isEmpty() -> throw Exception()
            isSingleAnimatedEmote(emotes) -> createOutputGif((emotes[0] as CustomEmote).emote, scaledSize)
            else -> createOutputPng(emotes, scaledSize)
        }
    }

    private fun parseOutEmotes(message: String, emotes: List<Emote>): List<WrappedEmote> {
        val emoteMap = getCustomEmotesMap(message, emotes).merge(getUnicodeEmotesMap(message))
        return emoteMap.toSortedMap().values.toList()
    }

    private fun getCustomEmotesMap(text: String, emotes: List<Emote>) : Map<Int, WrappedEmote> {
        val customEmoteMap = mutableMapOf<Int, WrappedEmote>()
        var checkIndex = 0
        var emoteIndex = 0

        while (emoteIndex < emotes.size) {
            val startIndex = text.indexOf("<", checkIndex)
            val endIndex = text.indexOf(">", startIndex) + 1
            checkIndex = startIndex + 1
            val customEmoteCandidate = text.substring(startIndex, endIndex)
            if (Emotes.isGuildEmote(customEmoteCandidate)) {
                customEmoteMap[startIndex] = CustomEmote(emotes[emoteIndex++])
            }
        }

        return customEmoteMap
    }

    private fun getUnicodeEmotesMap(text: String) : Map<Int, WrappedEmote> {
        val emotes = EmojiParser.extractEmojis(text)
        val unicodeEmoteMap = mutableMapOf<Int, WrappedEmote>()

        for (emote in emotes) {
            unicodeEmoteMap[text.indexOf(emote)] = UnicodeEmote(emote)
        }

        return unicodeEmoteMap
    }

    private fun isSingleAnimatedEmote(emotes: List<WrappedEmote>) : Boolean {
        return emotes.size == 1
                && emotes[0] is CustomEmote
                && (emotes[0] as CustomEmote).emote.isAnimated
    }

    private fun createOutputGif(
            emote: Emote,
            scaledSize: Int
    ) : File? {
        var gif: StreamingGifWriter.GifStream? = null
        var file: File? = null
        try {
            val inputStream = URL(emote.imageUrl).openStream()
            val reader = ImageIO.getImageReadersByFormatName(GIF).next() as ImageReader
            reader.input = ImageIO.createImageInputStream(inputStream)
            val frames = Images.readGif(reader)

            file = FileUtil.createTemporaryFile(GIF)
            gif = StreamingGifWriter(Duration.ofMillis(frames[0].delay.toLong()), true)
                    .prepareStream(file, BufferedImage.TYPE_INT_ARGB)

            for (frame in frames) {
                val scaledFrame = MutableImage(frame.image).toImmutableImage()
                        .fit(scaledSize, scaledSize)

                val attributes = GifAttributes(
                        disposal = frame.disposal,
                        delay = Duration.ofMillis(frame.delay.toLong())
                )
                gif.writeFrame(scaledFrame, attributes)
            }
        } catch (e : Exception) {
            println("Error creating output GIF: ${e.printStackTrace()}")
            file?.delete()
        } finally {
            gif?.close()
            return file
        }
    }

    private fun createOutputPng(
            emotes: List<WrappedEmote>,
            scaledSize: Int
    ) : File? {
        var file: File? = null
        try {
            val images = mutableListOf<BufferedImage?>()
            for (emote in emotes) {
                when (emote) {
                    is CustomEmote -> images.add(resizeDiscordEmote(emote.emote, scaledSize))
                    is UnicodeEmote -> images.add(resizeUnicodeEmote(emote.unicode, scaledSize))
                }
            }

            val image = if (images.size > 1) Images.concatenateImages(images.filterNotNull()) else images[0]
            file = FileUtil.createTemporaryFile(PNG)
            ImageIO.write(image, PNG, file)
        } catch (e: Exception) {
            println("Error creating output PNG: ${e.printStackTrace()}")
            file?.delete()
        } finally {
            return file
        }
    }

    private fun resizeDiscordEmote(
            emote: Emote,
            scaledSize: Int
    ) : BufferedImage? {
        var scaledImage: BufferedImage? = null
        try {
            val inputStream = URL(emote.imageUrl).openStream()
            if (emote.isAnimated) {
                val reader = ImageIO.getImageReadersByFormatName(GIF).next() as ImageReader
                reader.input = ImageIO.createImageInputStream(inputStream)
                val image = Images.readGif(reader)[0].image
                scaledImage = MutableImage(image).toImmutableImage()
                        .fit(scaledSize, scaledSize)
                        .toNewBufferedImage(image.type)
            } else {
                val image = ImmutableImage.loader().fromStream(inputStream)
                scaledImage = image.fit(scaledSize, scaledSize)
                        .toNewBufferedImage(image.type)
            }
            inputStream.close()
        } catch (e: IOException) {
            println("Error resizing custom emote (${emote.id}): ${e.printStackTrace()}")
        }

        return scaledImage
    }

    private fun resizeUnicodeEmote(
            unicode: String,
            scaledSize: Int
    ) : BufferedImage? {
        return try {
            val filePath = Emotes.getUnicodeEmotePath(unicode)
            val image = ImmutableImage.loader().fromStream(FileUtil.getResourceAsStream(filePath))
            image.fit(scaledSize, scaledSize)
                    .toNewBufferedImage(image.type)
        } catch (e: IOException) {
            println("Error resizing unicode emote ($unicode): ${e.printStackTrace()}")
            null
        }
    }
}