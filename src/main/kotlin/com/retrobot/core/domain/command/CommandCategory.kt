package com.retrobot.core.domain.command

import com.retrobot.core.Commands.KQB
import com.retrobot.core.Commands.PollsAndEvents
import com.retrobot.core.Commands.Settings
import com.retrobot.core.Commands.Steam
import com.retrobot.core.Commands.Utils

/**
 * Each [Command] has a [CommandCategory]
 */
enum class CommandCategory(
        val title: String,
        val description: String,
        val aliases: List<String>
) {
    HIDDEN("", "", listOf("")),
    POLL_OR_EVENT(PollsAndEvents.TITLE, PollsAndEvents.DESCRIPTION, PollsAndEvents.ALIASES),
    SETTING(Settings.TITLE, Settings.DESCRIPTION, Settings.ALIASES),
    UTILITY(Utils.TITLE, Utils.DESCRIPTION, Utils.ALIASES),
    KILLER_QUEEN(KQB.TITLE, KQB.DESCRIPTION, KQB.ALIASES),
    STEAM(Steam.TITLE, Steam.DESCRIPTION, Steam.ALIASES)
}