package com.retrobot.core.koin

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.twitch4j.helix.TwitchHelix
import com.github.twitch4j.helix.TwitchHelixBuilder
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
import org.koin.core.qualifier.named
import org.koin.dsl.module


object BotModule {
    val modules = listOf(
        coreModule(),
        kqbModule(),
        twitchModule()
    )

    private fun coreModule() = module {
        single { provideBotDatabaseConfig() }
        single { CoreDatabase() }
        single<Cache<String, GuildSettings>> { LRUCache(PerpetualCache()) }
        single<GuildSettingsRepository> { ExposedGuildSettingsRepository(get(), get()) }

        single { CommandHandler() }
        single { ReactionHandler() }
        single { ServiceHandler() }

        single { ReactionListenerCleanupService(get()) }
        single { ServiceCleanupService(get()) }
        single(named("startup_services")) { provideStartupServices(get(), get(), get()) }
    }

    private fun kqbModule() = module {
        single { KqbDatabase() }

        single<AwardRepository> { ExposedAwardRepository(get()) }
        single<CasterRepository> { ExposedCasterRepository(get()) }
        single<MatchRepository> { ExposedMatchRepository(get()) }
        single<TeamRepository> { ExposedTeamRepository(get()) }

        factory { provideCsvReader() }
        single { KqbAlmanacService() }

        factory { GetMatchesUseCase(get(), get(), get()) }
        factory { GetPlayerUseCase(get(), get(), get()) }
    }

    private fun twitchModule() = module {
        single { provideTwitchHelix() }
        single<TwitchRepository> { Twitch4JTwitchRepository(get()) }
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