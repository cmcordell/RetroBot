package com.retrobot.core.koin

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.domain.reaction.ReactionHandler
import com.retrobot.core.domain.service.ReactionListenerCleanupService
import com.retrobot.core.domain.service.ServiceCleanupService
import com.retrobot.core.domain.service.ServiceHandler
import com.retrobot.kqb.GetMatchesUseCase
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.data.exposedrepo.ExposedCasterRepository
import com.retrobot.kqb.data.exposedrepo.ExposedMatchRepository
import com.retrobot.kqb.data.exposedrepo.ExposedTeamRepository
import com.retrobot.kqb.service.KqbAlmanacService
import org.koin.core.qualifier.named
import org.koin.dsl.module


private fun provideCsvReader(): CsvReader {
    return csvReader { escapeChar = '\\' }
}

private fun provideStartupServices(
    kqbAlmanacService: KqbAlmanacService,
    reactionListenerCleanupService: ReactionListenerCleanupService,
    serviceCleanupService: ServiceCleanupService
) = listOf(kqbAlmanacService, reactionListenerCleanupService, serviceCleanupService)

val dataModule = module {
    single { ExposedGuildSettingsRepository() as GuildSettingsRepository }

    single { ExposedCasterRepository() as CasterRepository }
    single { ExposedMatchRepository() as MatchRepository }
    single { ExposedTeamRepository() as TeamRepository }
}

val domainModule = module {
    factory { provideCsvReader() }

    single { ReactionHandler() }
    single { ServiceHandler() }

    single { KqbAlmanacService() }
    single { ReactionListenerCleanupService(get()) }
    single { ServiceCleanupService(get()) }

    single(named("StartupServices")) { provideStartupServices(get(), get(), get()) }

    factory { GetMatchesUseCase(get(), get(), get()) }
}

val botModules = listOf(dataModule, domainModule)