package com.retrobot.core.data.exposed

import org.jetbrains.exposed.sql.Table
import javax.sql.DataSource


interface Database {
    val name: String
    val dataSource: DataSource
    val tables: List<Table>
    val migrationsPath: String
}