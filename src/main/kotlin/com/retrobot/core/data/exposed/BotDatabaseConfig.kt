package com.retrobot.core.data.exposed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.flywaydb.core.Flyway

/**
 * Used to configure every [Database] for RetroBot
 */
class BotDatabaseConfig(
    private val databases: List<Database>
) {

    fun init() {
        GlobalScope.launch(Dispatchers.IO) {
            databases.forEach { database ->
                performMigrations(database)
            }
        }
    }

    private fun performMigrations(database: Database) {
        Flyway.configure()
                .dataSource(database.dataSource)
                .locations(database.migrationsPath)
                .load()
                .migrate()
    }
}