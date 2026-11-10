package org.milad.expense_share.data.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.net.URI
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.milad.expense_share.data.db.table.*

object DatabaseFactory {

    fun init() {
        val db = Database.connect(hikari())

        transaction(db) {
            SchemaUtils.create(
                Users,
                Passwords,
                FriendRelations,
                Groups,
                GroupMembers,
                Transactions,
                TransactionPayers,
                TransactionShares,
                TransactionShareMembers,
                Settlements,
            )
        }
    }

    private fun hikari(): HikariDataSource {
        val rawUrl = System.getenv("JDBC_DATABASE_URL")
            ?: System.getenv("DATABASE_URL")
            ?: System.getenv("JDBC_URL")

        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"

            if (rawUrl != null) {
                if (rawUrl.startsWith("jdbc:")) {
                    jdbcUrl = rawUrl
                    System.getenv("DB_USER")?.let { username = it }
                    System.getenv("DB_PASSWORD")?.let { password = it }
                } else {
                    // Parses postgres://user:pass@host:port/dbname?sslmode=require
                    try {
                        val uri = URI(rawUrl)
                        val userInfo = uri.userInfo?.split(":")
                        val dbUser = userInfo?.getOrNull(0) ?: System.getenv("DB_USER")
                        val dbPassword = userInfo?.getOrNull(1) ?: System.getenv("DB_PASSWORD")
                        val host = uri.host
                        val port = if (uri.port != -1) uri.port else 5432
                        val path = uri.path ?: "/expenseshare"
                        val query = if (uri.query != null) "?${uri.query}" else ""

                        jdbcUrl = "jdbc:postgresql://$host:$port$path$query"
                        dbUser?.let { username = it }
                        dbPassword?.let { password = it }
                    } catch (_: Exception) {
                        jdbcUrl = rawUrl
                    }
                }
            } else {
                jdbcUrl = "jdbc:postgresql://localhost:5432/expenseshare"
                username = System.getenv("DB_USER") ?: "postgres"
                password = System.getenv("DB_PASSWORD") ?: "miladmilad"
            }

            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }
}
