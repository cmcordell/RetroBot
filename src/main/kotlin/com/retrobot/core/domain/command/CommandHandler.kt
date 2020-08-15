package com.retrobot.core.domain.command

import com.retrobot.kqb.command.KqbCompetitionCommand
import com.retrobot.polls.command.PollCommand
import com.retrobot.settings.ColorCommand
import com.retrobot.settings.NicknameCommand
import com.retrobot.settings.PrefixCommand
import com.retrobot.twitch.StreamsCommand
import com.retrobot.utility.*

/**
 * Handler for all [Command]s.
 */
class CommandHandler {
    val commandSet = CommandSet(
            // Utilities
            HelpCommand(),
            PingCommand(),
            CoinflipCommand(),
            RandomCommand(),
            JumboCommand(),
            PollCommand(),

            // Settings
            ColorCommand(),
            NicknameCommand(),
            PrefixCommand(),

            // Twitch
            StreamsCommand(),

            // Killer Queen Black
            KqbCompetitionCommand()
    )
}