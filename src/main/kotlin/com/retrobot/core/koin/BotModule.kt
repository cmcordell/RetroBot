package com.retrobot.core.koin

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.twitch4j.helix.TwitchHelix
import com.github.twitch4j.helix.TwitchHelixBuilder
import com.retrobot.calendar.data.CalendarRepository
import com.retrobot.calendar.data.EventRepository
import com.retrobot.calendar.data.exposedrepo.CalendarDatabase
import com.retrobot.calendar.data.exposedrepo.ExposedCalendarRepository
import com.retrobot.calendar.data.exposedrepo.ExposedEventRepository
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.cache.Cache
import com.retrobot.core.data.cache.LRUCache
import com.retrobot.core.data.cache.PerpetualCache
import com.retrobot.core.data.exposed.BotDatabaseConfig
import com.retrobot.core.data.exposed.CoreDatabase
import com.retrobot.core.data.exposed.ExposedGuildSettingsRepository
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.CommandHandler
import com.retrobot.core.domain.reaction.ReactionHandler
import com.retrobot.core.domain.service.ReactionListenerCleanupService
import com.retrobot.core.domain.service.ServiceCleanupService
import com.retrobot.core.domain.service.ServiceHandler
import com.retrobot.core.util.Properties
import com.retrobot.kqb.data.AwardRepository
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.data.exposedrepo.*
import com.retrobot.kqb.domain.usecase.GetMatchesUseCase
import com.retrobot.kqb.domain.usecase.GetPlayerUseCase
import com.retrobot.kqb.service.KqbAlmanacService
import com.retrobot.twitch.data.Twitch4JTwitchRepository
import com.retrobot.twitch.data.TwitchRepository
import com.retrobot.twitch.domain.TwitchStreamsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.core.qualifier.named
import org.koin.dsl.module


object BotModule {
    val modules = listOf(
        coreModule(),
        calendarModule(),
        kqbModule(),
        twitchModule()
    )


    private fun coreModule() = module {
        single { provideBotDatabaseConfig() }
        single { CoreDatabase() }
        single { LRUCache<String, GuildSettings>(PerpetualCache()) as Cache<String, GuildSettings> }
        single { ExposedGuildSettingsRepository(get(), get()) as GuildSettingsRepository }

        single { CommandHandler() }
        single { ReactionHandler() }
        single { ServiceHandler() }

        single { ReactionListenerCleanupService(get()) }
        single { ServiceCleanupService(get()) }
        single(named("startup_services")) { provideStartupServices(get(), get(), get()) }
    }

    private fun calendarModule() = module {
        single { CalendarDatabase() }
        single { ExposedCalendarRepository(get(), get()) as CalendarRepository}
        single { ExposedEventRepository(get()) as EventRepository }
    }

    private fun kqbModule() = module {
        single { KqbDatabase() }
        single { ExposedAwardRepository(get()) as AwardRepository }
        single { ExposedCasterRepository(get()) as CasterRepository }
        single { ExposedMatchRepository(get()) as MatchRepository }
        single { ExposedTeamRepository(get()) as TeamRepository }

        factory { provideCsvReader() }
        single { KqbAlmanacService() }

        factory { GetMatchesUseCase(get(), get(), get()) }
        factory { GetPlayerUseCase(get(), get(), get()) }
    }

    private fun twitchModule() = module {
        single { provideTwitchHelix() }
        single { Twitch4JTwitchRepository(get()) as TwitchRepository }
        single { TwitchStreamsUseCase(get()) }
    }


    private fun provideBotDatabaseConfig(): BotDatabaseConfig {
        return BotDatabaseConfig(listOf(CoreDatabase(), KqbDatabase()))
    }

    private fun provideCsvReader(): CsvReader {
        return csvReader { escapeChar = '\\' }
    }

    private fun provideStartupServices(
        kqbAlmanacService: KqbAlmanacService,
        reactionListenerCleanupService: ReactionListenerCleanupService,
        serviceCleanupService: ServiceCleanupService
    ) = listOf(kqbAlmanacService, reactionListenerCleanupService, serviceCleanupService)

    private fun provideTwitchHelix(): TwitchHelix {
        return TwitchHelixBuilder.builder()
            .withClientId(Properties.config()[Properties.api.twitchClientId])
            .withClientSecret(Properties.config()[Properties.api.twitchClientSecret])
            .build()
    }
}