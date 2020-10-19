package com.retrobot.kqb.command

import com.retrobot.core.Commands.KQB.Competition.CATEGORY
import com.retrobot.core.Commands.KQB.Competition.COMMAND
import com.retrobot.core.Commands.KQB.Competition.DESCRIPTION
import com.retrobot.core.Commands.KQB.Competition.USAGE
import com.retrobot.core.domain.command.CompositeCommand

/**
 * !kqb
 * !kqb awards
 * !kqb awards <circuit>, <division>, <conference> | e.g. !kqb standings West, 1, b
 * !kqb casters
 * !kqb caster <caster name>
 * !kqb matches
 * !kqb matches <month/day>
 * !kqb match next
 * !kqb news
 * !kqb news <1-10>
 * !kqb player <player name (full)>
 * !kqb replays
 * !kqb replays <month/day>
 * !kqb replay last
 * !kqb standings
 * !kqb standings <circuit>, <division>, <conference> | e.g. !kqb standings West, 1, b
 * !kqb stats
 * !kqb teams
 * !kqb team <team name>
 */
class KqbCommand : CompositeCommand() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    override val subCommands = listOf(
            AwardsSubCommand(),
            CastersSubCommand(),
            MatchesSubCommand(),
            NewsSubCommand(),
            PlayersSubCommand(),
            ReplaysSubCommand(),
            StandingsSubCommand(),
            StatsSubCommand(),
            TeamsSubCommand()
    )
}