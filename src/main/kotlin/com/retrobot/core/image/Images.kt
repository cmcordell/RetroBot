package com.retrobot.core.image

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageReader
import javax.imageio.metadata.IIOMetadataNode
import kotlin.math.max

/**
 *
 */
object Images {
    enum class Direction {
        HORIZONTAL, VERTICAL
    }

    fun concatenateImages(
            images: List<BufferedImage>,
            direction: Direction = Direction.HORIZONTAL
    ) : BufferedImage {
        var width = 0
        var height = 0
        for (image in images) {
            when (direction) {
                Direction.HORIZONTAL -> {
                    width += image.width
                    height = max(height, image.height)
                }
                Direction.VERTICAL -> {
                    height += image.height
                    width = max(width, image.width)
                }
            }
        }

        val concatImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics2D = concatImage.createGraphics()

        var directionCurrent = 0
        for (image in images) {
            directionCurrent += when (direction) {
                Direction.HORIZONTAL -> {
                    graphics2D.drawImage(image, directionCurrent, 0, null)
                    image.width
                }
                Direction.VERTICAL -> {
                    graphics2D.drawImage(image, 0, directionCurrent, null)
                    image.height
                }
            }
        }
        graphics2D.dispose()

        return concatImage
    }

    /**
     * // TODO Clean this function up
     * // TODO Make own GIF Reading Library in kotlin
     * Copied from https://stackoverflow.com/questions/8933893/convert-each-animated-gif-frame-to-a-separate-bufferedimage
     */
    @Throws(IOException::class)
    fun readGif(reader: ImageReader) : List<ImageFrame> {
        val frames = mutableListOf<ImageFrame>()

        var width = -1
        var height = -1

        val metadata = reader.streamMetadata
        if (metadata != null) {
            val globalRoot = metadata.getAsTree(metadata.nativeMetadataFormatName) as IIOMetadataNode
            val globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor")

            if (globalScreenDescriptor != null && globalScreenDescriptor.length > 0) {
                val screenDescriptor = globalScreenDescriptor.item(0) as IIOMetadataNode
                width = screenDescriptor.getAttribute("logicalScreenWidth").toInt()
                height = screenDescriptor.getAttribute("logicalScreenHeight").toInt()
            }
        }

        var master: BufferedImage? = null
        var masterGraphics: Graphics2D? = null

        for (frameIndex in 0..Int.MAX_VALUE) {
            val image: BufferedImage
            try {
                image = reader.read(frameIndex)
            } catch(io: IndexOutOfBoundsException) {
                break
            }

            if (width == -1 || height == -1) {
                width = image.width
                height = image.height
            }

            val root = reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0") as IIOMetadataNode
            val gce = root.getElementsByTagName("GraphicControlExtension").item(0) as IIOMetadataNode
            val delay = gce.getAttribute("delayTime").toInt()
            val disposal = gce.getAttribute("disposalMethod")

            var x = 0
            var y = 0

            if (master == null) {
                master = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                masterGraphics = master.createGraphics()
                masterGraphics.background = Color(0, 0, 0, 0)
            } else {
                for (i in 0 until root.childNodes.length) {
                    val child = root.childNodes.item(i)
                    if (child.nodeName == "ImageDescriptor") {
                        x = child.attributes.getNamedItem("imageLeftPosition").nodeValue.toInt()
                        y = child.attributes.getNamedItem("imageTopPosition").nodeValue.toInt()
                    }
                }
            }
            masterGraphics!!.drawImage(image, x, y, null)

            val copy = BufferedImage(master.colorModel, master.copyData(null), master.isAlphaPremultiplied, null)
            frames.add(ImageFrame(copy, delay, disposal))

            if (disposal == "restoreToPrevious") {
                var from: BufferedImage? = null
                for (i in (frameIndex - 1)..0) {
                    if (frames[i].disposal != "restoreToPrevious" || i == 0) {
                        from = frames[i].image
                        break
                    }
                }

                master = BufferedImage(from!!.colorModel, from.copyData(null), from.isAlphaPremultiplied, null)
                masterGraphics = master.createGraphics()
                masterGraphics.background = Color(0, 0, 0, 0)
            } else if (disposal == "restoreToBackgroundColor") {
                masterGraphics.clearRect(x, y, image.width, image.height)
            }
        }
        reader.dispose()
        return frames
    }
}