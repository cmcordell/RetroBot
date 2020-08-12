package com.retrobot.core

import com.natpryce.konfig.*


object Properties {

    object bot : PropertyGroup() {
        val buildType by enumType<BuildType>()
        val token by stringType
    }

    fun config(): ConfigurationProperties {
        return botProperties()
    }

    private fun botProperties(): ConfigurationProperties {
        return ConfigurationProperties.fromResource("bot.properties")
    }
}