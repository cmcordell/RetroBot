package com.retrobot.core.command

import com.retrobot.core.Commands.KQB
import com.retrobot.core.Commands.PollsAndEvents
import com.retrobot.core.Commands.Settings
import com.retrobot.core.Commands.Utils

enum class CommandCategory(
        val title: String,
        val description: String,
        val aliases: List<String>
) {
    HIDDEN("", "", listOf("")),
    POLL_OR_EVENT(PollsAndEvents.TITLE, PollsAndEvents.DESCRIPTION, PollsAndEvents.ALIASES),
    SETTING(Settings.TITLE, Settings.DESCRIPTION, Settings.ALIASES),
    UTILITY(Utils.TITLE, Utils.DESCRIPTION, Utils.ALIASES),
    KILLER_QUEEN(KQB.TITLE, KQB.DESCRIPTION, KQB.ALIASES)
}