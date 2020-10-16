package com.retrobot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.test.KoinTest

/**
 * Base testing class to use when Koin is needed, because its finicky with JUnit5
 */
@ExperimentalCoroutinesApi
abstract class BaseKoinTest : KoinTest {

    abstract val testModule: Module

    private val testDispatcher = TestCoroutineDispatcher()

    @BeforeEach
    private fun _beforeEach() {
        Dispatchers.setMain(testDispatcher)
        startKoin { modules(testModule) }
    }

    @AfterEach
    private fun _afterEach() {
        stopKoin()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}