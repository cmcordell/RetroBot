package com.retrobot.kqb.service

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.retrobot.BaseKoinTest
import com.retrobot.kqb.data.AwardRepository
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.model.Award
import com.retrobot.kqb.domain.model.Caster
import com.retrobot.kqb.domain.model.Match
import com.retrobot.kqb.domain.model.Team
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.inject

@ExperimentalCoroutinesApi
internal class KqbAlmanacServiceTest : BaseKoinTest() {

    private val awardRepo = mockk<AwardRepository>(relaxed = true)
    private val casterRepo = mockk<CasterRepository>(relaxed = true)
    private val matchRepo = mockk<MatchRepository>(relaxed = true)
    private val teamRepo = mockk<TeamRepository>(relaxed = true)
    private val csvReader = mockk<CsvReader>(relaxed = true)

    override val testModule = module {
        single { awardRepo }
        single { casterRepo }
        single { matchRepo }
        single { teamRepo }
        factory { csvReader }

        single { KqbAlmanacService() }
    }

    private val kqbAlmanacService: KqbAlmanacService by inject()

    @Test
    @DisplayName("Service.start() - Awards inserted into DB")
    fun start_insertsDataIntoAwardsRepo() = runBlockingTest {
//        val pullAwardsMethod = kqbAlmanacService.javaClass.getDeclaredMethod("pullAwards")
//        pullAwardsMethod.isAccessible = true
//        pullAwardsMethod.invoke(kqbAlmanacService)
//        coVerify(exactly = 1) { awardRepo.put(any<List<Award>>()) }

        kqbAlmanacService.start()
        coVerify(exactly = 1) { awardRepo.clear() }
        coVerify(exactly = 1) { awardRepo.put(any<List<Award>>()) }
    }

    @Test
    @DisplayName("Service.start() - Casters inserted into DB")
    fun start_insertsDataIntoCasterRepo() = runBlockingTest {
        kqbAlmanacService.start()
        coVerify(exactly = 1) { casterRepo.clear() }
        coVerify(exactly = 1) { casterRepo.put(any<List<Caster>>()) }
    }

    @Test
    @DisplayName("Service.start() - Matches inserted into DB")
    fun start_insertsDataIntoMatchRepo() = runBlockingTest {
        kqbAlmanacService.start()
        coVerify(exactly = 1) { matchRepo.clear() }
        coVerify(exactly = 1) { matchRepo.put(any<List<Match>>()) }
    }

    @Test
    @DisplayName("Service.start() - Teams inserted into DB")
    fun start_insertsDataIntoTeamRepo() = runBlockingTest {
        kqbAlmanacService.start()
        coVerify(exactly = 1) { teamRepo.clear() }
        coVerify(exactly = 1) { teamRepo.put(any<List<Team>>()) }
    }
}