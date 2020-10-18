package com.retrobot.core.util

import com.natpryce.konfig.*
import com.retrobot.core.BuildType


object Properties {

    object bot : PropertyGroup() {
        val buildType by enumType<BuildType>()
        val token by stringType
    }

    object api : PropertyGroup() {
        val steamKey by stringType
    }

    fun config(): ConfigurationProperties {
        return botProperties()
    }

    private fun botProperties(): ConfigurationProperties {
        return ConfigurationProperties.fromResource("bot.properties")
    }
}