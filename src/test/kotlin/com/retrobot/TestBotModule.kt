package com.retrobot

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposed.ExposedGuildSettingsRepository
import com.retrobot.core.domain.reaction.ReactionHandler
import com.retrobot.core.domain.service.ReactionListenerCleanupService
import com.retrobot.core.domain.service.ServiceCleanupService
import com.retrobot.core.domain.service.ServiceHandler
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.data.exposedrepo.ExposedCasterRepository
import com.retrobot.kqb.data.exposedrepo.ExposedMatchRepository
import com.retrobot.kqb.data.exposedrepo.ExposedTeamRepository
import com.retrobot.kqb.domain.usecase.GetMatchesUseCase
import com.retrobot.kqb.service.KqbAlmanacService
import org.koin.core.qualifier.named
import org.koin.dsl.module


object TestBotModule {
    private fun provideCsvReader(): CsvReader {
        return csvReader { escapeChar = '\\' }
    }

    private fun provideStartupServices(
            kqbAlmanacService: KqbAlmanacService,
            reactionListenerCleanupService: ReactionListenerCleanupService,
            serviceCleanupService: ServiceCleanupService
    ) = listOf(kqbAlmanacService, reactionListenerCleanupService, serviceCleanupService)

    private val dataModule = module {
        single { ExposedGuildSettingsRepository() as GuildSettingsRepository }

        single { ExposedCasterRepository() as CasterRepository }
        single { ExposedMatchRepository() as MatchRepository }
        single { ExposedTeamRepository() as TeamRepository }
    }

    private val domainModule = module {
        factory { provideCsvReader() }

        single { ReactionHandler() }
        single { ServiceHandler() }

        single { KqbAlmanacService() }
        single { ReactionListenerCleanupService(get()) }
        single { ServiceCleanupService(get()) }

        single(named("StartupServices")) { provideStartupServices(get(), get(), get()) }

        factory { GetMatchesUseCase(get(), get(), get()) }
    }

    val modules = listOf(dataModule, domainModule)
}