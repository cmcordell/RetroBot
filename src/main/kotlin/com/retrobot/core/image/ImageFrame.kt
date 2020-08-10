package com.retrobot.core.image

import java.awt.image.BufferedImage

data class ImageFrame(
    val image: BufferedImage,
    val delay: Int,
    val disposal: String
)