package com.retrobot.core.util

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.ResultSet


class UpsertStatement<Key : Any>(table: Table, private val onDupUpdate: List<Column<*>>) : InsertStatement<Key>(table, false) {
    override fun prepareSQL(transaction: Transaction): String {
        val onUpdateSQL = if (onDupUpdate.isNotEmpty()) {
            " ON DUPLICATE KEY UPDATE " + onDupUpdate.joinToString { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
        } else ""
        return super.prepareSQL(transaction) + onUpdateSQL
    }
}

fun <T : Table> T.upsert(onDupUpdateColumns: List<Column<*>>, body: T.(UpsertStatement<Number>)->Unit): UpsertStatement<Number> = UpsertStatement<Number>(this, onDupUpdateColumns).apply {
    body(this)
    execute(TransactionManager.current())
}


class BatchUpsertStatement(table: Table, private val onDupUpdate: List<Column<*>>) : BatchInsertStatement(table, false) {
    override fun prepareSQL(transaction: Transaction): String {
        val onUpdateSQL = if (onDupUpdate.isNotEmpty()) {
            " ON DUPLICATE KEY UPDATE " + onDupUpdate.joinToString { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
        } else ""
        return super.prepareSQL(transaction) + onUpdateSQL
    }
}

fun <T : Table, E> T.batchUpsert(data: Collection<E>, onDupUpdateColumns: List<Column<*>>, body: T.(BatchUpsertStatement, E) -> Unit) {
    data.takeIf { it.isNotEmpty() }?.let {
        val upsert = BatchUpsertStatement(this, onDupUpdateColumns)
        data.forEach {
            upsert.addBatch()
            body(upsert, it)
        }
        TransactionManager.current().exec(upsert)
    }
}

suspend fun <T> dbQuery(db: Database? = null, block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, db) { block() }

suspend fun <T> dbActionQuery(db: Database? = null, block: suspend () -> T) {
    newSuspendedTransaction(Dispatchers.IO, db) { block() }
}

fun <T : Any> String.execAndMap(transform : (ResultSet) -> T) : List<T> {
    val result = arrayListOf<T>()
    TransactionManager.current().exec(this) { rs ->
        while (rs.next()) {
            result += transform(rs)
        }
    }
    return result
}