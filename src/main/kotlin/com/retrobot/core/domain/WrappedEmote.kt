package com.retrobot.core.domain

import net.dv8tion.jda.api.entities.Emote

open class WrappedEmote
data class CustomEmote(val emote: Emote): WrappedEmote()
data class UnicodeEmote(val unicode: String): WrappedEmote()