package com.retrobot.core.koin

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.chat.TwitchChatBuilder
import com.github.twitch4j.helix.TwitchHelix
import com.github.twitch4j.helix.TwitchHelixBuilder
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.domain.command.CommandHandler
import com.retrobot.core.domain.reaction.ReactionHandler
import com.retrobot.core.domain.service.ReactionListenerCleanupService
import com.retrobot.core.domain.service.ServiceCleanupService
import com.retrobot.core.domain.service.ServiceHandler
import com.retrobot.kqb.domain.usecase.GetMatchesUseCase
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.data.exposedrepo.ExposedCasterRepository
import com.retrobot.kqb.data.exposedrepo.ExposedMatchRepository
import com.retrobot.kqb.data.exposedrepo.ExposedTeamRepository
import com.retrobot.kqb.service.KqbAlmanacService
import com.retrobot.twitch.data.Twitch4JTwitchRepository
import com.retrobot.twitch.data.TwitchRepository
import com.retrobot.twitch.domain.TwitchStreamsUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module


object BotModule {
    private fun provideCsvReader(): CsvReader {
        return csvReader { escapeChar = '\\' }
    }

    private fun provideStartupServices(
            kqbAlmanacService: KqbAlmanacService,
            reactionListenerCleanupService: ReactionListenerCleanupService,
            serviceCleanupService: ServiceCleanupService
    ) = listOf(kqbAlmanacService, reactionListenerCleanupService, serviceCleanupService)

    private fun provideTwitchHelix(): TwitchHelix {
        val CLIENT_ID = "0fspyb9nqlbc8zmkn3rcjo71yumprd"
        val CLIENT_SECRET = "7u5zzbyoag2irgza4tfu453bx0uu3x"
        return TwitchHelixBuilder.builder()
            .withClientId(CLIENT_ID)
            .withClientSecret(CLIENT_SECRET)
            .build()
    }

    private fun provideTwitchChat(): TwitchChat {
        val CLIENT_ID = "0fspyb9nqlbc8zmkn3rcjo71yumprd"
        val CLIENT_SECRET = "7u5zzbyoag2irgza4tfu453bx0uu3x"
        val RETROBOT_CHAT_ACCOUNT_TOKEN = "oauth:cr1ukurcz1sv2n4ryikyprgudx1loe"
        val credential = OAuth2Credential("twitch", RETROBOT_CHAT_ACCOUNT_TOKEN)
        return TwitchChatBuilder.builder()
            .withClientId(CLIENT_ID)
            .withClientSecret(CLIENT_SECRET)
            .withChatAccount(credential)
            .build()
    }

    private val dataModule = module {
        single { ExposedGuildSettingsRepository() as GuildSettingsRepository }

        single { ExposedCasterRepository() as CasterRepository }
        single { ExposedMatchRepository() as MatchRepository }
        single { ExposedTeamRepository() as TeamRepository }

        single { provideTwitchHelix() }
        single { Twitch4JTwitchRepository(get()) as TwitchRepository }
        single { TwitchStreamsUseCase(get()) }
    }

    private val domainModule = module {
        factory { provideCsvReader() }

        single { CommandHandler() }
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