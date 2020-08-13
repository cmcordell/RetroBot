package com.retrobot.core.koin

import com.retrobot.TestBotModule
import org.junit.experimental.categories.Category
import org.junit.jupiter.api.Test
import org.koin.test.AutoCloseKoinTest
import org.koin.test.category.CheckModuleTest
import org.koin.test.check.checkModules

@Category(CheckModuleTest::class)
class BotModuleTest : AutoCloseKoinTest() {

    @Test
    fun checkBotModules() = checkModules {
        modules(TestBotModule.modules)
    }
}