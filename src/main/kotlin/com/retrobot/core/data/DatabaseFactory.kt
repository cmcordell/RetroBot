package com.retrobot.core.data

import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.util.dbActionQuery
import com.retrobot.kqb.data.exposedrepo.ExposedCasterRepository
import com.retrobot.kqb.data.exposedrepo.ExposedMatchRepository
import com.retrobot.kqb.data.exposedrepo.ExposedTeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import javax.sql.ConnectionPoolDataSource


object DatabaseFactory {
    fun connect() {
        GlobalScope.launch(Dispatchers.IO) {
            Database.connect(h2())
            dbActionQuery {
                SchemaUtils.drop(
                        ExposedCasterRepository.Casters,
                        ExposedMatchRepository.Matches,
                        ExposedTeamRepository.Teams
                )
                SchemaUtils.createDatabase("retrobot")
                SchemaUtils.create(
                        ExposedGuildSettingsRepository.GuildSettings,
                        ExposedCasterRepository.Casters,
                        ExposedMatchRepository.Matches,
                        ExposedTeamRepository.Teams
                )
            }
        }
    }

    private fun h2() : ConnectionPoolDataSource = JdbcDataSource().apply {
        setUrl("jdbc:h2:~/retrobot;mode=MySQL")
        user = "root"
    }
}