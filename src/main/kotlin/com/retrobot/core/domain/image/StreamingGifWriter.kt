package com.retrobot.core.domain.image

import com.sksamuel.scrimage.ImmutableImage
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.FileImageOutputStream

/**
 *
 */
class StreamingGifWriter(
    private val frameDelay: Duration = Duration.ofSeconds(1),
    private val infiniteLoop: Boolean = true
) {
    data class GifAttributes(
        val disposal: String = "None",
        val userInputFlag: Boolean = false,
        val transparentColorFlag: Boolean = false,
        val delay: Duration = Duration.ofSeconds(1),
        val transparentColorIndex: Int = 0
    )

    /**
     * Returns an existing child node, or creates and returns a new child node (if
     * the requested node does not exist).
     *
     * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
     * @param nodeName the name of the child node.
     * @return the child node, if found or a new node created with the given name.
     */
    private fun getNode(rootNode: IIOMetadataNode, nodeName: String) : IIOMetadataNode{
        for (i in 0 until rootNode.length) {
            val node = rootNode.item(i) as IIOMetadataNode
            if (node.nodeName.equals(nodeName, true)) {
                return node
            }
        }

        val node = IIOMetadataNode(nodeName)
        rootNode.appendChild(node)
        return node
    }

    interface GifStream : AutoCloseable {
        @Throws(IOException::class)
        fun writeFrame(image: ImmutableImage): GifStream

        @Throws(IOException::class)
        fun writeFrame(image: ImmutableImage, attributes: GifAttributes): GifStream
    }

    @Throws(IOException::class)
    fun prepareStream(path: String, imageType: Int) : GifStream {
        return prepareStream(Paths.get(path), imageType)
    }

    @Throws(IOException::class)
    fun prepareStream(path: Path, imageType: Int) : GifStream {
        return prepareStream(path.toFile(), imageType)
    }

    @Throws(IOException::class)
    fun prepareStream(file: File, imageType: Int) : GifStream {
        val writer = ImageIO.getImageWritersBySuffix("gif").next()
        val imageWriteParam = writer.defaultWriteParam
        val imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType)
        val imageMetaData = writer.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam)
        val metaFormatName = imageMetaData.nativeMetadataFormatName
        val root = imageMetaData.getAsTree(metaFormatName) as IIOMetadataNode

        val graphicsControlExtensionNode = getNode(root, "GraphicControlExtension")
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none")
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE")
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE")
        graphicsControlExtensionNode.setAttribute("delayTime", (frameDelay.toMillis()).toString())
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0")

        val appExtensionsNode = getNode(root, "ApplicationExtensions")
        val child = IIOMetadataNode("ApplicationExtension")

        child.setAttribute("applicationID", "NETSCAPE")
        child.setAttribute("authenticationCode", "2.0")

        val loop = if (infiniteLoop) 0 else 1
        val childObj = ByteArray(3)
        childObj[0] = 0x1
        childObj[1] = (loop and 0xFF).toByte()
        childObj[2] = ((loop shr 8) and 0xFF).toByte()
        child.userObject = childObj
        appExtensionsNode.appendChild(child)

        imageMetaData.setFromTree(metaFormatName, root)

        val fos = FileImageOutputStream(file)
        writer.output = fos
        writer.prepareWriteSequence(null)

        return object : GifStream {
            @Throws(IOException::class)
            override fun writeFrame(image: ImmutableImage) : GifStream {
                writer.writeToSequence(IIOImage(image.awt(), null, imageMetaData), imageWriteParam)
                return this
            }

            @Throws(IOException::class)
            override fun writeFrame(image: ImmutableImage, attributes: GifAttributes) : GifStream {
                val rootOverride = imageMetaData.getAsTree(metaFormatName) as IIOMetadataNode
                val graphicsControlExtensionNodeOverride = getNode(rootOverride, "GraphicControlExtension")
                graphicsControlExtensionNodeOverride.setAttribute("disposalMethod", attributes.disposal)
                graphicsControlExtensionNodeOverride.setAttribute("userInputFlag", attributes.userInputFlag.toString())
                graphicsControlExtensionNodeOverride.setAttribute("transparentColorFlag", attributes.transparentColorFlag.toString())
                graphicsControlExtensionNodeOverride.setAttribute("delayTime", (attributes.delay.toMillis()).toString())
                graphicsControlExtensionNodeOverride.setAttribute("transparentColorIndex", attributes.transparentColorIndex.toString())
                imageMetaData.setFromTree(metaFormatName, rootOverride)

                writer.writeToSequence(IIOImage(image.awt(), null, imageMetaData), imageWriteParam)

                imageMetaData.setFromTree(metaFormatName, root)
                return this
            }

            @Throws(IOException::class)
            override fun close() {
                writer.endWriteSequence()
                writer.dispose()
                fos.close()
            }
        }
    }
}