package com.retrobot.core.util

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*


object File {
    fun getResourceAsStream(resource: String): InputStream? {
        return Thread.currentThread().contextClassLoader?.getResourceAsStream(resource)
            ?: resource::class.java.classLoader?.getResourceAsStream(resource)
    }

    @Throws(IOException::class)
    fun createTemporaryFile(formatName: String) : File {
        return File.createTempFile(UUID.randomUUID().toString(), ".$formatName")
    }
}